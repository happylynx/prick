package com.github.happylynx.prick.lib.commands;

import com.github.happylynx.prick.lib.model.*;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class IndexToTree {

    private final Stack<TreeItem> stack = new Stack<>();

    private IndexToTree() {}

    public static HashId convert(Index index) {
        final IndexToTree indexToTree = new IndexToTree();
        index.getItems().stream().forEachOrdered(indexToTree::nextItem);
        indexToTree.finish();
        return indexToTree.getResult();
    }

    private void finish() {
        while (stack.size() > 1) {
            final List<TreeItem> dirItems = extractLastDirItems();
            final TreeHash treeHash = computeNStoreTree(dirItems);
            stack.push(treeHash);
        }
    }

    private HashId getResult() {
        if (stack.isEmpty()) {
            return ObjectStorage.store("", pri);
        }
        return stack.pop().getHash();
    }

    private void nextItem(IndexItem item) {
        stack.push(item);
        while (directoryFinished()) {
            storeDirectory();
        }
    }

    private void storeDirectory() {
        final TreeItem lastItem = stack.pop();
        final List<TreeItem> completedDirItems = extractLastDirItems();
        final TreeHash tree = computeNStoreTree(completedDirItems);
        stack.push(tree);
        stack.push(lastItem);
    }

    private TreeHash computeNStoreTree(List<TreeItem> items) {
        final String treeFileContent = items.stream()
                .map(FileFormats::createTreeLine)
                .collect(Collectors.joining(FileFormats.LINE_SEPARATOR));
        final HashId treeHash = ObjectStorage.store(treeFileContent);
        return new TreeHash(treeHash, items.get(0).getPath().getParent());
    }

    private List<TreeItem> extractLastDirItems() {
        final ArrayList<TreeItem> result = new ArrayList<>();
        final Path parentDir = stack.peek().getPath().getParent();
        while (stack.peek().getPath().getParent().equals(parentDir)) {
            result.add(stack.pop());
        }
        Collections.reverse(result);
        return result;
    }

    private boolean directoryFinished() {
        if (stack.size() < 2) {
            return false;
        }
        final Path lastDir = stack.get(stack.size() - 1).getPath().getParent();
        final Path previousDir = stack.get(stack.size() - 2).getPath().getParent();
        return !lastDir.equals(previousDir) && !lastDir.startsWith(previousDir);

    }
}
