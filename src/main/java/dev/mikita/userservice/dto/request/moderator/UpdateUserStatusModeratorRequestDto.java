package dev.mikita.userservice.dto.request.moderator;

import dev.mikita.userservice.entity.UserStatus;
import lombok.Data;

@Data
public class UpdateUserStatusModeratorRequestDto {
    UserStatus status;
}
