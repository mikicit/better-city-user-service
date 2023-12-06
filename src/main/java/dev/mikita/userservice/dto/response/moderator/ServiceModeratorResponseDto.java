package dev.mikita.userservice.dto.response.moderator;

import dev.mikita.userservice.entity.UserStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ServiceModeratorResponseDto {
    String uid;
    String email;
    String phoneNumber;
    String name;
    String description;
    String photo;
    UserStatus status;
    LocalDateTime creationDate;
}
