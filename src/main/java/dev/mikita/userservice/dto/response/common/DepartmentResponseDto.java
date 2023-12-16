package dev.mikita.userservice.dto.response.common;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DepartmentResponseDto {
    String uid;
    String name;
    String description;
    String address;
    String phoneNumber;
    List<Long> categories;
    LocalDateTime creationDate;
}
