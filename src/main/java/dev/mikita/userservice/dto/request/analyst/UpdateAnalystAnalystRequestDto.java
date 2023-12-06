package dev.mikita.userservice.dto.request.analyst;

import lombok.Data;

@Data
public class UpdateAnalystAnalystRequestDto {
    String email;
    String name;
    String description;
    String password;
}