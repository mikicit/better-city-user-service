package dev.mikita.userservice.controller;

import com.google.cloud.firestore.Query;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import dev.mikita.userservice.annotation.FirebaseAuthorization;
import dev.mikita.userservice.dto.request.common.UpdateServiceRequestDto;
import dev.mikita.userservice.dto.response.common.CountResponseDto;
import dev.mikita.userservice.dto.response.common.DepartmentResponseDto;
import dev.mikita.userservice.dto.response.common.EmployeeResponseDto;
import dev.mikita.userservice.dto.response.common.ServiceResponseDto;
import dev.mikita.userservice.entity.Department;
import dev.mikita.userservice.entity.Employee;
import dev.mikita.userservice.entity.Service;
import dev.mikita.userservice.entity.UserStatus;
import dev.mikita.userservice.exception.NotFoundException;
import dev.mikita.userservice.service.DepartmentService;
import dev.mikita.userservice.service.EmployeeService;
import dev.mikita.userservice.service.ServiceService;
import dev.mikita.userservice.service.UserService;
import dev.mikita.userservice.util.Pageable;
import dev.mikita.userservice.util.PagedResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

    @Getter
    public enum ServiceOrderBy {
        CREATION_DATE("creationDate"),
        NAME("name"),
        ADDRESS("address");
        private final String fieldName;

        ServiceOrderBy(String fieldName) {
            this.fieldName = fieldName;
        }
    }

    @Getter
    public enum DepartmentOrderBy {
        SERVICE_UID("serviceUid");
        private final String fieldName;

        DepartmentOrderBy(String fieldName) {
            this.fieldName = fieldName;
        }
    }

    @Getter
    public enum EmployeeOrderBy {
        SERVICE_UID("serviceUid");
        private final String fieldName;

        EmployeeOrderBy(String fieldName) {
            this.fieldName = fieldName;
        }
    }

    @Getter
    public enum Order {
        ASC("ASCENDING"),
        DESC("DESCENDING");

        private final String fieldName;

        Order(String fieldName) {
            this.fieldName = fieldName;
        }
    }

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

        if (service.getStatus() == UserStatus.DELETED || service.getStatus() == UserStatus.BANNED) {
            throw new NotFoundException("Resident not found");
        }

        ModelMapper modelMapper = new ModelMapper();
        ServiceResponseDto responsePublicServiceDto = modelMapper.map(service, ServiceResponseDto.class);

        return ResponseEntity.ok(responsePublicServiceDto);
    }

    @GetMapping(path = "", produces = "application/json")
    @FirebaseAuthorization(roles = {"ANALYST"}, statuses = {"ACTIVE"})
    public ResponseEntity<Map<String, Object>> getServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "order_by", required = false) ServiceOrderBy orderBy,
            @RequestParam(required = false) Order order) {
        // Pagination and sorting
        if (orderBy == null) orderBy = ServiceOrderBy.CREATION_DATE;
        if (order == null) order = Order.DESC;

        Pageable pageable = new Pageable(page, size, orderBy.getFieldName(), Query.Direction.valueOf(order.getFieldName()));
        PagedResult<Service> pageServices = serviceService.getServices(null, pageable);
        List<Service> services = pageServices.items();

        // Collect result
        ModelMapper modelMapper = new ModelMapper();

        Map<String, Object> response = new HashMap<>();
        response.put("services", services.stream()
                .map(service -> modelMapper.map(service, ServiceResponseDto.class))
                .collect(Collectors.toList()));
        response.put("currentPage", pageServices.currentPage());
        response.put("totalItems", pageServices.totalItems());
        response.put("totalPages", pageServices.totalPages());

        return ResponseEntity.ok(response);
    }

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
    public ResponseEntity<Map<String, Object>> getDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request)
            throws ExecutionException, InterruptedException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        // Pagination
        Pageable pageable = new Pageable(page, size, DepartmentOrderBy.SERVICE_UID.getFieldName(), Query.Direction.valueOf(Order.DESC.getFieldName()));
        PagedResult<Department> pageDepartments = departmentService.getDepartmentsByServiceUid(token.getUid(), pageable);
        List<Department> departments = pageDepartments.items();

        // Collect result
        ModelMapper modelMapper = new ModelMapper();

        Map<String, Object> response = new HashMap<>();
        response.put("departments", departments.stream()
                .map(service -> modelMapper.map(service, DepartmentResponseDto.class))
                .collect(Collectors.toList()));
        response.put("currentPage", pageDepartments.currentPage());
        response.put("totalItems", pageDepartments.totalItems());
        response.put("totalPages", pageDepartments.totalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/me/employees", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE"}, statuses = {"ACTIVE"})
    public ResponseEntity<Map<String, Object>> getEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        // Pagination and sorting
        Pageable pageable = new Pageable(page, size, EmployeeOrderBy.SERVICE_UID.getFieldName(), Query.Direction.valueOf(Order.DESC.getFieldName()));
        PagedResult<Employee> pageEmployees = employeeService.getEmployeesByServiceUid(token.getUid(), pageable);
        List<Employee> employees = pageEmployees.items();

        // Collect result
        ModelMapper modelMapper = new ModelMapper();

        Map<String, Object> response = new HashMap<>();
        response.put("employees", employees.stream()
                .map(service -> modelMapper.map(service, EmployeeResponseDto.class))
                .collect(Collectors.toList()));
        response.put("currentPage", pageEmployees.currentPage());
        response.put("totalItems", pageEmployees.totalItems());
        response.put("totalPages", pageEmployees.totalPages());

        return ResponseEntity.ok(response);
    }
}
