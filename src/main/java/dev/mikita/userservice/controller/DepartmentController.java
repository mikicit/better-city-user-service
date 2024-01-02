package dev.mikita.userservice.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import dev.mikita.userservice.annotation.FirebaseAuthorization;
import dev.mikita.userservice.dto.request.common.CreateDepartmentRequestDto;
import dev.mikita.userservice.dto.request.common.UpdateDepartmentRequestDto;
import dev.mikita.userservice.dto.response.common.DepartmentResponseDto;
import dev.mikita.userservice.dto.response.common.EmployeeResponseDto;
import dev.mikita.userservice.entity.Department;
import dev.mikita.userservice.entity.Employee;
import dev.mikita.userservice.service.DepartmentService;
import dev.mikita.userservice.service.EmployeeService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

    @Autowired
    public DepartmentController(DepartmentService departmentService,
                                EmployeeService employeeService) {
        this.departmentService = departmentService;
        this.employeeService = employeeService;
    }

    @GetMapping(path = "/{uid}", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE", "EMPLOYEE"}, statuses = {"ACTIVE"})
    public ResponseEntity<DepartmentResponseDto> getDepartment(@PathVariable String uid, HttpServletRequest request)
            throws AuthException, ExecutionException, FirebaseAuthException, InterruptedException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        Department department = departmentService.getDepartment(uid);

        switch (token.getClaims().get("role").toString()) {
            case "ROLE_SERVICE" -> {
                if (!token.getUid().equals(department.getServiceUid())) {
                    throw new AuthException("Unauthorized");
                }
            }
            case "ROLE_EMPLOYEE" -> {
                if (!employeeService.isEmployeeInDepartment(token.getUid(), uid)) {
                    throw new AuthException("Unauthorized");
                }
            }
        }

        ModelMapper modelMapper = new ModelMapper();
        DepartmentResponseDto departmentResponseDto = modelMapper.map(department, DepartmentResponseDto.class);

        return ResponseEntity.ok(departmentResponseDto);
    }

    @PostMapping(path = "", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @FirebaseAuthorization(roles = {"SERVICE"}, statuses = {"ACTIVE"})
    public void createDepartment(@Valid @RequestBody CreateDepartmentRequestDto requestDto,
                                 HttpServletRequest request) throws ExecutionException, InterruptedException {
        ModelMapper modelMapper = new ModelMapper();

        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        Department department = modelMapper.map(requestDto, Department.class);

        departmentService.createDepartment(token.getUid(), department);
    }

    @PatchMapping(path = "/{uid}", consumes = "application/json", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE"}, statuses = {"ACTIVE"})
    public ResponseEntity<DepartmentResponseDto> updateDepartment(
            @PathVariable String uid,
            @Valid @RequestBody UpdateDepartmentRequestDto requestDto,
            HttpServletRequest request)
            throws ExecutionException, InterruptedException, FirebaseAuthException, AuthException {

        ModelMapper modelMapper = new ModelMapper();
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        if (!departmentService.isServiceOwnerOfDepartment(token.getUid(), uid)) {
            throw new AuthException("Unauthorized");
        }

        Department department = modelMapper.map(requestDto, Department.class);
        department.setUid(uid);

        Department updatedDepartment = departmentService.updateDepartment(department);

        return ResponseEntity.ok(modelMapper.map(updatedDepartment, DepartmentResponseDto.class));
    }

    @DeleteMapping("/{uid}")
    @FirebaseAuthorization(roles = {"SERVICE"}, statuses = {"ACTIVE"})
    public void deleteDepartment(@PathVariable String uid, HttpServletRequest request)
            throws AuthException, FirebaseAuthException, ExecutionException, InterruptedException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        Department department = departmentService.getDepartment(uid);

        if (!token.getUid().equals(department.getServiceUid())) {
            throw new AuthException("Unauthorized");
        }

        departmentService.deleteDepartment(uid);
    }

    @GetMapping(path = "/{uid}/employees", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE", "EMPLOYEE"}, statuses = {"ACTIVE"})
    public ResponseEntity<List<EmployeeResponseDto>> getDepartmentEmployees(
            @PathVariable String uid, HttpServletRequest request)
            throws AuthException, ExecutionException, FirebaseAuthException, InterruptedException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        Department department = departmentService.getDepartment(uid);

        if (!token.getUid().equals(department.getServiceUid()) && !employeeService.isEmployeeInDepartment(token.getUid(), uid)) {
            throw new AuthException("Unauthorized");
        }

        List<Employee> employees = employeeService.getEmployeesByDepartmentUid(uid);

        return ResponseEntity.ok(new ModelMapper().map(
                employees, new ParameterizedTypeReference<List<EmployeeResponseDto>>() {}.getType()));
    }
}
