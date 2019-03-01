package com.github.happylynx.prick.lib.guice;

import com.google.inject.Provider;

import java.nio.file.Path;

public class PrickRootProvider implements Provider<Path> {

    private Path prickRoot;

    void set(Path prickRoot) {
        this.prickRoot = prickRoot;
    }

    @Override
    public Path get() {
        return prickRoot;
    }
}
