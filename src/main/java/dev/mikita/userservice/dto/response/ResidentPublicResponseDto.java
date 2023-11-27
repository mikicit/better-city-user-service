package dev.mikita.userservice.dto.response;

import lombok.Data;

/**
 * The type Resident public response dto.
 */
@Data
public class ResidentPublicResponseDto {
    /**
     * The Uid.
     */
    String uid;
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