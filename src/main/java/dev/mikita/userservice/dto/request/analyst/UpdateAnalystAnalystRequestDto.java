package dev.mikita.userservice.dto.request.analyst;

import dev.mikita.userservice.annotation.NullCheck;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateAnalystAnalystRequestDto {
    @NullCheck(message = "Specify email")
    @Email(message = "The email is not valid.")
    String email;
    @NullCheck(message = "Specify a name")
    String name;
    @NullCheck(message = "Specify the description")
    String description;
    @NullCheck(message = "Specify a password")
    String password;
}