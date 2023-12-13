package dev.mikita.userservice.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import dev.mikita.userservice.annotation.FirebaseAuthorization;
import dev.mikita.userservice.dto.request.common.CreateResidentRequestDto;
import dev.mikita.userservice.dto.request.common.UpdateResidentRequestDto;
import dev.mikita.userservice.dto.response.resident.ResidentResidentResponseDto;
import dev.mikita.userservice.dto.response.common.ResidentResponseDto;
import dev.mikita.userservice.entity.Resident;
import dev.mikita.userservice.entity.UserStatus;
import dev.mikita.userservice.exception.NotFoundException;
import dev.mikita.userservice.service.ResidentService;
import dev.mikita.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * The type Resident controller.
 */
@RestController
@RequestMapping("/api/v1/residents")
public class ResidentController {
    private final ResidentService residentService;
    private final UserService userService;

    /**
     * Instantiates a new Resident controller.
     *
     * @param residentService the resident service
     * @param userService     the user service
     */
    @Autowired
    public ResidentController(ResidentService residentService,
                                UserService userService) {
        this.residentService = residentService;
        this.userService = userService;
    }

    /**
     * Gets resident.
     *
     * @param uid     the uid
     * @return the resident
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    @GetMapping(path = "/{uid}", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<ResidentResponseDto> getResident(
            @PathVariable String uid)
            throws FirebaseAuthException, ExecutionException, InterruptedException {
        Resident resident = residentService.getResident(uid);

        if (resident.getStatus() == UserStatus.DELETED || resident.getStatus() == UserStatus.BANNED) {
            throw new NotFoundException("Resident not found");
        }

        ModelMapper modelMapper = new ModelMapper();
        ResidentResponseDto responsePublicResidentDto = modelMapper.map(resident, ResidentResponseDto.class);

        return ResponseEntity.ok(responsePublicResidentDto);
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
     * Gets current resident.
     *
     * @param request the request
     * @return the current resident
     * @throws ExecutionException    the execution exception
     * @throws FirebaseAuthException the firebase auth exception
     * @throws InterruptedException  the interrupted exception
     */
    @GetMapping(path = "/me", produces = "application/json")
    @FirebaseAuthorization(roles = {"RESIDENT"}, statuses = {"ACTIVE"})
    public ResponseEntity<ResidentResidentResponseDto> getCurrentResident(HttpServletRequest request)
            throws ExecutionException, FirebaseAuthException, InterruptedException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        Resident resident = residentService.getResident(token.getUid());
        return ResponseEntity.ok(new ModelMapper().map(resident, ResidentResidentResponseDto.class));
    }

    @PatchMapping(path = "/me", consumes = "application/json", produces = "application/json")
    @FirebaseAuthorization(roles = {"RESIDENT"}, statuses = {"ACTIVE"})
    public ResponseEntity<ResidentResidentResponseDto> updateCurrentResident(
            @Valid @RequestBody UpdateResidentRequestDto requestDto,
            HttpServletRequest request)
            throws ExecutionException, FirebaseAuthException, InterruptedException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        ModelMapper modelMapper = new ModelMapper();
        Resident resident = modelMapper.map(requestDto, Resident.class);
        resident.setUid(token.getUid());

        Resident updatedResident = residentService.updateResident(resident);

        return ResponseEntity.ok(new ModelMapper().map(updatedResident, ResidentResidentResponseDto.class));
    }

    /**
     * Update current resident photo.
     *
     * @param data    the data
     * @param request the request
     * @throws FirebaseAuthException the firebase auth exception
     * @throws IOException           the io exception
     */
    @PutMapping("/me/photo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @FirebaseAuthorization(roles = {"RESIDENT"}, statuses = {"ACTIVE"})
    public void updateCurrentResidentPhoto(MultipartHttpServletRequest data,
                                           HttpServletRequest request)
            throws FirebaseAuthException, IOException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        MultipartFile photoFile = data.getFile("photo");

        if (photoFile == null) {
            throw new BadRequestException("Photo is required");
        }

        userService.updateUserPhoto(token.getUid(), photoFile);
    }
}
