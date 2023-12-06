package dev.mikita.userservice.controller.admin;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.annotation.FirebaseAuthorization;
import dev.mikita.userservice.dto.request.moderator.CreateServiceModeratorRequestDto;
import dev.mikita.userservice.dto.request.moderator.UpdateUserStatusModeratorRequestDto;
import dev.mikita.userservice.dto.response.moderator.ServiceModeratorResponseDto;
import dev.mikita.userservice.entity.Service;
import dev.mikita.userservice.service.ServiceService;
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
@RequestMapping("/api/v1/admin/services")
public class AdminServiceController {
    private final ServiceService serviceService;

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
    public ResponseEntity<List<ServiceModeratorResponseDto>> getServices() {
        return ResponseEntity.ok(new ModelMapper().map(
                serviceService.getServices(), new ParameterizedTypeReference<List<ServiceModeratorResponseDto>>() {}.getType()));
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
