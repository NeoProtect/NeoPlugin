package de.cubeattack.neoprotect.core.model;

public class Gameshield {

    private final String id;
    private final String name;

    public Gameshield(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
