package dev.mikita.userservice.service;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.entity.Moderator;
import dev.mikita.userservice.exception.NotFoundException;
import dev.mikita.userservice.repository.ModeratorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ModeratorServiceTest {

    @Mock
    private ModeratorRepository moderatorRepository;

    @InjectMocks
    private ModeratorService moderatorService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetModerator() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String uid = "existingUid";
        Moderator existingModerator = new Moderator();
        when(moderatorRepository.find(uid)).thenReturn(existingModerator);

        Moderator result = moderatorService.getModerator(uid);

        assertEquals(existingModerator, result);
        verify(moderatorRepository, times(1)).find(uid);
        verifyNoMoreInteractions(moderatorRepository);
    }

    @Test
    public void testGetModerator_nonExistingModerator() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String uid = "nonExistingUid";
        when(moderatorRepository.find(uid)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> moderatorService.getModerator(uid));

        verify(moderatorRepository, times(1)).find(uid);
        verifyNoMoreInteractions(moderatorRepository);
    }

    @Test
    public void testCreateModerator() throws FirebaseAuthException {
        Moderator moderator = new Moderator();

        moderatorService.createModerator(moderator);

        verify(moderatorRepository, times(1)).persist(moderator);
        verifyNoMoreInteractions(moderatorRepository);
    }
}
