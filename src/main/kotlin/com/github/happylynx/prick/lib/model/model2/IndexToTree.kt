package com.github.happylynx.prick.lib.model.model2

import com.github.happylynx.prick.lib.commands.FileFormats
import com.github.happylynx.prick.lib.commands.ObjectStorage
import com.github.happylynx.prick.lib.commands.PrickContext
import com.github.happylynx.prick.lib.model.HashId
import com.github.happylynx.prick.lib.model.Index
import java.lang.IllegalStateException
import java.lang.IndexOutOfBoundsException
import java.nio.file.Path
import java.util.*

class IndexToTree(private val ctx: PrickContext) {

    companion object {
        fun run(index: Index, ctx: PrickContext): HashId {
            return IndexToTree(ctx).process(index)
        }
    }

    private val stack: Stack<IndexToTreeStackItem> = Stack()

    private fun process(index: Index): HashId {
        AddDirectoryBounds.add(index.items.values.stream()).forEach(::processItem)
        return finish()
    }

    private fun finish(): HashId {
        return (stack.peek() as TreeItem.TreeDirectory).hash
    }

    private fun processItem(item: IndexStreamItem) {
        val itemToPush: IndexToTreeStackItem = when (item) {
            is IndexStreamItem.DirBegin,
            is IndexStreamItem.DirEnd -> item as IndexToTreeStackItem
            is IndexItem.FileIndexItem -> item.toTreeItem()
            is IndexItem.SymlinkIndexItem -> item.toTreeItem()
            else -> throw IllegalStateException()
        }
        stack.push(itemToPush)
        if (item is IndexStreamItem.DirEnd) {
            wrapDirectory()
        }
    }

    private fun wrapDirectory() {
        val children = mutableSetOf<TreeItem>()
        var dirPath = (stack.pop() as IndexStreamItem.DirEnd).path
        while (stack.peek() !is IndexStreamItem.DirBegin) {
            children.add(stack.pop() as TreeItem)
        }
        var dirBegin = stack.pop() as IndexStreamItem.DirBegin
        assert(dirBegin.path == dirPath)
        val dir: TreeItem.TreeDirectory = storeDirectory(dirPath.fileName.toString(), children)
        stack.push(dir)
    }

    private fun storeDirectory(name: String, items: Set<TreeItem>): TreeItem.TreeDirectory{
        val data = FileFormats.Tree.serialize(items)
        val hash = ObjectStorage.store(data, ctx)
        return TreeItem.TreeDirectory(name, hash)
    }

    interface IndexToTreeStackItem
}