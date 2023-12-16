package dev.mikita.userservice.controller.admin;

import com.google.cloud.firestore.Query;
import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.annotation.FirebaseAuthorization;
import dev.mikita.userservice.dto.request.moderator.CreateServiceModeratorRequestDto;
import dev.mikita.userservice.dto.request.moderator.UpdateUserStatusModeratorRequestDto;
import dev.mikita.userservice.dto.response.moderator.ServiceModeratorResponseDto;
import dev.mikita.userservice.entity.Service;
import dev.mikita.userservice.entity.UserStatus;
import dev.mikita.userservice.service.ServiceService;
import dev.mikita.userservice.util.Pageable;
import dev.mikita.userservice.util.PagedResult;
import jakarta.validation.Valid;
import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/services")
public class AdminServiceController {
    private final ServiceService serviceService;

    @Getter
    public enum OrderBy {
        CREATION_DATE("creationDate"),
        STATUS("status"),
        NAME("name"),
        ADDRESS("address");
        private final String fieldName;

        OrderBy(String fieldName) {
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

    @Autowired
    public AdminServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping("/{uid}")
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public ResponseEntity<ServiceModeratorResponseDto> getService(@PathVariable String uid)
            throws FirebaseAuthException, ExecutionException, InterruptedException {
        return ResponseEntity.ok(new ModelMapper().map(serviceService.getService(uid), ServiceModeratorResponseDto.class));
    }

    @GetMapping
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public ResponseEntity<Map<String, Object>> getServices(
            @RequestParam(required = false) List<UserStatus> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "order_by", required = false) OrderBy orderBy,
            @RequestParam(required = false) Order order) {
        // Pagination and sorting
        if (orderBy == null) orderBy = OrderBy.CREATION_DATE;
        if (order == null) order = Order.DESC;

        Pageable pageable = new Pageable(page, size, orderBy.getFieldName(), Query.Direction.valueOf(order.getFieldName()));
        PagedResult<Service> pageServices = serviceService.getServices(statuses, pageable);
        List<Service> services = pageServices.items();

        // Collect result
        ModelMapper modelMapper = new ModelMapper();

        Map<String, Object> response = new HashMap<>();
        response.put("services", services.stream()
                .map(service -> modelMapper.map(service, ServiceModeratorResponseDto.class))
                .collect(Collectors.toList()));
        response.put("currentPage", pageServices.currentPage());
        response.put("totalItems", pageServices.totalItems());
        response.put("totalPages", pageServices.totalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * Create service.
     *
     * @param request the request
     * @throws FirebaseAuthException the firebase auth exception
     */
    @PostMapping(path = "", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public void createService(@Valid @RequestBody CreateServiceModeratorRequestDto request) throws FirebaseAuthException {
        ModelMapper modelMapper = new ModelMapper();
        Service service = modelMapper.map(request, Service.class);

        serviceService.createService(service);
    }

    @PatchMapping(path = "/{uid}", consumes = "application/json")
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public ResponseEntity<HttpStatus> updateServiceStatus(@PathVariable String uid,
                                                           @RequestBody UpdateUserStatusModeratorRequestDto status)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        serviceService.updateServiceStatus(uid, status.getStatus());
        return ResponseEntity.noContent().build();
    }
}
