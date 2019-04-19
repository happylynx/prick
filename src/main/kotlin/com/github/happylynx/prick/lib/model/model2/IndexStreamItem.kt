package com.github.happylynx.prick.lib.model.model2

import java.nio.file.Path

interface IndexStreamItem {

    val path: Path

    data class DirBegin(override val path: Path) : IndexStreamItem, IndexToTree.IndexToTreeStackItem
    data class DirEnd(override val path: Path) : IndexStreamItem, IndexToTree.IndexToTreeStackItem
}