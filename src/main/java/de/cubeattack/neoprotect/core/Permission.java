package de.cubeattack.neoprotect.core;

public enum Permission {

    ADMIN("neoprotect.admin") ,
    NOTIFY("neoprotect.notify");

    public final String value;

    Permission(String permission) {
        this.value = permission;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
