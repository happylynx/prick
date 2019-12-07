package com.github.happylynx.prick.lib.commands

import com.github.happylynx.prick.lib.model.HashId
import com.github.happylynx.prick.lib.model.model2.IndexItem
import com.github.happylynx.prick.lib.model.model2.TreeItem
import com.github.happylynx.prick.lib.model.model2.WithHash
import java.nio.file.Path
import java.time.Instant

object FileFormats {

    const val LINE_SEPARATOR = "\n"
    const val ZERO = "\u0000"

    // TODO delete
//    @Deprecated("")
//    fun createTreeLine(item: TreeItem): String {
//        return String.format(
//                "%s\u0000%s\u0000%s",
//                item.hash,
//                item.type.code,
//                item.path.fileName.toString())
//    }
//
//    @Deprecated("")
//    fun parseTreeLine(line: String): TreeItem {
//        throw UnsupportedOperationException()
//    }

    @Deprecated("")
    fun createCommit(rootTree: HashId, vararg parents: String): String {
        return (rootTree.toString() + LINE_SEPARATOR
                + parents.joinToString(" ") + LINE_SEPARATOR
                + Instant.now().epochSecond + LINE_SEPARATOR)
    }

    object Tree {

        fun serialize(items: Set<TreeItem>): ByteArray {
            return items
                    .sortedBy { it.name }
                    .map(::serializeLine)
                    .joinToString(LINE_SEPARATOR)
                    .toByteArray()
        }

        private fun serializeLine(item: TreeItem): String {
            return "${item.hash}\u0000${toCode(item)}\u0000${item.name}"
        }

        fun parse(fileContent: ByteArray): List<TreeItem> {
            return String(fileContent)
                    .split(LINE_SEPARATOR)
                    .filter { it.trim().isNotEmpty() }
                    .map(::parseLine)
        }

        private val classToCode = mapOf(
                TreeItem.TreeFile::class to "f",
                TreeItem.TreeSymlink::class to "s",
                TreeItem.TreeDirectory::class to "d"
        )

        private fun toCode(item: TreeItem): String {
            return classToCode.getOrElse(item::class, throw java.lang.IllegalStateException("Unknown item: '$item'"))
        }

        private fun parseLine(line: String): TreeItem {
            val parts = line.split("\u0000")
            val hash = HashId(parts[0])
            val code = parts[1]
            val name = parts[2]
            val treeItemClass = classToCode.entries
                    .filter { entry -> entry.value == code }
                    .map { it.key }
                    .firstOrNull() ?: throw IllegalStateException("Unknow code '$code'")
            return when (treeItemClass) {
                TreeItem.TreeFile::class -> TreeItem.TreeFile(name, hash)
                TreeItem.TreeSymlink::class -> TreeItem.TreeSymlink(name, hash)
                TreeItem.TreeDirectory::class -> TreeItem.TreeDirectory(name, hash)
                else -> throw java.lang.IllegalStateException("Unknown class '$treeItemClass'")
            }
        }
    }

    object Index {

        fun serialize(index: Collection<IndexItem>): ByteArray {
            return index.map(::serializeLine)
                    .joinToString(LINE_SEPARATOR)
                    .toByteArray()
        }

        private fun serializeLine(item: IndexItem): String {
            return when (item) {
                is IndexItem.DirectoryIndexItem -> "${item.code}$ZERO${item.path}"
                is WithHash -> "${item.code}$ZERO${item.hash}$ZERO${item.modificationInstant}$ZERO${item.path}"
                else -> throw IllegalStateException()
            }
        }

        fun parse(bytes: ByteArray): List<IndexItem> {
            return String(bytes).split(LINE_SEPARATOR)
                    .map(::parseLine)
        }

        private fun parseLine(line: String): IndexItem {
            val parts: List<String> = line.split(ZERO)
            return when (parts[0]) {
                IndexItem.FileIndexItem.code -> parseFileIndexItem(parts)
                IndexItem.SymlinkIndexItem.code -> parseSymlinkIndexItem(parts)
                IndexItem.DirectoryIndexItem.code -> parseDirectoryIndexItem(parts)
                else -> throw IllegalStateException()
            }
        }

        private fun parseDirectoryIndexItem(parts: List<String>): IndexItem.DirectoryIndexItem {
            return IndexItem.DirectoryIndexItem(Path.of(parts[1]))
        }

        private fun parseSymlinkIndexItem(parts: List<String>): IndexItem.SymlinkIndexItem {
            return IndexItem.SymlinkIndexItem(
                    Path.of(parts[3]),
                    Instant.ofEpochSecond(parts[2].toLong()),
                    HashId(parts[1]))
        }

        private fun parseFileIndexItem(parts: List<String>): IndexItem.FileIndexItem {
            return IndexItem.FileIndexItem(
                    Path.of(parts[3]),
                    Instant.ofEpochSecond(parts[2].toLong()),
                    HashId(parts[1]))
        }

    }
}
