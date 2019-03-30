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
        Files.walk(otherRoot)
                .forEach { other ->
                    val otherPath = otherRoot.relativize(other).normalize()
                    val originalItem = remainingOriginal.remove(otherPath)
                    if (originalItem == null) {
                        differences.add(Difference(otherPath, "Extra in other"))
                        return@forEach
                    }
                    val difference = originalItem.compareTo(other)
                    if (difference != null) {
                        differences.add(difference)
                    }
                }
        val extraInOriginalDifferences = remainingOriginal.keys.map { Difference(it, "Extra in original") }
        differences.addAll(extraInOriginalDifferences)
        return differences
    }






    private sealed class Item {

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
                    return Difference(path, "Original is a file. Other is not.")
                }
                val changeTimeSame = changeTime == otherAttributes.lastModifiedTime().toInstant()
                if (!changeTimeSame) {
                    return Difference(path, "Change times differ")
                }
                val contentSame = Arrays.equals(contentHash, hashFile(other))
                if (!contentSame) {
                    return Difference(path, "Different content")
                }
                return null
            }
        }

        internal data class Directory(override val path: Path, override val changeTime: Instant) : Item() {
            override fun compareTo(other: Path): Difference? {
                val otherAttributes = getAttributes(other)
                if (!otherAttributes.isDirectory) {
                    return Difference(path, "Original is a directory. Other is not.")
                }
                val changeTimeSame = changeTime == otherAttributes.lastModifiedTime().toInstant()
                if (!changeTimeSame) {
                    return Difference(path, "Change times differ")
                }
                return null
            }
        }
    }

    data class Difference(val item: Path, val type: String)

}

private fun getAttributes(path: Path): BasicFileAttributes {
    return Files.readAttributes(path, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
}