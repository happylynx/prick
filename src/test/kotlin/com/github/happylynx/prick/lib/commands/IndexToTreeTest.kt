package com.github.happylynx.prick.lib.commands

import com.github.happylynx.prick.lib.LibUtils
import com.github.happylynx.prick.lib.PathUtils
import com.github.happylynx.prick.lib.TreeComparator
import com.github.happylynx.prick.lib.model.HashId
import com.github.happylynx.prick.lib.model.Index
import com.github.happylynx.prick.lib.model.model2.IndexToTree
import com.github.happylynx.prick.lib.model.model2.TreeItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.LinkedList
import java.util.Queue
import java.util.Spliterator
import java.util.Spliterators
import java.util.Stack
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.stream.StreamSupport

class IndexToTreeTest {

    private lateinit var ctx: PrickContext

    @BeforeEach
    fun initPrickDir(@TempDir dir: Path) {
        InitCommand(dir).run()
        ctx = PrickContext(dir)
    }

    @Test
    fun emptyIndex() {
        val treeComparator = TreeComparator(ctx.rootDir)

        val treeHash: HashId = IndexToTree.run(Index.empty(), ctx)

        assertTreeObjectExists(treeHash)
        assertTreeContentMatchesToFs(treeHash)
        assertDirectoryContentUnchanged(treeComparator)
    }

    @Test
    fun nonEmptyIndex() {
        createSampleContent()
        val index: Index = Index.fromDisk(ctx.rootDir, ctx, Index.empty())
        val treeComparator: TreeComparator = TreeComparator(ctx.rootDir)

        val treeHash: HashId = IndexToTree.run(index, ctx)

        assertTreeObjectExists(treeHash)
        assertTreeContentMatchesToFs(treeHash)
        assertDirectoryContentUnchanged(treeComparator)
    }

    @Test
    fun nonEmptyIndexPreviousCommits() {
        // TODO
    }

    private fun createSampleContent() {
        Files.createDirectories(ctx.rootDir.resolve("dirA").resolve("dirB"))
        Files.write(ctx.rootDir.resolve("emptyFile"), ByteArray(0))
        val dirC = Files.createDirectory(ctx.rootDir.resolve("dirC"))
        Files.writeString(dirC.resolve("foo"), "bar")
        Files.writeString(dirC.resolve("hello"), "world")
    }

    private fun readTreeRecursively(tree: HashId) : Stream<Pair<Path, TreeItem>> {
        val iterator: Iterator<Pair<Path, TreeItem>> = object : Iterator<Pair<Path, TreeItem>> {

            val stack = Stack<Pair<Path, HashId>>()
            val output: Queue<Pair<Path, TreeItem>> = LinkedList()

            init {
                stack.push(Path.of("") to tree)
            }

            override fun hasNext(): Boolean {
                if (output.isNotEmpty()) {
                    return true
                }
                computeNext()
                return output.isNotEmpty()
            }

            override fun next(): Pair<Path, TreeItem> {
                if (!hasNext()) {
                    throw IllegalStateException()
                }
                return output.remove()
            }

            fun computeNext() {
                if (stack.isEmpty()) {
                    return
                }
                val (path, hash) = stack.pop()
                val children = readDirTree(hash).map { path.resolve(it.name) to it }
                stack.addAll(children.filter { it.second is TreeItem.TreeDirectory }.map {it.first to it.second.hash})
                output.addAll(children)
            }
        }
        return StreamSupport.stream(Spliterators.spliterator(iterator, Long.MAX_VALUE, Spliterator.ORDERED), false)
    }

    private fun readDirTree(dirHash: HashId): List<TreeItem> {
        val data = ObjectStorage.read(dirHash, ctx)
        return FileFormats.Tree.parse(data)
    }

    private fun assertDirectoryContentUnchanged(treeComparator: TreeComparator) {
        val hashOfEmpty = LibUtils.hashBytes(ByteArray(0)).toString()
        val unexpectedChanges = treeComparator.compareTo(ctx.rootDir)
                .filter {
                    !PathUtils.equalsNormalized(it.item, ctx.files.objects)
                            && !PathUtils.equalsNormalized(it.item, ctx.files.objects.resolve(hashOfEmpty.substring(0, 2)))
                            && !PathUtils.equalsNormalized(it.item, ctx.files.objects.resolve(hashOfEmpty.substring(0, 2)).resolve(hashOfEmpty))
                            && !(PathUtils.equalsNormalized(it.item, ctx.files.prickDir) && it.type == TreeComparator.DiffType.MOD_TIME_CHANGED)
                }
        assertTrue(unexpectedChanges.isEmpty(), "Unexpected changes: $unexpectedChanges")
    }

    private fun assertTreeContentMatchesToFs(treeHash: HashId) {
        val fsContent = Files.walk(ctx.rootDir).use { it
                .map { path -> ctx.rootDir.relativize(path) }
                .filter { it.normalize() != Path.of("").normalize() } // ignore root directory
                .filter { path -> !path.startsWith(".prick") } // default ignore
                .map(::pathToLine)
                .collect(Collectors.toSet())
        }
        val treeContent = treeToLineSet(treeHash)
        assertEquals(fsContent, treeContent)
    }

    private fun pathToLine(fsPath: Path): String {
        val name = fsPath.fileName
        val hash: String
        val type: String
        when {
            Files.isDirectory(fsPath) -> {
                hash = "-"
                type = "d"
            }
            Files.isRegularFile(fsPath) -> {
                hash = LibUtils.hashFile(fsPath).toString()
                type = "f"
            }
            Files.isSymbolicLink(fsPath) -> {
                hash = LibUtils.hashSymlink(fsPath).toString()
                type = "s"
            }
            else -> throw IllegalStateException()
        }
        return "$hash | $type | $name"
    }

    private fun assertTreeObjectExists(treeHash: HashId) {
        val objectFilePath = ctx.files.objects.resolve(treeHash.toString().substring(0, 2)).resolve(treeHash.toString())
        assertTrue(Files.isRegularFile(objectFilePath))
    }

    private fun treeToLineSet(rootTree: HashId): Set<String> {
        return readTreeRecursively(rootTree)
                .map {
                    val hash = if (it.second is TreeItem.TreeDirectory) "-" else it.second.hash.toString()
                    val type = when(it.second) {
                        is TreeItem.TreeDirectory -> "d"
                        is TreeItem.TreeFile -> "f"
                        is TreeItem.TreeSymlink -> "s"
                    }
                    "$hash | $type | ${it.first}"
                }.collect(Collectors.toSet())
    }
}