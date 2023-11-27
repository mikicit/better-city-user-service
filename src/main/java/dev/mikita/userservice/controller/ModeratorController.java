package dev.mikita.userservice.controller;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.annotation.FirebaseAuthorization;
import dev.mikita.userservice.dto.request.CreateModeratorRequestDto;
import dev.mikita.userservice.dto.response.ModeratorPublicResponseDto;
import dev.mikita.userservice.entity.Moderator;
import dev.mikita.userservice.service.ModeratorService;
import jakarta.validation.Valid;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.ExecutionException;

/**
 * The type Moderator controller.
 */
@RestController
@RequestMapping("/api/v1/moderators")
public class ModeratorController {
    private final ModeratorService moderatorService;

    /**
     * Instantiates a new Moderator controller.
     *
     * @param moderatorService the moderator service
     */
    @Autowired
    public ModeratorController(ModeratorService moderatorService) {
        this.moderatorService = moderatorService;
    }

    /**
     * Gets moderator.
     *
     * @param uid the uid
     * @return the moderator
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    @GetMapping("/{uid}")
    @FirebaseAuthorization
    public ResponseEntity<ModeratorPublicResponseDto> getModerator(@PathVariable String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        Moderator moderator = moderatorService.getModerator(uid);

        ModelMapper modelMapper = new ModelMapper();
        ModeratorPublicResponseDto responsePublicModeratorDto = modelMapper.map(moderator, ModeratorPublicResponseDto.class);

        return ResponseEntity.ok(responsePublicModeratorDto);
    }

    /**
     * Create moderator.
     *
     * @param request the request
     * @throws FirebaseAuthException the firebase auth exception
     */
    @PostMapping(path = "", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @FirebaseAuthorization(roles = {"ROLE_MODERATOR"})
    public void createModerator(@Valid @RequestBody CreateModeratorRequestDto request) throws FirebaseAuthException {
        ModelMapper modelMapper = new ModelMapper();
        Moderator moderator = modelMapper.map(request, Moderator.class);

        moderatorService.createModerator(moderator);
    }

    /**
     * Update moderator response entity.
     *
     * @param request the request
     * @param uid     the uid
     * @return the response entity
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    @PutMapping(path ="/{uid}", consumes = "application/json", produces = "application/json")
    @FirebaseAuthorization(roles = {"ROLE_MODERATOR"})
    public ResponseEntity<Moderator> updateModerator(@Valid @RequestBody Moderator request,
                                                     @PathVariable String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {

        Moderator moderator = moderatorService.getModerator(uid);

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.map(request, moderator);

        return ResponseEntity.ok(moderatorService.updateModerator(moderator));
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
        moderatorService.deleteModerator(uid);
    }
}
