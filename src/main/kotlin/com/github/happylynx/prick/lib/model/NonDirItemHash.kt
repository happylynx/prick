package com.github.happylynx.prick.lib.model

import java.nio.file.Path

open class NonDirItemHash(
        override val path: Path,
        override val type: FsEntryType,
        override val hash: HashId
) : TreeItem