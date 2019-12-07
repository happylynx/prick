package com.github.happylynx.prick.lib

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.security.MessageDigest
import java.time.Instant
import java.util.Arrays
import java.util.stream.Collectors

internal class TreeComparator(originalRoot: Path) {

    /**
     * relative path to item
     */
    private val items: Map<Path, Item>

    init {
        items = Files.walk(originalRoot).use {
            it.map {
                val attributes = getAttributes(it)
                when {
                    attributes.isRegularFile -> Item.File(it, attributes.lastModifiedTime().toInstant())
                    attributes.isDirectory -> Item.Directory(it, attributes.lastModifiedTime().toInstant())
                    else -> throw IllegalStateException()
                }
            }.collect(Collectors.toUnmodifiableMap<Item, Path, Item>(
                    { originalRoot.relativize(it.path).normalize() },
                    { it }
            ))
        }
    }

    fun compareTo(otherRoot: Path): Set<Difference> {
        val remainingOriginal = items.toMutableMap()
        val differences = mutableSetOf<Difference>()
        Files.walk(otherRoot).use { it
            .forEach { other ->
                val otherPath = otherRoot.relativize(other).normalize()
                val originalItem = remainingOriginal.remove(otherPath)
                if (originalItem == null) {
                    differences.add(Difference(otherPath, DiffType.EXTRA_IN_OTHER))
                    return@forEach
                }
                val difference = originalItem.compareTo(other)
                if (difference != null) {
                    differences.add(difference)
                }
            }
        }
        val extraInOriginalDifferences = remainingOriginal.keys.map { Difference(it, DiffType.EXTRA_IN_ORIGINAL) }
        differences.addAll(extraInOriginalDifferences)
        return differences
    }






    private sealed class Item {

        /**
         * absolute path
         */
        abstract val path: Path
        abstract val changeTime: Instant

        abstract fun compareTo(other: Path): Difference?

        internal data class File(override val path: Path, override val changeTime: Instant) : Item() {
            val contentHash: ByteArray by lazy { hashFile(path) }

            private fun hashFile(file: Path): ByteArray {
                val content: ByteArray = Files.readAllBytes(file)
                return MessageDigest.getInstance("SHA-256").digest(content)
            }

            override fun compareTo(other: Path): Difference? {
                val otherAttributes = getAttributes(other)
                if (!otherAttributes.isRegularFile) {
                    return Difference(path, DiffType.WAS_FILE_TYPE_CHANGED)
                }
                val changeTimeSame = changeTime == otherAttributes.lastModifiedTime().toInstant()
                if (!changeTimeSame) {
                    return Difference(path, DiffType.MOD_TIME_CHANGED)
                }
                val contentSame = Arrays.equals(contentHash, hashFile(other))
                if (!contentSame) {
                    return Difference(path, DiffType.CONTENT_CHANGED)
                }
                return null
            }
        }

        internal data class Directory(override val path: Path, override val changeTime: Instant) : Item() {
            override fun compareTo(other: Path): Difference? {
                val otherAttributes = getAttributes(other)
                if (!otherAttributes.isDirectory) {
                    return Difference(path, DiffType.WAS_DIR_TYPE_CHANGED)
                }
                val changeTimeSame = changeTime == otherAttributes.lastModifiedTime().toInstant()
                if (!changeTimeSame) {
                    return Difference(path, DiffType.MOD_TIME_CHANGED)
                }
                return null
            }
        }
    }

    data class Difference(/** Absolute path*/ val item: Path, val type: DiffType)

    enum class DiffType {
        MOD_TIME_CHANGED,
        CONTENT_CHANGED,
        WAS_FILE_TYPE_CHANGED,
        WAS_DIR_TYPE_CHANGED,
        EXTRA_IN_ORIGINAL,
        EXTRA_IN_OTHER
    }

}

private fun getAttributes(path: Path): BasicFileAttributes {
    return Files.readAttributes(path, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
}