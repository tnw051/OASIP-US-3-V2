package int221.oasip.backendus3.entities;

public enum Role {
    ADMIN,
    STUDENT,
    LECTURER;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
