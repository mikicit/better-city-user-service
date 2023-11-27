package dev.mikita.userservice.controller;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.dto.request.CreateServiceRequestDto;
import dev.mikita.userservice.dto.response.ServicePublicResponseDto;
import dev.mikita.userservice.entity.Service;
import dev.mikita.userservice.service.ServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ServiceControllerTest {
    @Mock
    private ServiceService serviceService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ServiceController serviceController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetService() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String uid = "test-uid";
        Service service = new Service();

        when(serviceService.getService(uid)).thenReturn(service);

        ServicePublicResponseDto responsePublicServiceDto = new ServicePublicResponseDto();
        when(modelMapper.map(service, ServicePublicResponseDto.class)).thenReturn(responsePublicServiceDto);

        ResponseEntity<ServicePublicResponseDto> expectedResponse = ResponseEntity.ok(responsePublicServiceDto);

        ResponseEntity<ServicePublicResponseDto> actualResponse = serviceController.getService(uid);

        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), actualResponse.getBody());
    }

    @Test
    void testCreateService() throws FirebaseAuthException {
        CreateServiceRequestDto request = new CreateServiceRequestDto();
        request.setName("Test Service");
        request.setDescription("This is a test service");
        Service service = new Service();
        service.setName("Test Service");
        service.setDescription("This is a test service");
        serviceController.createService(request);
        verify(serviceService, times(1)).createService(service);
    }

    @Test
    void testUpdateService() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String uid = "test-uid";

        Service existingService = new Service();
        existingService.setName("Existing Service");
        existingService.setDescription("This is an existing service");

        Service updatedService = new Service();
        updatedService.setName("Updated Service");
        updatedService.setDescription("This is an updated service");

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());

        when(serviceService.getService(uid)).thenReturn(existingService);
        when(serviceService.updateService(existingService)).thenReturn(updatedService);

        ResponseEntity<Service> expectedResponse = ResponseEntity.ok(updatedService);

        ResponseEntity<Service> actualResponse = serviceController.updateService(updatedService, uid);

        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), actualResponse.getBody());

        verify(serviceService, times(1)).getService(uid);
        verify(serviceService, times(1)).updateService(existingService);
    }

    @Test
    void testDeleteService() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String uid = "test-uid";
        serviceController.deleteService(uid);
        verify(serviceService, times(1)).deleteService(uid);
    }
}

