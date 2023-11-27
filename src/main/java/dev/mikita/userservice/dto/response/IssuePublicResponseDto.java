package dev.mikita.userservice.dto.response;

import lombok.Data;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;

/**
 * The type Issue public response dto.
 */
@Data
public class IssuePublicResponseDto {
    /**
     * The Id.
     */
    Long id;
    /**
     * The Photo.
     */
    String photo;
    /**
     * The Coordinates.
     */
    Point coordinates;
    /**
     * The Description.
     */
    String description;
    /**
     * The Title.
     */
    String title;
    /**
     * The Author id.
     */
    String authorId;
    /**
     * The Category id.
     */
    Long categoryId;
    /**
     * The Creation date.
     */
    LocalDateTime creationDate;
    /**
     * The Status.
     */
    String status;
}