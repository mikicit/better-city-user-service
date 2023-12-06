package dev.mikita.userservice.dto.request.common;

import lombok.Data;

@Data
public class CreateEmployeeRequestDto {
    String firstName;
    String lastName;
    String phoneNumber;
    String email;
    String password;
    String departmentUid;
}
