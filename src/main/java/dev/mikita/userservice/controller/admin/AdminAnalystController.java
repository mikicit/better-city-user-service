package dev.mikita.userservice.controller.admin;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.annotation.FirebaseAuthorization;
import dev.mikita.userservice.dto.request.moderator.CreateAnalystModeratorRequestDto;
import dev.mikita.userservice.dto.request.moderator.UpdateUserStatusModeratorRequestDto;
import dev.mikita.userservice.dto.response.moderator.AnalystModeratorResponseDto;
import dev.mikita.userservice.entity.Analyst;
import dev.mikita.userservice.service.AnalystService;
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
@RequestMapping("/api/v1/admin/analysts")
public class AdminAnalystController {
    private final AnalystService analystService;

    @Autowired
    public AdminAnalystController(AnalystService analystService) {
        this.analystService = analystService;
    }

    @GetMapping(path = "", produces = "application/json")
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public ResponseEntity<List<AnalystModeratorResponseDto>> getAnalysts() {
        return ResponseEntity.ok(new ModelMapper().map(
                analystService.getAnalysts(), new ParameterizedTypeReference<List<AnalystModeratorResponseDto>>() {}.getType()));
    }

    @GetMapping(path = "/{uid}", produces = "application/json")
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public ResponseEntity<AnalystModeratorResponseDto> getAnalyst(@PathVariable String uid)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        return ResponseEntity.ok(new ModelMapper().map(analystService.getAnalyst(uid), AnalystModeratorResponseDto.class));
    }

    @PostMapping(path = "", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public void createAnalyst(@Valid @RequestBody CreateAnalystModeratorRequestDto requestDto)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        analystService.createAnalyst(new ModelMapper().map(requestDto, Analyst.class));
    }

    @PatchMapping(path = "/{uid}", consumes = "application/json")
    @FirebaseAuthorization(roles = {"MODERATOR"})
    public ResponseEntity<HttpStatus> updateAnalystStatus(@PathVariable String uid,
                                                          @RequestBody UpdateUserStatusModeratorRequestDto status)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        analystService.updateAnalystStatus(uid, status.getStatus());
        return ResponseEntity.noContent().build();
    }
}
