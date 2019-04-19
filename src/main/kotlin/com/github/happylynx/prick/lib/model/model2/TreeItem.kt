package com.github.happylynx.prick.lib.model.model2

import com.github.happylynx.prick.lib.model.HashId

sealed class TreeItem: IndexToTree.IndexToTreeStackItem {

    abstract val name: String
    abstract val hash: HashId

    data class TreeFile(override val name: String, override val hash: HashId) : TreeItem()

    data class TreeSymlink(override val name: String, override val hash: HashId) : TreeItem()

    data class TreeDirectory(override val name: String, override val hash: HashId) : TreeItem()
}