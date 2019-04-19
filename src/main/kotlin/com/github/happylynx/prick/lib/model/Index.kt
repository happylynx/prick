package com.github.happylynx.prick.lib.model

import com.github.happylynx.prick.lib.LibUtils
import com.github.happylynx.prick.lib.commands.FileFormats
import com.github.happylynx.prick.lib.commands.PrickContext
import com.github.happylynx.prick.lib.model.model2.IndexItem
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.stream.Stream

/**
 * Cache of files and directories in the head commit and their caches and modification times
 */
class Index(
        /*
    *
    * index item:
    * * path
    * * mod date (non-dir)
    * * hash     (non-dir)
    * * type
    *
    * tree item:
    * - name
    * - type
    * - hash
    *
    * */

        /**
         * IndexItems are ordered according to their path.
         *
         * Root directory is not included.
         */
        val items: SortedMap<Path, IndexItem>,
        /**
         * relative to the prick root
         */
        private val root: Path) {


    fun combineWith(other: Index): Index {
        if (root == other.root) {
            return other
        }
        if (other.root.startsWith(root)) {
            return combineWithSubtree(other)
        }
        if (root.startsWith(other.root)) {
            return other.combineWithSubtree(this)
        }
        throw RuntimeException("One tree has to be a subtree of the other")
    }

    fun toFile(file: Path) {
        val bytes = FileFormats.Index.serialize(items.values)
        Files.write(file, bytes)
    }

    private fun combineWithSubtree(subtree: Index): Index {
        val subtreePrefix = root.relativize(subtree.root)
        val resultIndexItems = TreeMap(items)
        resultIndexItems.keys.removeIf { path -> path.startsWith(subtreePrefix) }
        resultIndexItems.putAll(subtree.items)
        return Index(resultIndexItems, root)
    }

    operator fun get(path: Path): Optional<IndexItem> {
        return Optional.ofNullable(items[path])
    }

    companion object {

        fun empty(): Index {
            return Index(Collections.emptySortedMap(), Path.of(""))
        }
        
        fun fromFile(file: Path): Index {
            val bytes: ByteArray
            try {
                bytes = Files.readAllBytes(file)
            } catch (e: NoSuchFileException) {
                return empty()
            }
            val items: List<IndexItem> = FileFormats.Index.parse(bytes)
            return fromItemsStream(items.stream(), Path.of(""))
        }

        /**
         * @param relativePath path relative to the prick root dir
         */
        private fun fromItemsStream(itemsStream: Stream<IndexItem>, relativePath: Path): Index {
            val itemsMap: SortedMap<Path, IndexItem> = itemsStream
                    .reduce(
                            TreeMap<Path, IndexItem>(),
                            { map, item -> map[item.path] = item; map },
                            { map1, map2 -> map1.putAll(map2); map1 })
            return Index(itemsMap, relativePath)
        }

        fun fromDisk(/** relative to prick root */ root: Path, ctx: PrickContext, oldIndex: Index): Index {
            val absoluteIndexRoot = ctx.rootDir.resolve(root)
            return Files.walk(absoluteIndexRoot).use {
                val itemsStream: Stream<IndexItem> = it .parallel()
                        .filter { absolutePath -> Files.isSameFile(absolutePath, absoluteIndexRoot) } // remote the root
                        .filter { absolutePath -> ignore(ctx.rootDir.relativize(absolutePath)) }
                        .map { absolutePath -> toIndexItem(absolutePath, ctx, oldIndex) }
                fromItemsStream(itemsStream, root)
            }
        }

        private fun toIndexItem(absolutePath: Path, ctx: PrickContext, oldIndex: Index): IndexItem {
            val fileAttributes =
                    Files.readAttributes(absolutePath, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
            val relativePath = ctx.rootDir.relativize(absolutePath)
            if (fileAttributes.isRegularFile) {
                val oldItem = oldIndex.get(relativePath)
                val hash: HashId = if (
                        oldItem.isPresent
                        && oldItem.get() is IndexItem.FileIndexItem
                        && (oldItem.get() as IndexItem.FileIndexItem).modificationInstant == fileAttributes.lastModifiedTime().toInstant()) {
                    (oldItem.get() as IndexItem.FileIndexItem).hash
                } else {
                    LibUtils.hashFile(absolutePath)
                }
                return IndexItem.FileIndexItem(relativePath, fileAttributes.lastModifiedTime().toInstant(), hash)
            }
            if (fileAttributes.isSymbolicLink) {
                val oldItem = oldIndex.get(relativePath)
                val hash: HashId = if (
                    oldItem.isPresent
                    && oldItem.get() is IndexItem.SymlinkIndexItem
                    && (oldItem.get() as IndexItem.SymlinkIndexItem).modificationInstant == fileAttributes.lastModifiedTime().toInstant()) {
                    (oldItem.get() as IndexItem.SymlinkIndexItem).hash
                } else {
                    LibUtils.hashSymlink(absolutePath)
                }
                return IndexItem.SymlinkIndexItem(relativePath, fileAttributes.lastModifiedTime().toInstant(), hash)
            }
            if (fileAttributes.isDirectory) {
                return IndexItem.DirectoryIndexItem(relativePath)
            }
            throw IllegalStateException("Unexpected FS entry type: '$absolutePath'")
        }

        private fun ignore(relativePath: Path): Boolean {
            return !relativePath.startsWith(Path.of(".prick"))
        }
    }
}
