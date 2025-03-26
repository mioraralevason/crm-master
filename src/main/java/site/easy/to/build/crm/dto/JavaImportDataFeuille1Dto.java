package site.easy.to.build.crm.dto;

import java.util.List;

public class JavaImportDataFeuille1Dto {

    private String customer_email;
    private String subject_or_name;
    private String type;
    private String status;
    private Double expense;

    public String getCustomerEmail() {
        return customer_email;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customer_email = customerEmail;
    }

    public String getSubjectOrName() {
        return subject_or_name;
    }

    public void setSubjectOrName(String subjectOrName) {
        this.subject_or_name = subjectOrName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getExpense() {
        return expense;
    }

    public void setExpense(Double expense) {
        this.expense = expense;
    }

}
