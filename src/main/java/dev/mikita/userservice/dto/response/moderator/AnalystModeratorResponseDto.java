package dev.mikita.userservice.dto.response.moderator;

import dev.mikita.userservice.entity.UserStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AnalystModeratorResponseDto {
    String uid;
    String email;
    String name;
    String description;
    UserStatus status;
    LocalDateTime creationDate;
}
