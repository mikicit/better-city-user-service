package dev.mikita.userservice.controller;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.dto.request.CreateModeratorRequestDto;
import dev.mikita.userservice.dto.response.ModeratorPublicResponseDto;
import dev.mikita.userservice.entity.Moderator;
import dev.mikita.userservice.service.ModeratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModeratorControllerTest {
    @Mock
    private ModeratorService moderatorService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ModeratorController moderatorController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetModerator() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String uid = "testUid";
        Moderator moderator = new Moderator();
        moderator.setUid(uid);
        moderator.setEmail("test@test.com");

        ModeratorPublicResponseDto expectedResponseDto = new ModeratorPublicResponseDto();
        expectedResponseDto.setUid(uid);

        when(moderatorService.getModerator(uid)).thenReturn(moderator);
        when(modelMapper.map(moderator, ModeratorPublicResponseDto.class)).thenReturn(expectedResponseDto);

        ResponseEntity<ModeratorPublicResponseDto> response = moderatorController.getModerator(uid);

        assertEquals(expectedResponseDto, response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testCreateModerator() throws FirebaseAuthException {
        CreateModeratorRequestDto requestDto = new CreateModeratorRequestDto();
        requestDto.setEmail("john.doe@example.com");
        requestDto.setPassword("password");

        ModelMapper modelMapper = new ModelMapper();
        Moderator moderator = modelMapper.map(requestDto, Moderator.class);
        moderatorController.createModerator(requestDto);
        verify(moderatorService).createModerator(moderator);
    }

    @Test
    void testUpdateModerator() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String uid = "testUid";

        Moderator existingModerator = new Moderator();
        existingModerator.setUid(uid);
        existingModerator.setEmail("email@email.com");

        Moderator updatedModerator = new Moderator();
        updatedModerator.setUid(uid);
        updatedModerator.setEmail("email@email.com");

        when(moderatorService.getModerator(uid)).thenReturn(existingModerator);
        when(moderatorService.updateModerator(existingModerator)).thenReturn(existingModerator);

        ResponseEntity<Moderator> response = moderatorController.updateModerator(updatedModerator, uid);

        assertEquals(existingModerator, response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDeleteModerator() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String uid = "testUid";
        moderatorController.deleteResident(uid);
        verify(moderatorService).deleteModerator(uid);
    }
}
