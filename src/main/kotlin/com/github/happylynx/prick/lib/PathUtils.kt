package com.github.happylynx.prick.lib

import java.nio.file.Path

object PathUtils {

    fun equalsNormalized(a: Path, b: Path): Boolean {
        return a.normalize() == b.normalize()
    }
}