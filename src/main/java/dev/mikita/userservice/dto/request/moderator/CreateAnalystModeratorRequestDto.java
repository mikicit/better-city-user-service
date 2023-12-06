package dev.mikita.userservice.dto.request.moderator;

import lombok.Data;

@Data
public class CreateAnalystModeratorRequestDto {
    private String email;
    private String password;
    private String name;
}