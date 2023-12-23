package dev.mikita.userservice.dto.request.common;

import dev.mikita.userservice.annotation.NullCheck;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateServiceRequestDto {

    @NullCheck(message = "Specify email")
    @Email(message = "The email is not valid")
    String email;
    @NullCheck(message = "Specify the phone number")
    String phoneNumber;
    @NullCheck(message = "Specify a name")
    String name;
    @NullCheck(message = "Specify the description")
    String description;
    @NullCheck(message = "Specify the address")
    String address;
    @NullCheck(message = "Specify a password")
    String password;
}