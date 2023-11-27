package dev.mikita.userservice.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The type Resident.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Resident extends User {
    private String firstName;
    private String lastName;
}
