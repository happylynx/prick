package com.github.happylynx.prick.lib.model;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;

public class FileTimestamps {
    private List<FileTimestamp> timestamps;

    public void write(final Path path) {

    }

    public static FileTimestamps read(final Path path) {
        throw new RuntimeException();
    }
}
