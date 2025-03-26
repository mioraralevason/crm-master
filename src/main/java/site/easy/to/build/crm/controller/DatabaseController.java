package site.easy.to.build.crm.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import site.easy.to.build.crm.service.budget.CustomerBudgetService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.database.CsvService;
import site.easy.to.build.crm.service.database.DatabaseService;
import site.easy.to.build.crm.service.expense.ExpenseService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.ticket.TicketService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.dto.JavaImportDataFeuille1Dto;
import site.easy.to.build.crm.dto.JavaImportDataFeuille3Dto;
import site.easy.to.build.crm.dto.JavaImportDataFeuille4Dto;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.CustomerBudget;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;

@Controller
public class DatabaseController {

    @Autowired
    private DatabaseService databaseService;
    
    @Autowired
    private CsvService csvService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private LeadService leadService;
    
    @Autowired
    private CustomerBudgetService customerBudgetService;
    
    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private ExpenseService expenseService;

    @GetMapping("database/my-database")
    public String showDatabasePage(Model model, Authentication authentication) {
        model.addAttribute("message", "Hello");
        return "database/my-database";
    }

    @PostMapping("/restore/database")
    public String restoreDatabase(Model model) {
        try {
            // Restaurer la base de données
            databaseService.restoreDatabase();
            model.addAttribute("message", "Base de données restaurée avec succès !");
        } catch (Exception e) {
            model.addAttribute("message", "Échec de la restauration de la base de données : " + e.getMessage());
        }
        return "database/my-database";
    }
    
