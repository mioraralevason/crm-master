package site.easy.to.build.crm.service.employee;

import org.springframework.stereotype.Service;
import site.easy.to.build.crm.repository.EmployeeRepository;
import site.easy.to.build.crm.entity.Employee;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Employee findByEmployeeId(int employeeId) {
        return employeeRepository.findByEmployeeId(employeeId);
    }

    @Override
    public Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    @Override
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Override
    public void delete(Employee employee) {
        employeeRepository.delete(employee);
    }
}
