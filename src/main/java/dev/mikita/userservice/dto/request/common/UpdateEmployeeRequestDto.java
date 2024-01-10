package dev.mikita.userservice.dto.request.common;

import dev.mikita.userservice.annotation.NullCheck;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateEmployeeRequestDto {
    @NullCheck(message = "Specify the first name")
    String firstName;
    @NullCheck(message = "Specify the last name")
    String lastName;
    @NullCheck(message = "Specify the phone number")
    String phoneNumber;
    @NullCheck(message = "Specify email")
    @Email(message = "The email is not valid")
    String email;
    @NullCheck(message = "Specify a password")
    String password;
    @NullCheck(message = "Specify the department id")
    String departmentUid;
}