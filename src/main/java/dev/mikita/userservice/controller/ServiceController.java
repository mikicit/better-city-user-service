package dev.mikita.userservice.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import dev.mikita.userservice.annotation.FirebaseAuthorization;
import dev.mikita.userservice.dto.request.CreateServiceRequestDto;
import dev.mikita.userservice.dto.response.CountResponseDto;
import dev.mikita.userservice.dto.response.ServicePublicResponseDto;
import dev.mikita.userservice.entity.Service;
import dev.mikita.userservice.service.ServiceService;
import dev.mikita.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * The type Service controller.
 */
@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {
    private final RestTemplate restTemplate;
    private final ServiceService serviceService;
    private final UserService userService;

    /**
     * Instantiates a new Service controller.
     *
     * @param serviceService the service
     * @param restTemplate   the rest template
     * @param userService    the user service
     */
    @Autowired
    public ServiceController(ServiceService serviceService,
                             RestTemplate restTemplate,
                             UserService userService
    ) {
        this.serviceService = serviceService;
        this.restTemplate = restTemplate;
        this.userService = userService;
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
    @GetMapping("/{uid}")
    @FirebaseAuthorization
    public ResponseEntity<ServicePublicResponseDto> getService(@PathVariable String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        Service service = serviceService.getService(uid);

        ModelMapper modelMapper = new ModelMapper();
        ServicePublicResponseDto responsePublicServiceDto = modelMapper.map(service, ServicePublicResponseDto.class);

        return ResponseEntity.ok(responsePublicServiceDto);
    }

    /**
     * Create service.
     *
     * @param request the request
     * @throws FirebaseAuthException the firebase auth exception
     */
    @PostMapping(path = "", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @FirebaseAuthorization(roles = {"ROLE_MODERATOR"})
    public void createService(@Valid @RequestBody CreateServiceRequestDto request) throws FirebaseAuthException {
        ModelMapper modelMapper = new ModelMapper();
        Service service = modelMapper.map(request, Service.class);

        serviceService.createService(service);
    }

    /**
     * Update service response entity.
     *
     * @param request the request
     * @param uid     the uid
     * @return the response entity
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    @PutMapping(path = "/{uid}", consumes = "application/json", produces = "application/json")
    @FirebaseAuthorization(roles = {"ROLE_MODERATOR"})
    public ResponseEntity<Service> updateService(@Valid @RequestBody Service request,
                                                   @PathVariable String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {

        Service service = serviceService.getService(uid);

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.map(request, service);

        return ResponseEntity.ok(serviceService.updateService(service));
    }

    /**
     * Delete service.
     *
     * @param uid the uid
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    @DeleteMapping("/{uid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @FirebaseAuthorization(roles = {"ROLE_MODERATOR"})
    public void deleteService(@PathVariable String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        serviceService.deleteService(uid);
    }

    /**
     * Gets service reservations count.
     *
     * @param uid     the uid
     * @param request the request
     * @return the service reservations count
     */
    @GetMapping("/{uid}/reservations/count")
    @FirebaseAuthorization
    public ResponseEntity<CountResponseDto> getServiceReservationsCount(
            @PathVariable String uid, HttpServletRequest request
    ) {
        // Headers
        String authorizationHeader = request.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationHeader);
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        // Query
        String uri = "http://issue-service/api/v1/issues/reservations/count?serviceId=" + uid;
        ResponseEntity<CountResponseDto> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<>() {}
        );

        return ResponseEntity.ok(response.getBody());
    }

    /**
     * Gets service solutions count.
     *
     * @param uid     the uid
     * @param request the request
     * @return the service solutions count
     */
    @GetMapping("/{uid}/solutions/count")
    @FirebaseAuthorization
    public ResponseEntity<CountResponseDto> getServiceSolutionsCount(
            @PathVariable String uid, HttpServletRequest request) {

        // Headers
        String authorizationHeader = request.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationHeader);
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        // Query
        String uri = "http://issue-service/api/v1/issues/solutions/count?serviceId=" + uid;
        ResponseEntity<CountResponseDto> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<>() {}
        );

        return ResponseEntity.ok(response.getBody());
    }

    /**
     * Update current service photo.
     *
     * @param data    the data
     * @param request the request
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     * @throws IOException           the io exception
     */
    @PutMapping("/me/photo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @FirebaseAuthorization(roles = {"ROLE_SERVICE"})
    public void updateCurrentServicePhoto(MultipartHttpServletRequest data,
                                           HttpServletRequest request)
            throws FirebaseAuthException, ExecutionException, InterruptedException, IOException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        MultipartFile photoFile = data.getFile("photo");

        if (photoFile == null) {
            throw new BadRequestException("Photo is required");
        }

        userService.updateUserPhoto(token.getUid(), photoFile);
    }
}
