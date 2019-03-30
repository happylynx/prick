package com.github.happylynx.prick.lib.model;

import com.github.happylynx.prick.lib.LibUtils;
import com.github.happylynx.prick.lib.commands.FileFormats;
import com.github.happylynx.prick.lib.commands.PrickContext;
import com.github.happylynx.prick.lib.walking.FsEntry;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Index {

    private final SortedMap<Path, IndexItem> indexItems;

    /**
     * relative to the prick root
     */
    private final Path root;

    public Index(SortedMap<Path, IndexItem> indexItems, Path root) {
        this.indexItems = indexItems;
        this.root = root;
    }

    public static Index fromLines(Stream<String> lines) {
        final SortedMap<Path, IndexItem> items = lines.map(IndexItem::fromLine)
                .collect(toSortedMap());
        return new Index(items, Path.of(""));
    }

    @NotNull
    public static Index empty() {
        return new Index(Collections.emptySortedMap(), Path.of(""));
    }

    public String toLines() {
        return indexItems.values().stream()
                .map(IndexItem::toLine)
                .collect(Collectors.joining(FileFormats.LINE_SEPARATOR));
    }

    public static Index fromDisk(Path root, PrickContext ctx, Index oldIndex) {
        final SortedMap<Path, IndexItem> indexItems = LibUtils.dirStream(root)
                .parallel()
                .filter(entry -> ignore(toRelativePath(entry, ctx)))
                .map(fsEntry -> IndexItem.fromFsEntry(fsEntry, ctx, oldIndex))
                .collect(toSortedMap());
        return new Index(indexItems, ctx.getRootDir().relativize(root));
    }

    private static Path toRelativePath(FsEntry entry, PrickContext ctx) {
        return ctx.getRootDir().relativize(entry.getPath());
    }

    /**
     * IndexItems are ordered according to their path.
     */
    public Collection<IndexItem> getItems() {
        return indexItems.values();
    }

    public Index combineWith(Index other) {
        if (root.equals(other.root)) {
            return other;
        }
        if (other.root.startsWith(root)) {
            return combineWithSubtree(other);
        }
        if (root.startsWith(other.root)) {
            return other.combineWithSubtree(this);
        }
        throw new RuntimeException("One tree has to be a subtree of the other");
    }

    private Index combineWithSubtree(Index subtree) {
        final Path subtreePrefix = root.relativize(subtree.root);
        final TreeMap<Path, IndexItem> resultIndexItems = new TreeMap<>(indexItems);
        resultIndexItems.keySet().removeIf(path -> path.startsWith(subtreePrefix));
        resultIndexItems.putAll(subtree.indexItems);
        return new Index(resultIndexItems, root);
    }

    public Optional<IndexItem> get(Path path) {
        return Optional.ofNullable(indexItems.get(path));
    }

    private static boolean ignore(final Path relativePath) {
        if (relativePath.startsWith(Path.of(".prick"))) {
            return false;
        }
        return true;
    }

    private static Collector<IndexItem, SortedMap<Path, IndexItem>, SortedMap<Path, IndexItem>> toSortedMap() {
        return Collector.of(
                TreeMap::new,
                (SortedMap<Path, IndexItem> map, IndexItem indexItem) -> map.put(indexItem.getPath(), indexItem),
                (a, b) -> { a.putAll(b); return a;});
    }
}
