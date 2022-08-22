package int221.oasip.backendus3.entities;

public enum Role {
    ADMIN,
    STUDENT,
    LECTURER;

    public static Role fromString(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
