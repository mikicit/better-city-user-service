package dev.mikita.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * The type Create service request dto.
 */
@Data
public class CreateServiceRequestDto {
    @NotBlank(message = "Specify the name.")
    private String name;
    @NotBlank(message = "Specify the description.")
    private String description;
    @NotBlank(message = "Specify the email.")
    @Email(message = "The email is not valid.")
    private String email;
    @NotBlank(message = "Specify the password.")
    private String password;
}
