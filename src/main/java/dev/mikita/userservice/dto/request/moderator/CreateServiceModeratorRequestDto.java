package dev.mikita.userservice.dto.request.moderator;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * The type Create service request dto.
 */
@Data
public class CreateServiceModeratorRequestDto {
    @NotBlank(message = "Specify the name.")
    String name;
    @NotBlank(message = "Specify the email.")
    @Email(message = "The email is not valid.")
    String email;
    @NotBlank(message = "Specify the password.")
    String password;
}