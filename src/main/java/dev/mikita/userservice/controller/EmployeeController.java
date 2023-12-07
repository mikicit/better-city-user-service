package dev.mikita.userservice.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import dev.mikita.userservice.annotation.FirebaseAuthorization;
import dev.mikita.userservice.dto.request.common.CreateEmployeeRequestDto;
import dev.mikita.userservice.dto.request.common.UpdateEmployeeRequestDto;
import dev.mikita.userservice.dto.response.common.EmployeeResponseDto;
import dev.mikita.userservice.entity.Employee;
import dev.mikita.userservice.service.EmployeeService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping(path = "/{uid}", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE"}, statuses = {"ACTIVE"})
    public ResponseEntity<EmployeeResponseDto> getEmployee(
            @PathVariable String uid,
            HttpServletRequest request)
            throws FirebaseAuthException, ExecutionException, InterruptedException, AuthException {
        Employee employee = employeeService.getEmployee(uid);

        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        if (!employee.getServiceUid().equals(token.getUid())) {
            throw new AuthException("Unauthorized");
        }

        return ResponseEntity.ok(new ModelMapper().map(employee, EmployeeResponseDto.class));
    }

    @PostMapping(path = "", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @FirebaseAuthorization(roles = {"SERVICE"}, statuses = {"ACTIVE"})
    public void createEmployee(@Valid @RequestBody CreateEmployeeRequestDto requestDto,
                                 HttpServletRequest request)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        ModelMapper modelMapper = new ModelMapper();
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        employeeService.createEmployee(token.getUid(), modelMapper.map(requestDto, Employee.class));
    }

    @PatchMapping(path = "/{uid}", consumes = "application/json", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE"}, statuses = {"ACTIVE"})
    public ResponseEntity<EmployeeResponseDto> updateEmployee(
            @PathVariable String uid,
            @Valid @RequestBody UpdateEmployeeRequestDto requestDto,
            HttpServletRequest request)
            throws FirebaseAuthException, ExecutionException, InterruptedException, AuthException {
        Employee employee = employeeService.getEmployee(uid);

        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        if (!employee.getServiceUid().equals(token.getUid())) {
            throw new AuthException("Unauthorized");
        }

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.map(requestDto, employee);

        Employee updatedEmployee = employeeService.updateEmployee(employee);

        return ResponseEntity.ok(modelMapper.map(updatedEmployee, EmployeeResponseDto.class));
    }

    @DeleteMapping("/{uid}")
    @FirebaseAuthorization(roles = {"SERVICE"}, statuses = {"ACTIVE"})
    public void deleteEmployee(@PathVariable String uid, HttpServletRequest request)
            throws AuthException, FirebaseAuthException, ExecutionException, InterruptedException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        if (employeeService.isEmployeeInService(uid, token.getUid())) {
            throw new AuthException("Unauthorized");
        }

        employeeService.deleteEmployee(uid);
    }

    @GetMapping(path = "/me", produces = "application/json")
    @FirebaseAuthorization(roles = {"EMPLOYEE"}, statuses = {"ACTIVE"})
    public ResponseEntity<EmployeeResponseDto> getCurrentEmployee(HttpServletRequest request)
            throws ExecutionException, FirebaseAuthException, InterruptedException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        return ResponseEntity.ok(
                new ModelMapper().map(employeeService.getEmployee(token.getUid()), EmployeeResponseDto.class));
    }
}
