package dev.mikita.userservice.dto.response.common;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmployeeResponseDto {
    String uid;
    String email;
    String firstName;
    String lastName;
    String phoneNumber;
    String departmentUid;
    LocalDateTime creationDate;
}