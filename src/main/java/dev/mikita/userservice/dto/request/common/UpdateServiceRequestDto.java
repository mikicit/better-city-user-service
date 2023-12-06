package dev.mikita.userservice.dto.request.common;

import lombok.Data;

@Data
public class UpdateServiceRequestDto {
    String email;
    String phoneNumber;
    String name;
    String description;
    String address;
    String password;
}