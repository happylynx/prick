package com.github.happylynx.prick.lib.commands

import com.github.happylynx.prick.lib.LibUtils
import com.github.happylynx.prick.lib.model.HashId

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption

object ObjectStorage {

    fun store(content: String, ctx: PrickContext): HashId {
        return store(content.toByteArray(StandardCharsets.UTF_8), ctx)
    }

    fun store(bytes: ByteArray, ctx: PrickContext): HashId {
        val hash = LibUtils.hashBytes(bytes)
        val destination = ctx.files.objects
                .resolve(hash.toString().substring(0, 2))
                .resolve(hash.toString())
        if (!Files.isRegularFile(destination, LinkOption.NOFOLLOW_LINKS)) {
            try {
                Files.createDirectories(destination.parent)
                Files.write(destination, bytes, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)
            } catch (ex: IOException) {
                throw RuntimeException(ex)
            }

        }
        return hash
    }

    fun read(hash: HashId, ctx: PrickContext): ByteArray {
        val file = ctx.files.objects.resolve(hash.toString().substring(0, 2)).resolve(hash.toString())
        return Files.readAllBytes(file)
    }
}
