package dev.mikita.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * The type Create resident request dto.
 */
@Data
public class CreateResidentRequestDto {
    @NotBlank(message = "Specify the first name.")
    private String firstName;
    @NotBlank(message = "Specify the last name.")
    private String lastName;
    @NotBlank(message = "Specify the email.")
    @Email(message = "The email is not valid.")
    private String email;
    @NotBlank(message = "Specify the password.")
    private String password;
}