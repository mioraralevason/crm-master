package site.easy.to.build.crm.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import site.easy.to.build.crm.service.database.CsvService;
import site.easy.to.build.crm.service.database.DatabaseService;
import site.easy.to.build.crm.service.employee.EmployeeService;
import site.easy.to.build.crm.dto.EmployeeDto;
import site.easy.to.build.crm.entity.Employee;

@Controller
public class DatabaseController {

    @Autowired
    private DatabaseService databaseService;
    
    @Autowired
    private CsvService csvService;
    
    @Autowired
    private EmployeeService employeeService;

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
    
    @PostMapping("/database/import")
    public String importEspace(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Erreur : Aucun fichier sélectionné.");
            return "database/my-database";
        }

        try {
            Path tempFile = Files.createTempFile("csv_upload_", file.getOriginalFilename());
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            List<EmployeeDto> importedData = csvService.importCsv(tempFile.toString(), EmployeeDto.class);
            List<Employee> savedEmployees = new ArrayList<>();

            for (EmployeeDto employeeDto : importedData) {
                Employee newEmployee = new Employee();
                newEmployee.setEmail(employeeDto.getEmail());
                newEmployee.setEmployeeId(employeeDto.getId());
                newEmployee.setFirstName(employeeDto.getFirstName());
                newEmployee.setLastName(employeeDto.getLastName());
                newEmployee.setPassword(employeeDto.getPassword());
                newEmployee.setProvider(employeeDto.getProvider());
                newEmployee.setUsername(employeeDto.getUsername());
                savedEmployees.add(employeeService.save(newEmployee));
                
            }

            Files.deleteIfExists(tempFile);

            model.addAttribute("message", "Importation réussie : " + savedEmployees.size() + " employés ajoutés.");

        } catch (Exception e) {
            model.addAttribute("message", "Erreur lors de l'importation : " + e.getMessage());
        }
        return "database/my-database";
    }
}
