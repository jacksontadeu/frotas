package br.com.novotriunfo.frotas.entity.enums;

public enum Role {
    ADMIN("ROLE_ADMIN"),
    TECNICO("ROLE_TECNICO"),
    BASE("ROLE_BASE");

    private final String role;

    Role(String role) {
        this.role = role;
    }
    public String getRole() {
        return role;
    }
}
