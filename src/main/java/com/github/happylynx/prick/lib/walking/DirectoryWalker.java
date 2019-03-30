package com.github.happylynx.prick.lib.walking;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectoryWalker {
    private final Stack<Path> dfsStack = new Stack<>();

    public DirectoryWalker(Path root) {
        dfsStack.push(root);
    }

    public FsEntry getNext() {
        try {
            return getNextUnchecked();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FsEntry getNextUnchecked() throws IOException {
        while (!dfsStack.isEmpty()) {
            final Path fsItem = dfsStack.pop();
            if (Files.isDirectory(fsItem, LinkOption.NOFOLLOW_LINKS)) {
                try (Stream<Path> listing = Files.list(fsItem)) {
                    final List<Path> sortedItems =listing
                            .sorted(Comparator.<Path>naturalOrder().reversed())
                            .collect(Collectors.toList());
                    dfsStack.addAll(sortedItems);
                }
                continue;
            }
            final BasicFileAttributes basicFileAttributes =
                    Files.readAttributes(fsItem, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (basicFileAttributes.isRegularFile()) {
                return new FsEntry(fsItem, FsNonDirEntryType.FILE, basicFileAttributes.lastModifiedTime().toInstant());
            }
            if (basicFileAttributes.isSymbolicLink()) {
                return new FsEntry(fsItem, FsNonDirEntryType.SYMLINK, basicFileAttributes.lastModifiedTime().toInstant());
            }
        }
        return null;
    }
}
