package dev.mikita.userservice.dto.response.resident;

import lombok.Data;

/**
 * The type Resident private response dto.
 */
@Data
public class ResidentResidentResponseDto {
    String uid;
    String email;
    String phoneNumber;
    String firstName;
    String lastName;
    String photo;
}
