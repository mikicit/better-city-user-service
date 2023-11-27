package dev.mikita.userservice.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The type Service.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Service extends User {
    private String name;
    private String description;
}
