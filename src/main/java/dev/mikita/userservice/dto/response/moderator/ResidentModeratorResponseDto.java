package dev.mikita.userservice.dto.response.moderator;

import dev.mikita.userservice.entity.UserStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ResidentModeratorResponseDto {
    String uid;
    String email;
    String phoneNumber;
    String firstName;
    String lastName;
    String photo;
    UserStatus status;
    LocalDateTime creationDate;
}