    @PostMapping("/database/importCSV")
    @Transactional(rollbackFor = Exception.class)
    public String importEspace(
            @RequestParam("file1") MultipartFile file1,
            @RequestParam("file2") MultipartFile file2,
            @RequestParam("file3") MultipartFile file3,
            Model model) {

        if (file1.isEmpty() && file2.isEmpty() && file3.isEmpty()) {
            model.addAttribute("message", "Erreur : Aucun fichier sélectionné.");
            return "database/my-database";
        }

        List<String> errors = new ArrayList<>();
        int customersImported = 0, leadsImported = 0, ticketsImported = 0, budgetsImported = 0;
        Path tempFile1 = null, tempFile2 = null, tempFile3 = null;

        try {
            // Traitement du fichier 2 (Customers)
            if (!file2.isEmpty()) {
                tempFile2 = Files.createTempFile("csv_upload_", file2.getOriginalFilename());
                Files.copy(file2.getInputStream(), tempFile2, StandardCopyOption.REPLACE_EXISTING);
                List<JavaImportDataFeuille3Dto> importedData2 = csvService.importCsv(tempFile2.toString(), JavaImportDataFeuille3Dto.class);
                for (int i = 0; i < importedData2.size(); i++) {
                    JavaImportDataFeuille3Dto dto = importedData2.get(i);
                    int lineNumber = i + 1;
                    Customer existingCustomer = customerService.findByEmail(dto.getCustomerEmail());
                    if (existingCustomer != null) {
                        errors.add("Fichier 2, Ligne " + lineNumber + " : Email en double détecté : " + dto.getCustomerEmail());
                        continue;
                    }
                    Customer customer = new Customer();
                    customer.setEmail(dto.getCustomerEmail());
                    customer.setName(dto.getCustomerName());
                    customer.setUser(userService.findById(52));
                    customer.setCustomerLoginInfo(null);
                    customer.setCreatedAt(LocalDateTime.now());
                    customer.setCountry("Unknown");
                    customer.setPhone(generateRandomPhone());
                    customer.setAddress(generateRandomAddress());
                    customer.setCity("Unknown City");
                    customerService.save(customer);
                    customersImported++;
                }
            }

            // Traitement du fichier 1 (Leads et Tickets)
            if (!file1.isEmpty()) {
                tempFile1 = Files.createTempFile("csv_upload_", file1.getOriginalFilename());
                Files.copy(file1.getInputStream(), tempFile1, StandardCopyOption.REPLACE_EXISTING);
                List<JavaImportDataFeuille1Dto> importedData1 = csvService.importCsv(tempFile1.toString(), JavaImportDataFeuille1Dto.class);
                for (int i = 0; i < importedData1.size(); i++) {
                    JavaImportDataFeuille1Dto dto = importedData1.get(i);
                    int lineNumber = i + 1;
                    Customer customer = customerService.findByEmail(dto.getCustomerEmail());
                    if (customer == null) {
                        errors.add("Fichier 1, Ligne " + lineNumber + " : Client introuvable pour l'email : " + dto.getCustomerEmail());
                        continue;
                    }
                    Expense expense = null;
                    if (dto.getExpense() != null) {
                        double amount = dto.getExpense();
                        if (amount < 0) {
                            errors.add("Fichier 1, Ligne " + lineNumber + " : Montant négatif non autorisé : " + amount);
                            continue;
                        }
                        expense = new Expense();
                        expense.setAmount(amount);
                        expense.setExpenseDate(LocalDate.now());
                        expense = expenseService.save(expense);
                    }
                    if ("lead".equalsIgnoreCase(dto.getType())) {
                        Lead lead = new Lead();
                        lead.setName(dto.getSubjectOrName());
                        lead.setStatus(dto.getStatus());
                        lead.setCustomer(customer);
                        lead.setManager(userService.findById(52));
                        lead.setEmployee(userService.findById(53));
                        lead.setCreatedAt(LocalDateTime.now());
                        lead.setPhone(generateRandomPhone());
                        lead.setGoogleDrive(false);
                        if (expense != null) lead.setExpense(expense);
                        leadService.save(lead);
                        leadsImported++;
                    } else if ("ticket".equalsIgnoreCase(dto.getType())) {
                        Ticket ticket = new Ticket();
                        ticket.setSubject(dto.getSubjectOrName());
                        ticket.setStatus(dto.getStatus());
                        ticket.setCustomer(customer);
                        ticket.setManager(userService.findById(52));
                        ticket.setEmployee(userService.findById(53));
                        ticket.setCreatedAt(LocalDateTime.now());
                        ticket.setPriority(generateRandomPriority());
                        ticket.setDescription("Imported ticket");
                        if (expense != null) ticket.setExpense(expense);
                        ticketService.save(ticket);
                        ticketsImported++;
                    }
                }
            }

            // Traitement du fichier 3 (CustomerBudget)
            if (!file3.isEmpty()) {
                tempFile3 = Files.createTempFile("csv_upload_", file3.getOriginalFilename());
                Files.copy(file3.getInputStream(), tempFile3, StandardCopyOption.REPLACE_EXISTING);
                List<JavaImportDataFeuille4Dto> importedData3 = csvService.importCsv(tempFile3.toString(), JavaImportDataFeuille4Dto.class);
                for (int i = 0; i < importedData3.size(); i++) {
                    JavaImportDataFeuille4Dto dto = importedData3.get(i);
                    int lineNumber = i + 1;
                    Customer customer = customerService.findByEmail(dto.getCustomerEmail());
                    if (customer == null) {
                        errors.add("Fichier 3, Ligne " + lineNumber + " : Client introuvable pour l'email : " + dto.getCustomerEmail());
                        continue;
                    }
                    if (dto.getBudget() != null && dto.getBudget() < 0) {
                        errors.add("Fichier 3, Ligne " + lineNumber + " : Budget négatif non autorisé : " + dto.getBudget());
                        continue;
                    }
                    CustomerBudget budget = new CustomerBudget();
                    budget.setCustomer(customer);
                    budget.setLabel("Budget importé");
                    budget.setAmount(BigDecimal.valueOf(dto.getBudget()));
                    budget.setTransactionDate(LocalDate.now());
                    budget.setCreatedAt(LocalDateTime.now());
                    budget.setUpdatedAt(LocalDateTime.now());
                    budget.setUser(userService.findById(52));
                    customerBudgetService.save(budget);
                    budgetsImported++;
                }
            }

            // Vérification des erreurs avant finalisation
            if (!errors.isEmpty()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                model.addAttribute("errors", errors);
                return "database/my-database";
            }

            // Réponse en cas de succès
            String message = "Importation réussie : " +
                    customersImported + " clients, " +
                    leadsImported + " leads, " +
                    ticketsImported + " tickets, " +
                    budgetsImported + " budgets ajoutés.";
            model.addAttribute("message", message);

        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            model.addAttribute("message", "Erreur lors de l'importation, aucune donnée enregistrée : " + e.getMessage());
        } finally {
            try {
                if (tempFile1 != null) Files.deleteIfExists(tempFile1);
                if (tempFile2 != null) Files.deleteIfExists(tempFile2);
                if (tempFile3 != null) Files.deleteIfExists(tempFile3);
            } catch (IOException ignored) { }
        }
        
        return "database/my-database";
    }

    // Helper methods for random data generation
    private String generateRandomPhone() {
        return "+33" + (int)(Math.random() * 900000000 + 100000000);
    }

    private String generateRandomPriority() {
        String[] priorities = {"low", "medium", "high", "urgent", "critical"};
        return priorities[(int)(Math.random() * priorities.length)];
    }

    private String generateRandomAddress() {
        return (int)(Math.random() * 1000) + " Random Street";
}

}
