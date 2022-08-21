package int221.oasip.backendus3.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    ADMIN,
    STUDENT,
    LECTURER;

    @JsonCreator
    public static Role fromString(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
