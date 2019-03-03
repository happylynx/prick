package com.github.happylynx.prick.lib.model;

import com.github.happylynx.prick.lib.commands.PrickContext;
import com.github.happylynx.prick.lib.walking.FsEntry;
import com.github.happylynx.prick.lib.walking.FsNonDirEntryType;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class IndexItem implements TreeItem {
    /**
     * relative to the prick root
     */
    private final Path path;
    private final HashId contentHash;
    private final Instant changeTime;
    private final FsNonDirEntryType type;

    public IndexItem(Path path, HashId contentHash, Instant changeTime, FsNonDirEntryType type) {
        this.path = path;
        this.contentHash = contentHash;
        this.changeTime = changeTime;
        this.type = type;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public FsEntryType getType() {
        return type;
    }

    @Override
    public HashId getHash() {
        return contentHash;
    }

    public Instant getChangeTime() {
        return changeTime;
    }

    public String toLine() {
        return String.format("%s\0%s\0%d\0%s", contentHash, type, changeTime.getEpochSecond(), getPathString());
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
        return StreamSupport.stream(path.spliterator(), false)
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
