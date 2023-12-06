package dev.mikita.userservice.dto.request.common;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * The type Create resident request dto.
 */
@Data
public class CreateResidentRequestDto {
    @NotBlank(message = "Specify the first name.")
    String firstName;
    @NotBlank(message = "Specify the last name.")
    String lastName;
    @NotBlank(message = "Specify the email.")
    @Email(message = "The email is not valid.")
    String email;
    @NotBlank(message = "Specify the password.")
    String password;
}