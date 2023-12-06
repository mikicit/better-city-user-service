package dev.mikita.userservice.dto.response.common;

import lombok.Data;

@Data
public class EmployeeResponseDto {
    String uid;
    String email;
    String firstName;
    String lastName;
    String phoneNumber;
    String departmentUid;
}