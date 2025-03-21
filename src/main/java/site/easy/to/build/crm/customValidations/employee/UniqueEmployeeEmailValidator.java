package site.easy.to.build.crm.customValidations.employee;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import site.easy.to.build.crm.entity.Employee;
import site.easy.to.build.crm.service.employee.EmployeeService;

public class UniqueEmployeeEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final EmployeeService employeeService;

    @Autowired
    public UniqueEmployeeEmailValidator(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public UniqueEmployeeEmailValidator() {
        this.employeeService = null;
    }

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        if (employeeService == null || email == null || email.isEmpty()) {
            return true;
        }
        Employee employee = employeeService.findByEmail(email);
        return employee == null;
    }
}
