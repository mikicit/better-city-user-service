package dev.mikita.userservice.dto.request.common;

import lombok.Data;

@Data
public class UpdateResidentRequestDto {
    String firstName;
    String lastName;
    String email;
    String password;
}
