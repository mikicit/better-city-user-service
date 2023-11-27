package dev.mikita.userservice.entity;

import lombok.Data;

/**
 * The type User.
 */
@Data
public class User {
    private String uid;
    private String email;
    private String password;
    private String photo;
    private UserRole role;
    private UserStatus status;
}