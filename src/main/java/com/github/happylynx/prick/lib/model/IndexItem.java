package com.github.happylynx.prick.lib.model;

import com.github.happylynx.prick.lib.commands.PrickContext;
import com.github.happylynx.prick.lib.walking.FsEntry;
import com.github.happylynx.prick.lib.walking.FsNonDirEntryType;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class IndexItem extends NonDirItemHash {
    private final Instant changeTime;

    public IndexItem(Path path, HashId contentHash, Instant changeTime, FsNonDirEntryType type) {
        super(path, type, contentHash);
        this.changeTime = changeTime;
    }

    public Instant getChangeTime() {
        return changeTime;
    }

    public String toLine() {
        return String.format("%s\0%s\0%d\0%s", getHash(), getType(), changeTime.getEpochSecond(), getPathString());
    }

    public static IndexItem fromLine(String line) {
        final String[] parts = line.split("\0");
        return new IndexItem(
                Path.of(parts[3]),
                new HashId(parts[0]),
                Instant.ofEpochSecond(Long.parseLong(parts[2])),
                FsNonDirEntryType.parse(parts[1]));
    }

    private String getPathString() {
        return StreamSupport.stream(getPath().spliterator(), false)
                .map(Path::toString)
                .collect(Collectors.joining("/"));
    }

    public static IndexItem fromFsEntry(FsEntry fsEntry, PrickContext ctx, Index oldIndex) {
        final Path path = ctx.getRootDir().relativize(fsEntry.getPath());
        final Optional<IndexItem> oldIndexItemOptional = oldIndex.get(path);
        final Boolean itemUnchanged = oldIndexItemOptional
                .map(indexItem -> indexItem.getChangeTime().equals(fsEntry.getModifiedTime()))
                .orElse(false);
        if (itemUnchanged) {
            return oldIndexItemOptional.get();
        }
        final HashId hash = fsEntry.getType().hash(fsEntry.getPath());
        return new IndexItem(path, hash, fsEntry.getModifiedTime(), fsEntry.getType());
    }
}
