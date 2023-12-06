package dev.mikita.userservice.dto.response.common;

import lombok.Data;

/**
 * The type Resident public response dto.
 */
@Data
public class ResidentResponseDto {
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