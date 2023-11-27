package dev.mikita.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * The type Create moderator request dto.
 */
@Data
public class CreateModeratorRequestDto {
    @NotBlank(message = "Specify the email.")
    @Email(message = "The email is not valid.")
    private String email;
    @NotBlank(message = "Specify the password.")
    private String password;
}
