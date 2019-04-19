package com.github.happylynx.prick.lib.commands

import com.github.happylynx.prick.lib.LibUtils
import com.github.happylynx.prick.lib.TreeComparator
import com.github.happylynx.prick.lib.model.FsDirType
import com.github.happylynx.prick.lib.model.HashId
import com.github.happylynx.prick.lib.model.Index
import com.github.happylynx.prick.lib.model.TreeItem
import com.github.happylynx.prick.lib.model.model2.IndexToTree
import com.github.happylynx.prick.lib.model.model2.TreeItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.lang.IllegalStateException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.stream.Collectors

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

    private fun readTreeRecursively(tree: HashId) : List<TreeItem> {
        val result = mutableListOf<TreeItem>()
        val dirsToProcess = Stack<Pair<HashId, Path>>()
        dirsToProcess.push(tree to Path.of(""))
        while (!dirsToProcess.isEmpty()) {
            val (treeHash, path) = dirsToProcess.pop()
            val treeBytes: ByteArray = ObjectStorage.read(treeHash, ctx)
            val items: List<TreeItem> = FileFormats.Tree.parse(treeBytes, path)
            result.addAll(items)
            dirsToProcess.addAll(items.filter { it is TreeItem.TreeDirectory }.map { it.hash to it.path })
        }
        return result
    }

    private fun assertDirectoryContentUnchanged(treeComparator: TreeComparator) {
        val unexpectedChanges = treeComparator.compareTo(ctx.rootDir)
                .filter {  !it.item.startsWith(ctx.files.objects) }
        assertTrue(unexpectedChanges.isEmpty())
    }

    private fun assertTreeContentMatchesToFs(treeHash: HashId) {
        val fsContent = Files.walk(ctx.rootDir).use { it
                .map { path -> ctx.rootDir.relativize(path) }
                .filter { Files.isSameFile(it, Path.of(".")) } // ignore root directory
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
        assertTrue(Files.readString(objectFilePath).isNotEmpty())
    }

    private fun treeToLineSet(rootTree: HashId): Set<String> {
        return readTreeRecursively(rootTree)
                .map {
                    val hash = if (it.type == FsDirType.DIRECTORY) "-" else it.hash.toString()
                    "$hash | ${it.type} | ${it.path.fileName}"
                }.toSet()
    }
}