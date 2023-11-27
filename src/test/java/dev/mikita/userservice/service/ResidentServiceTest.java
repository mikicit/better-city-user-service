package dev.mikita.userservice.service;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.entity.Resident;
import dev.mikita.userservice.entity.UserStatus;
import dev.mikita.userservice.exception.NotFoundException;
import dev.mikita.userservice.repository.ResidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResidentServiceTest {
    @Mock
    private ResidentRepository residentRepository;

    @InjectMocks
    private ResidentService residentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetResident_ValidUid() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String validUid = "validUid";
        Resident resident = new Resident();
        when(residentRepository.find(validUid)).thenReturn(resident);

        Resident result = residentService.getResident(validUid);
        assertEquals(resident, result);
    }

    @Test
    void testGetResident_InvalidUid() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String invalidUid = "invalidUid";
        when(residentRepository.find(invalidUid)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> residentService.getResident(invalidUid));
    }

    @Test
    void testCreateResident() throws FirebaseAuthException {
        Resident resident = new Resident();
        residentService.createResident(resident);
        verify(residentRepository).persist(resident);
    }

    @Test
    void testUpdateResident_ValidResident_ReturnsUpdatedResident() throws FirebaseAuthException, ExecutionException, InterruptedException {
        Resident existingResident = new Resident();
        Resident updatedResident = new Resident();
        when(residentRepository.find(existingResident.getUid())).thenReturn(existingResident);
        when(residentRepository.update(updatedResident)).thenReturn(updatedResident);

        Resident result = residentService.updateResident(updatedResident);

        assertEquals(updatedResident, result);
        verify(residentRepository).find(existingResident.getUid());
        verify(residentRepository).update(updatedResident);
    }

    @Test
    void testUpdateResident_NonExistingResident() throws FirebaseAuthException, ExecutionException, InterruptedException {
        Resident nonExistingResident = new Resident();
        when(residentRepository.find(nonExistingResident.getUid())).thenReturn(null);

        assertThrows(NotFoundException.class, () -> residentService.updateResident(nonExistingResident));
        verify(residentRepository).find(nonExistingResident.getUid());
    }

    @Test
    void testDeleteResident_ExistingResident() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String existingUid = "existingUid";
        Resident existingResident = new Resident();
        when(residentRepository.find(existingUid)).thenReturn(existingResident);

        residentService.deleteResident(existingUid);

        assertEquals(UserStatus.DELETED, existingResident.getStatus());
        verify(residentRepository).find(existingUid);
        verify(residentRepository).update(existingResident);
    }
}

