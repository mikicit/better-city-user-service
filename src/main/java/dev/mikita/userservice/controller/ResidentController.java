package dev.mikita.userservice.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import dev.mikita.userservice.annotation.FirebaseAuthorization;
import dev.mikita.userservice.dto.request.CreateResidentRequestDto;
import dev.mikita.userservice.dto.response.IssuePublicResponseDto;
import dev.mikita.userservice.dto.response.CountResponseDto;
import dev.mikita.userservice.dto.response.ResidentPrivateResponseDto;
import dev.mikita.userservice.dto.response.ResidentPublicResponseDto;
import dev.mikita.userservice.entity.IssueStatus;
import dev.mikita.userservice.entity.Resident;
import dev.mikita.userservice.service.ResidentService;
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
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * The type Resident controller.
 */
@RestController
@RequestMapping("/api/v1/residents")
public class ResidentController {
    private final RestTemplate restTemplate;
    private final ResidentService residentService;
    private final UserService userService;

    /**
     * Instantiates a new Resident controller.
     *
     * @param residentService the resident service
     * @param userService     the user service
     * @param restTemplate    the rest template
     */
    @Autowired
    public ResidentController(ResidentService residentService,
                                UserService userService,
                                RestTemplate restTemplate) {
        this.residentService = residentService;
        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    /**
     * Gets resident.
     *
     * @param uid     the uid
     * @param request the request
     * @return the resident
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    @GetMapping("/{uid}")
    @FirebaseAuthorization
    public ResponseEntity<ResidentPublicResponseDto> getResident(
            @PathVariable String uid,
            HttpServletRequest request)
            throws FirebaseAuthException, ExecutionException, InterruptedException {
        Resident resident = residentService.getResident(uid);

        ModelMapper modelMapper = new ModelMapper();
        ResidentPublicResponseDto responsePublicResidentDto = modelMapper.map(resident, ResidentPublicResponseDto.class);

        return ResponseEntity.ok(responsePublicResidentDto);
    }

    /**
     * Gets issues count.
     *
     * @param uid     the uid
     * @param request the request
     * @return the issues count
     */
    @GetMapping("/{uid}/issues/count")
    @FirebaseAuthorization
    public ResponseEntity<CountResponseDto> getIssuesCount(
            @PathVariable String uid,
            HttpServletRequest request) {

        // Headers
        String authorizationHeader = request.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationHeader);
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        // Query
        String uri = "http://issue-service-service.default.svc.cluster.local:8080/api/v1/issues/count?authorId=" + uid;
        ResponseEntity<CountResponseDto> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<>() {}
        );

        return ResponseEntity.ok(response.getBody());
    }

    /**
     * Create resident.
     *
     * @param request the request
     * @throws FirebaseAuthException the firebase auth exception
     */
    @PostMapping(path = "", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void createResident(@Valid @RequestBody CreateResidentRequestDto request) throws FirebaseAuthException {
        ModelMapper modelMapper = new ModelMapper();
        Resident resident = modelMapper.map(request, Resident.class);

        residentService.createResident(resident);
    }

    /**
     * Update resident response entity.
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
    public ResponseEntity<Resident> updateResident(@Valid @RequestBody Resident request,
                                                   @PathVariable String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {

        Resident resident = residentService.getResident(uid);

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.map(request, resident);

        return ResponseEntity.ok(residentService.updateResident(resident));
    }

    /**
     * Delete resident.
     *
     * @param uid the uid
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    @DeleteMapping("/{uid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @FirebaseAuthorization(roles = {"ROLE_MODERATOR"})
    public void deleteResident(@PathVariable String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        residentService.deleteResident(uid);
    }

    /**
     * Gets current resident.
     *
     * @param request the request
     * @return the current resident
     * @throws ExecutionException    the execution exception
     * @throws FirebaseAuthException the firebase auth exception
     * @throws InterruptedException  the interrupted exception
     */
    @GetMapping("/me")
    @FirebaseAuthorization(roles = {"ROLE_RESIDENT"})
    public ResponseEntity<ResidentPrivateResponseDto> getCurrentResident(HttpServletRequest request) throws ExecutionException, FirebaseAuthException, InterruptedException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        Resident resident = residentService.getResident(token.getUid());

        ModelMapper modelMapper = new ModelMapper();
        ResidentPrivateResponseDto responsePublicResidentDto = modelMapper.map(resident, ResidentPrivateResponseDto.class);

        return ResponseEntity.ok(responsePublicResidentDto);
    }

    /**
     * Gets current resident issues.
     *
     * @param status  the status
     * @param request the request
     * @return the current resident issues
     */
    @GetMapping("/me/issues")
    @FirebaseAuthorization(roles = {"ROLE_RESIDENT"})
    public ResponseEntity<List<IssuePublicResponseDto>> getCurrentResidentIssues(
            @RequestParam(required = false) IssueStatus status,
            HttpServletRequest request
    ) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        // Headers
        String authorizationHeader = request.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationHeader);

        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        // Query
        String uri = "http://issue-service-service.default.svc.cluster.local:8080/api/v1/issues?authorId=" + token.getUid();
        if (status != null) {
            uri += "&status=" + status;
        }

        ResponseEntity<List<IssuePublicResponseDto>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<>() {}
        );

        return ResponseEntity.ok(response.getBody());
    }

    /**
     * Gets current resident issues count.
     *
     * @param request the request
     * @return the current resident issues count
     */
    @GetMapping("/me/issues/count")
    @FirebaseAuthorization(roles = {"ROLE_RESIDENT"})
    public ResponseEntity<CountResponseDto> getCurrentResidentIssuesCount(HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        // Headers
        String authorizationHeader = request.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationHeader);
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        // Query
        String uri = "http://issue-service-service.default.svc.cluster.local:8080/api/v1/issues/count?authorId=" + token.getUid();
        ResponseEntity<CountResponseDto> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<>() {}
        );

        return ResponseEntity.ok(response.getBody());
    }

    /**
     * Update current resident photo.
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
    @FirebaseAuthorization(roles = {"ROLE_RESIDENT"})
    public void updateCurrentResidentPhoto(MultipartHttpServletRequest data,
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
