package com.github.happylynx.prick.lib.model;

import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Set;

public class FileTimestamp {

    private static final DateTimeFormatter DATE_FORMATTEER = DateTimeFormatter.ofPattern("YYYY-MM-DD HH:mm:ss");

    final LocalDateTime changedDate;
    final FsEntryType entryType;
    final Set<PosixFilePermission> permissions;

    public FileTimestamp(LocalDateTime changedDate, FsEntryType entryType, Set<PosixFilePermissions> permissions) {
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
        final FsEntryType type = FsEntryType.parse(parts[0]);
        PosixFilePermissions.fromString()
    }
}
