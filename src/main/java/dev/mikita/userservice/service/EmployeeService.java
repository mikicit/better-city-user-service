package dev.mikita.userservice.service;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.entity.Department;
import dev.mikita.userservice.entity.Employee;
import dev.mikita.userservice.exception.NotFoundException;
import dev.mikita.userservice.repository.DepartmentRepository;
import dev.mikita.userservice.repository.EmployeeRepository;
import dev.mikita.userservice.util.Pageable;
import dev.mikita.userservice.util.PagedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    public Employee getEmployee(String uid)
            throws FirebaseAuthException, ExecutionException, InterruptedException {
        return employeeRepository.find(uid);
    }

    public PagedResult<Employee> getEmployeesByServiceUid(String serviceUid, Pageable pageable) {
        return employeeRepository.findAllByServiceUid(serviceUid, pageable);
    }

    public List<Employee> getEmployeesByDepartmentUid(String departmentUid)
            throws ExecutionException, InterruptedException {
        return employeeRepository.findAllByDepartmentUid(departmentUid);
    }

    public void createEmployee(String serviceUid, Employee employee)
            throws FirebaseAuthException, ExecutionException, InterruptedException {
        employee.setServiceUid(serviceUid);
        Department department = departmentRepository.find(employee.getDepartmentUid());

        if (!department.getServiceUid().equals(serviceUid)) {
            throw new NotFoundException("Department not found");
        }

        employeeRepository.persist(employee);
    }

    public Employee updateEmployee(Employee employee)
            throws FirebaseAuthException, ExecutionException, InterruptedException {
        return employeeRepository.update(employee);
    }

    public void deleteEmployee(String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        Employee employee = employeeRepository.find(uid);
        if (employeeRepository.find(employee.getUid()) == null) {
            throw new NotFoundException("Employee not found");
        }

        employeeRepository.delete(uid);
    }

    public boolean isEmployeeInDepartment(String employeeUid, String departmentUid)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        Employee employee = employeeRepository.find(employeeUid);
        return employee.getDepartmentUid().equals(departmentUid);
    }

    public boolean isEmployeeInService(String employeeUid, String serviceUid)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        Employee employee = employeeRepository.find(employeeUid);
        return employee.getServiceUid().equals(serviceUid);
    }
}
