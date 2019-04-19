package com.github.happylynx.prick.lib.model.model2

import java.nio.file.Path

// TODO consider deletion
interface WithPath {
    val path: Path

    val parent: Path
        get() = path.parent
}