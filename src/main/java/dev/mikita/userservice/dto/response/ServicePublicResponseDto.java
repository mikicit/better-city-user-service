package dev.mikita.userservice.dto.response;

import lombok.Data;

/**
 * The type Service public response dto.
 */
@Data
public class ServicePublicResponseDto {
    /**
     * The Uid.
     */
    String uid;
    /**
     * The Name.
     */
    String name;
    /**
     * The Description.
     */
    String description;
    /**
     * The Photo.
     */
    String photo;
}
