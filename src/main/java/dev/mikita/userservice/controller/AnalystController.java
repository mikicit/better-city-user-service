package dev.mikita.userservice.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import dev.mikita.userservice.annotation.FirebaseAuthorization;
import dev.mikita.userservice.dto.request.analyst.UpdateAnalystAnalystRequestDto;
import dev.mikita.userservice.dto.response.analyst.AnalystAnalystResponseDto;
import dev.mikita.userservice.entity.Analyst;
import dev.mikita.userservice.service.AnalystService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/analysts")
public class AnalystController {
    private final AnalystService analystService;

    @Autowired
    public AnalystController(AnalystService analystService) {
        this.analystService = analystService;
    }

    @GetMapping(path = "/me", produces = "application/json")
    @FirebaseAuthorization(roles = {"ANALYST"}, statuses = {"ACTIVE"})
    public ResponseEntity<AnalystAnalystResponseDto> getCurrentAnalyst(HttpServletRequest request)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        return ResponseEntity.ok(new ModelMapper().map(
                analystService.getAnalyst(token.getUid()), AnalystAnalystResponseDto.class));
    }

    @PutMapping(path = "/me", consumes = "application/json", produces = "application/json")
    @FirebaseAuthorization(roles = {"ANALYST"}, statuses = {"ACTIVE"})
    public ResponseEntity<AnalystAnalystResponseDto> updateCurrentAnalyst(
            @Valid @RequestBody UpdateAnalystAnalystRequestDto requestDto,
            HttpServletRequest request)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        ModelMapper modelMapper = new ModelMapper();
        Analyst analyst = modelMapper.map(requestDto, Analyst.class);
        analyst.setUid(token.getUid());

        return ResponseEntity.ok(modelMapper.map(analystService.updateAnalyst(analyst), AnalystAnalystResponseDto.class));
    }
}
