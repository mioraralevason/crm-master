package site.easy.to.build.crm.service.employee;

import site.easy.to.build.crm.entity.Employee;
import java.util.List;

public interface EmployeeService {

    Employee findByEmployeeId(int employeeId);

    Employee findByEmail(String email);

    List<Employee> findAll();

    Employee save(Employee employee);

    void delete(Employee employee);


}
