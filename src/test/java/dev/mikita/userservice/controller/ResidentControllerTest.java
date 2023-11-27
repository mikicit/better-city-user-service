package dev.mikita.userservice.controller;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.dto.request.CreateResidentRequestDto;
import dev.mikita.userservice.dto.response.CountResponseDto;
import dev.mikita.userservice.dto.response.ResidentPublicResponseDto;
import dev.mikita.userservice.entity.Resident;
import dev.mikita.userservice.service.ResidentService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ResidentControllerTest {
    @Mock
    private ResidentService residentService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ResidentController residentController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetResident() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String uid = "someUid";
        Resident resident = new Resident();
        ResidentPublicResponseDto expectedResponseDto = new ResidentPublicResponseDto();

        when(residentService.getResident(uid)).thenReturn(resident);
        when(modelMapper.map(resident, ResidentPublicResponseDto.class)).thenReturn(expectedResponseDto);

        ResponseEntity<ResidentPublicResponseDto> response = residentController.getResident(uid, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponseDto, response.getBody());
    }

    @Test
    public void testGetIssuesCount() {
        String uid = "someUid";
        String authorizationHeader = "Bearer token";
        String expectedUri = "http://issue-service/api/v1/issues/count?authorId=" + uid;
        CountResponseDto expectedResponseDto = new CountResponseDto();

        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationHeader);
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<CountResponseDto> responseEntity = new ResponseEntity<>(expectedResponseDto, HttpStatus.OK);
        doReturn(responseEntity)
                .when(restTemplate)
                .exchange(
                        eq(expectedUri),
                        eq(HttpMethod.GET),
                        eq(httpEntity),
                        any(ParameterizedTypeReference.class)
                );
        ResponseEntity<CountResponseDto> response = residentController.getIssuesCount(uid, request);

        verify(request, times(1)).getHeader("Authorization");
        verify(restTemplate, times(1)).exchange(
                eq(expectedUri),
                eq(HttpMethod.GET),
                eq(httpEntity),
                any(ParameterizedTypeReference.class)
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponseDto, response.getBody());
    }

    @Test
    public void testCreateResident_ValidRequest() throws FirebaseAuthException {
        CreateResidentRequestDto request = new CreateResidentRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPassword("password");
        request.setEmail("email@email.com");

        residentController.createResident(request);

        verify(residentService, times(1)).createResident(any(Resident.class));
    }

    @Test
    public void testCreateResident_InvalidRequest() throws FirebaseAuthException {
        verify(residentService, never()).createResident(any(Resident.class));
    }

    @Test
    public void testUpdateResident_ValidRequest() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String uid = "someUid";
        Resident request = new Resident();

        Resident existingResident = new Resident();

        when(residentService.getResident(uid)).thenReturn(existingResident);

        Resident updatedResident = new Resident();

        when(residentService.updateResident(existingResident)).thenReturn(updatedResident);

        ResponseEntity<Resident> response = residentController.updateResident(request, uid);

        verify(residentService, times(1)).getResident(uid);
        verify(residentService, times(1)).updateResident(existingResident);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedResident, response.getBody());
    }

    @Test
    public void testUpdateResident_InvalidRequest() throws FirebaseAuthException, ExecutionException, InterruptedException {
        String uid = "someUid";
        verify(residentService, never()).getResident(uid);
        verify(residentService, never()).updateResident(any(Resident.class));
    }
}