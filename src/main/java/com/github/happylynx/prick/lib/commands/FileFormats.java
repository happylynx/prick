package com.github.happylynx.prick.lib.commands;

import com.github.happylynx.prick.lib.model.HashId;
import com.github.happylynx.prick.lib.model.Index;
import com.github.happylynx.prick.lib.model.TreeItem;

import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileFormats {

    public static final String LINE_SEPARATOR = "\n";

    private FileFormats() {}

    public static String createTreeLine(TreeItem item) {
        return String.format(
                "%s\0%s\0%s",
                item.getHash(),
                item.getType().getCode(),
                item.getPath().getFileName().toString());
    }

    public static TreeItem parseTreeLine(String line) {
        throw new UnsupportedOperationException();
    }

    public static String createCommit(HashId rootTree, String... parents) {
        return rootTree + LINE_SEPARATOR
                + String.join(" ", parents) + LINE_SEPARATOR
                + Instant.now().getEpochSecond() + LINE_SEPARATOR;
    }
}
