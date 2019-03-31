package com.github.happylynx.prick.lib.commands

import com.github.happylynx.prick.lib.model.*
import com.github.happylynx.prick.lib.walking.FsNonDirEntryType
import java.lang.IllegalStateException
import java.nio.file.Path

import java.time.Instant
import java.util.stream.Collectors
import java.util.stream.Stream

object FileFormats {

    const val LINE_SEPARATOR = "\n"

    @Deprecated("")
    fun createTreeLine(item: TreeItem): String {
        return String.format(
                "%s\u0000%s\u0000%s",
                item.hash,
                item.type.code,
                item.path.fileName.toString())
    }

    @Deprecated("")
    fun parseTreeLine(line: String): TreeItem {
        throw UnsupportedOperationException()
    }

    @Deprecated("")
    fun createCommit(rootTree: HashId, vararg parents: String): String {
        return (rootTree.toString() + LINE_SEPARATOR
                + parents.joinToString(" ") + LINE_SEPARATOR
                + Instant.now().epochSecond + LINE_SEPARATOR)
    }

    object Tree {

        fun serialize(items: List<TreeItem>): ByteArray {
            return items
                    .map(::serializeLine)
                    .joinToString(LINE_SEPARATOR)
                    .toByteArray()
        }

        private fun serializeLine(item: TreeItem): String {
            return "${item.hash}\u0000${item.type.code}\u0000${item.path.fileName}"
        }

        fun parse(fileContent: ByteArray, dir: Path): List<TreeItem> {
            return String(fileContent)
                    .split(LINE_SEPARATOR)
                    .map { parseLine(it, dir) }
        }


        private fun parseLine(line: String, dir: Path): TreeItem {
            val parts = line.split("\u0000")
            val hash = HashId(parts[0])
            val type = sequenceOf<FsEntryType>(*FsNonDirEntryType.values(), *FsDirType.values())
                    .filter { type -> type.code == parts[1] }
                    .first()
            val path = dir.resolve(parts[2])
            return if (type == FsDirType.DIRECTORY) {
                TreeHash(hash, path)
            } else {
                NonDirItemHash(path, type, hash)
            }
        }
    }
}
