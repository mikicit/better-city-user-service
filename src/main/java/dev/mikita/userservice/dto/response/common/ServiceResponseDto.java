package dev.mikita.userservice.dto.response.common;

import lombok.Data;

@Data
public class ServiceResponseDto {
    String uid;
    String email;
    String phoneNumber;
    String name;
    String description;
    String address;
    String photo;
}
