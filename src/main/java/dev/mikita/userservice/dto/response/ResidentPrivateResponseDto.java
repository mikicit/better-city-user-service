package dev.mikita.userservice.dto.response;

import lombok.Data;

/**
 * The type Resident private response dto.
 */
@Data
public class ResidentPrivateResponseDto {
    /**
     * The Uid.
     */
    String uid;
    /**
     * The Email.
     */
    String email;
    /**
     * The First name.
     */
    String firstName;
    /**
     * The Last name.
     */
    String lastName;
    /**
     * The Photo.
     */
    String photo;
}
