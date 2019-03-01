package com.github.happylynx.prick.lib.walking;

import java.nio.file.Path;
import java.time.Instant;

public class FsEntry {
    private final Path path;
    private final FsNonDirEntryType type;
    private final Instant modifiedTime;

    public FsEntry(Path path, FsNonDirEntryType type, Instant modifiedTime) {
        this.path = path;
        this.type = type;
        this.modifiedTime = modifiedTime;
    }

    public Path getPath() {
        return path;
    }

    public FsNonDirEntryType getType() {
        return type;
    }

    public Instant getModifiedTime() {
        return modifiedTime;
    }
}
