package com.github.happylynx.prick.lib.model;

import com.github.happylynx.prick.lib.walking.FsNonDirEntryType;

import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

// TODO delete?
public class FileTimestamp {

    private static final DateTimeFormatter DATE_FORMATTEER = DateTimeFormatter.ofPattern("YYYY-MM-DD HH:mm:ss");

    final LocalDateTime changedDate;
    final FsNonDirEntryType entryType;
    final Set<PosixFilePermission> permissions;

    public FileTimestamp(LocalDateTime changedDate, FsNonDirEntryType entryType, Set<PosixFilePermission> permissions) {
        this.changedDate = changedDate;
        this.entryType = entryType;
        this.permissions = permissions;
    }

    public String toString() {
        return entryType.getCode() + " "
                + PosixFilePermissions.toString(permissions) + " "
                + DATE_FORMATTEER.format(changedDate);
    }

    public static FileTimestamp parse(String input) {
        final String[] parts = input.trim().split(" ");
        final FsNonDirEntryType type = FsNonDirEntryType.parse(parts[0]);
//        PosixFilePermissions.fromString();
        throw new UnsupportedOperationException();
    }
}
