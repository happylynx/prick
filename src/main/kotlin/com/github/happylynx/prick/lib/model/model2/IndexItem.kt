package com.github.happylynx.prick.lib.model.model2

import com.github.happylynx.prick.lib.model.HashId
import java.nio.file.Path
import java.time.Instant

sealed class IndexItem {
    /**
     * relative to the prick root dir
     */
    abstract val path: Path
    abstract val code: String

    data class FileIndexItem(
            override val path: Path,
            override val modificationInstant: Instant,
            override val hash: HashId) : IndexItem(), WithHash, IndexStreamItem {
        override val code: String = FileIndexItem.code

        fun toTreeItem(): TreeItem.TreeFile {
            return TreeItem.TreeFile(path.fileName.toString(), hash)
        }

        companion object {
            const val code = "f"
        }
    }

    data class SymlinkIndexItem(
            override val path: Path,
            override val modificationInstant: Instant,
            override val hash: HashId) : IndexItem(), WithHash, IndexStreamItem {
        override val code: String = SymlinkIndexItem.code

        fun toTreeItem(): TreeItem.TreeSymlink {
            return TreeItem.TreeSymlink(path.fileName.toString(), hash)
        }

        companion object {
            const val code = "s"
        }
    }

    data class DirectoryIndexItem(override val path: Path) : IndexItem(), WithPath {
        override val code: String = DirectoryIndexItem.code

        companion object {
            const val code = "d"
        }
    }
}