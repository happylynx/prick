package com.github.happylynx.prick.lib.model

import java.nio.file.Path

interface TreeItem {
    /**
     * relative to the prick root
     */
    val path: Path
    val type: FsEntryType
    val hash: HashId
}
