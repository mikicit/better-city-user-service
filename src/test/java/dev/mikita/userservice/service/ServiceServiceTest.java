package dev.mikita.userservice.service;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.entity.UserStatus;
import dev.mikita.userservice.exception.NotFoundException;
import dev.mikita.userservice.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ServiceServiceTest {
    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceService serviceService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetService_existingService() throws ExecutionException, InterruptedException, FirebaseAuthException {
        String uid = "123";
        dev.mikita.userservice.entity.Service service = new dev.mikita.userservice.entity.Service();
        when(serviceRepository.find(uid)).thenReturn(service);

        dev.mikita.userservice.entity.Service result = serviceService.getService(uid);

        assertEquals(service, result);
        verify(serviceRepository, times(1)).find(uid);
    }

    @Test
    public void testGetService_nonExistingService() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String uid = "123";
        when(serviceRepository.find(uid)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> serviceService.getService(uid));

        verify(serviceRepository, times(1)).find(uid);
    }

    @Test
    public void testCreateService_validService() throws FirebaseAuthException {
        dev.mikita.userservice.entity.Service service = new dev.mikita.userservice.entity.Service();

        serviceService.createService(service);

        verify(serviceRepository, times(1)).persist(service);
    }

    @Test
    public void testUpdateService_nonExistingService() throws FirebaseAuthException, ExecutionException, InterruptedException {
        dev.mikita.userservice.entity.Service service = new dev.mikita.userservice.entity.Service();
        service.setUid("123");
        when(serviceRepository.find(service.getUid())).thenReturn(null);

        assertThrows(NotFoundException.class, () -> serviceService.updateService(service));

        verify(serviceRepository, times(1)).find(service.getUid());
        verify(serviceRepository, never()).update(any());
    }

    @Test
    public void testDeleteService_existingService() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String uid = "123";
        dev.mikita.userservice.entity.Service service = new dev.mikita.userservice.entity.Service();
        service.setStatus(UserStatus.ACTIVE);
        when(serviceRepository.find(uid)).thenReturn(service);

        serviceService.deleteService(uid);

        assertEquals(UserStatus.DELETED, service.getStatus());
        verify(serviceRepository, times(1)).find(uid);
        verify(serviceRepository, times(1)).update(service);
    }

    @Test
    public void testDeleteService_deletedService() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String uid = "123";
        dev.mikita.userservice.entity.Service service = new dev.mikita.userservice.entity.Service();
        service.setStatus(UserStatus.DELETED);
        when(serviceRepository.find(uid)).thenReturn(service);

        assertThrows(RuntimeException.class, () -> serviceService.deleteService(uid));

        verify(serviceRepository, times(1)).find(uid);
        verify(serviceRepository, never()).update(any());
    }
}

