package dev.mikita.userservice.controller.admin;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.annotation.FirebaseAuthorization;
import dev.mikita.userservice.dto.request.moderator.UpdateUserStatusModeratorRequestDto;
import dev.mikita.userservice.dto.response.moderator.AnalystModeratorResponseDto;
import dev.mikita.userservice.dto.response.moderator.ResidentModeratorResponseDto;
import dev.mikita.userservice.service.ResidentService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/admin/residents")
public class AdminResidentController {
    private final ResidentService residentService;

    @Autowired
    public AdminResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    @GetMapping
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public ResponseEntity<List<ResidentModeratorResponseDto>> getResidents()
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        return ResponseEntity.ok(new ModelMapper().map(
                residentService.getResidents(), new ParameterizedTypeReference<List<ResidentModeratorResponseDto>>() {}.getType()));
    }

    @GetMapping("/{uid}")
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public ResponseEntity<ResidentModeratorResponseDto> getResident(@PathVariable String uid)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        return ResponseEntity.ok(new ModelMapper().map(residentService.getResident(uid), ResidentModeratorResponseDto.class));
    }

    @PatchMapping(path = "/{uid}", consumes = "application/json")
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public ResponseEntity<HttpStatus> updateResidentStatus(@PathVariable String uid,
                                                          @RequestBody UpdateUserStatusModeratorRequestDto status)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        residentService.updateResidentStatus(uid, status.getStatus());
        return ResponseEntity.noContent().build();
    }
}
