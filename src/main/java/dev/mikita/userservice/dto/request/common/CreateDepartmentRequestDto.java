package dev.mikita.userservice.dto.request.common;

import lombok.Data;
import java.util.List;

@Data
public class CreateDepartmentRequestDto {
    private String name;
    private String description;
    private String address;
    private String phoneNumber;
    private List<Long> categories;
}
