package com.github.happylynx.prick.lib.model;

public class HashId {
    private final String hash;

    public HashId(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return hash;
    }
}
