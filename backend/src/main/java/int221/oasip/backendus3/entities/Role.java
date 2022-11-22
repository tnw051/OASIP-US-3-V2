package int221.oasip.backendus3.entities;

public enum Role {
    ADMIN,
    STUDENT,
    LECTURER;

    public static Role fromString(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    public static Role tryFromString(String role) {
        try {
            return Role.fromString(role);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
