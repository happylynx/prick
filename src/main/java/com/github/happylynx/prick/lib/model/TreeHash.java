package com.github.happylynx.prick.lib.model;

import java.nio.file.Path;

public class TreeHash implements TreeItem {

    private final HashId hash;

    /**
     * relative to prick root
     */
    private final Path path;

    public TreeHash(HashId hash, Path path) {
        this.hash = hash;
        this.path = path;
    }

    public HashId getHash() {
        return hash;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public FsEntryType getType() {
        return FsDirType.DIRECTORY;
    }
}
