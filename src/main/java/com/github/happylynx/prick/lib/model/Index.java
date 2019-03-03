package com.github.happylynx.prick.lib.model;

import com.github.happylynx.prick.lib.Utils;
import com.github.happylynx.prick.lib.commands.FileFormats;
import com.github.happylynx.prick.lib.commands.PrickContext;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
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

    public String toLines() {
        return indexItems.values().stream()
                .map(IndexItem::toLine)
                .collect(Collectors.joining(FileFormats.LINE_SEPARATOR));
    }

    public static Index fromDisk(Path root, PrickContext ctx, Index oldIndex) {
        final SortedMap<Path, IndexItem> indexItems = Utils.dirStream(root)
                .parallel()
                .map(fsEntry -> IndexItem.fromFsEntry(fsEntry, ctx, oldIndex))
                .collect(toSortedMap());
        return new Index(indexItems, ctx.getRootDir().relativize(root));
    }

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

    private static Collector<IndexItem, SortedMap<Path, IndexItem>, SortedMap<Path, IndexItem>> toSortedMap() {
        return Collector.of(
                TreeMap::new,
                (SortedMap<Path, IndexItem> map, IndexItem indexItem) -> map.put(indexItem.getPath(), indexItem),
                (a, b) -> { a.putAll(b); return a;});
    }
}
