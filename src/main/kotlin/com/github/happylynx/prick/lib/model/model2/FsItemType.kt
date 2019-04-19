package com.github.happylynx.prick.lib.model.model2

// TODO delete?
enum class FsItemType {
    FILE,
    DIRECTORY,
    SYMLINK;

    val code: String = name.substring(0, 1).toLowerCase()
}