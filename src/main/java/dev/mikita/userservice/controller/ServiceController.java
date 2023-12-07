package dev.mikita.userservice.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import dev.mikita.userservice.annotation.FirebaseAuthorization;
import dev.mikita.userservice.dto.request.common.UpdateServiceRequestDto;
import dev.mikita.userservice.dto.response.common.CountResponseDto;
import dev.mikita.userservice.dto.response.common.DepartmentResponseDto;
import dev.mikita.userservice.dto.response.common.ServiceResponseDto;
import dev.mikita.userservice.dto.response.common.EmployeeResponseDto;
import dev.mikita.userservice.entity.Department;
import dev.mikita.userservice.entity.Employee;
import dev.mikita.userservice.entity.Service;
import dev.mikita.userservice.service.DepartmentService;
import dev.mikita.userservice.service.EmployeeService;
import dev.mikita.userservice.service.ServiceService;
import dev.mikita.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * The type Service controller.
 */
@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {
    private final ServiceService serviceService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

    /**
     * Instantiates a new Service controller.
     *
     * @param serviceService the service
     * @param userService    the user service
     */
    @Autowired
    public ServiceController(ServiceService serviceService,
                             UserService userService,
                             DepartmentService departmentService,
                             EmployeeService employeeService
    ) {
        this.serviceService = serviceService;
        this.userService = userService;
        this.departmentService = departmentService;
        this.employeeService = employeeService;
    }

    /**
     * Gets service.
     *
     * @param uid the uid
     * @return the service
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    @GetMapping(path = "/{uid}", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<ServiceResponseDto> getService(@PathVariable String uid)
            throws FirebaseAuthException, ExecutionException, InterruptedException {
        Service service = serviceService.getService(uid);

        ModelMapper modelMapper = new ModelMapper();
        ServiceResponseDto responsePublicServiceDto = modelMapper.map(service, ServiceResponseDto.class);

        return ResponseEntity.ok(responsePublicServiceDto);
    }

    @GetMapping(path = "", produces = "application/json")
    @FirebaseAuthorization(roles = {"ANALYST"}, statuses = {"ACTIVE"})
    public ResponseEntity<List<ServiceResponseDto>> getServices() {
        return ResponseEntity.ok(new ModelMapper().map(
                serviceService.getServices(), new ParameterizedTypeReference<List<ServiceResponseDto>>() {}.getType()));
    }

    // TODO: 07.12.2023 add filtration by status, return only active services
    @GetMapping(path = "/count", produces = "application/json")
    @FirebaseAuthorization(roles = {"ANALYST"}, statuses = {"ACTIVE"})
    public ResponseEntity<CountResponseDto> getServicesCount() {
        Long count = serviceService.getServicesCount();
        CountResponseDto responseDto = new CountResponseDto();
        responseDto.setCount(count);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping(path = "/me", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE"}, statuses = {"ACTIVE"})
    public ResponseEntity<ServiceResponseDto> getCurrentService(
            HttpServletRequest request)
            throws FirebaseAuthException, ExecutionException, InterruptedException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        Service service = serviceService.getService(token.getUid());

        return ResponseEntity.ok(new ModelMapper().map(service, ServiceResponseDto.class));
    }

    @PatchMapping(path = "/me", produces = "application/json", consumes = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE"}, statuses = {"ACTIVE"})
    public ResponseEntity<ServiceResponseDto> updateCurrentService(
            @Valid @RequestBody UpdateServiceRequestDto requestDto,
            HttpServletRequest request)
            throws ExecutionException, FirebaseAuthException, InterruptedException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        ModelMapper modelMapper = new ModelMapper();
        Service service = modelMapper.map(requestDto, Service.class);
        service.setUid(token.getUid());

        Service updatedService = serviceService.updateService(service);

        return ResponseEntity.ok(new ModelMapper().map(updatedService, ServiceResponseDto.class));
    }

    /**
     * Update current service photo.
     *
     * @param data    the data
     * @param request the request
     * @throws FirebaseAuthException the firebase auth exception
     * @throws IOException           the io exception
     */
    @PutMapping("/me/photo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @FirebaseAuthorization(roles = {"SERVICE"}, statuses = {"ACTIVE"})
    public void updateCurrentServicePhoto(MultipartHttpServletRequest data,
                                           HttpServletRequest request)
            throws FirebaseAuthException, IOException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        MultipartFile photoFile = data.getFile("photo");

        if (photoFile == null) {
            throw new BadRequestException("Photo is required");
        }

        userService.updateUserPhoto(token.getUid(), photoFile);
    }

    @GetMapping(path = "/me/departments", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE"}, statuses = {"ACTIVE"})
    public ResponseEntity<List<DepartmentResponseDto>> getDepartments(HttpServletRequest request)
            throws ExecutionException, InterruptedException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        List<Department> departments = departmentService.getDepartmentsByServiceUid(token.getUid());
        return ResponseEntity.ok(new ModelMapper().map(
                departments, new ParameterizedTypeReference<List<DepartmentResponseDto>>() {}.getType()));
    }

    @GetMapping(path = "/me/employees", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE"}, statuses = {"ACTIVE"})
    public ResponseEntity<List<EmployeeResponseDto>> getEmployees(HttpServletRequest request)
            throws ExecutionException, InterruptedException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        List<Employee> employees = employeeService.getEmployeesByServiceUid(token.getUid());
        return ResponseEntity.ok(new ModelMapper().map(
                employees, new ParameterizedTypeReference<List<EmployeeResponseDto>>() {}.getType()));
    }
}
