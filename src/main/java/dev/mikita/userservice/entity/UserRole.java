package dev.mikita.userservice.entity;

/**
 * The enum User role.
 */
public enum UserRole {
    /**
     * Admin user role.
     */
    ADMIN("ROLE_ADMIN"),
    /**
     * Moderator user role.
     */
    MODERATOR("ROLE_MODERATOR"),
    /**
     * Resident user role.
     */
    RESIDENT("ROLE_RESIDENT"),
    /**
     * Service user role.
     */
    SERVICE("ROLE_SERVICE");

    private final String name;

    UserRole(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
