package com.github.happylynx.prick.lib.model.model2

import java.nio.file.Path
import java.util.LinkedList
import java.util.Queue
import java.util.Spliterator
import java.util.Spliterators
import java.util.Stack
import java.util.stream.Stream
import java.util.stream.StreamSupport

object AddDirectoryBounds {

    fun add(inStream: Stream<IndexItem>): Stream<IndexStreamItem> {
        val inSpliterator = inStream.spliterator()
        val iterator: Iterator<IndexStreamItem> = object : Iterator<IndexStreamItem> {

            val inputStack = Stack<IndexStreamItem.DirEnd>()
            val outputQueue: Queue<IndexStreamItem> = LinkedList<IndexStreamItem>()
            lateinit var itemRead: IndexItem

            init {
                // artificial root added
                inputStack.push(IndexStreamItem.DirEnd(Path.of("")))
                outputQueue.add(IndexStreamItem.DirBegin(Path.of("")))
            }

            override fun hasNext(): Boolean {
                if (outputQueue.isNotEmpty()) {
                    return true
                }
                computeNext()
                return outputQueue.isNotEmpty()
            }

            override fun next(): IndexStreamItem {
                if (hasNext()) {
                    return outputQueue.remove()
                } else {
                    throw IllegalStateException()
                }
            }

            private fun computeNext() {
                assert(outputQueue.isEmpty())
                val read = inSpliterator.tryAdvance { itemRead = it }
                if (!read) {
                    outputQueue.addAll(inputStack.reversed())
                    inputStack.clear()
                    return
                }
                if ((itemRead.path.parent ?: Path.of("")) == inputStack.peek().path) {
                    addReadItem()
                    return
                }
                assert(!itemRead.path.startsWith(inputStack.peek().path))
                while (!itemRead.path.startsWith(inputStack.peek().path)) {
                    outputQueue.add(inputStack.pop())
                }
                addReadItem()
            }

            private fun begin(dirItem: IndexItem.DirectoryIndexItem): IndexStreamItem.DirBegin {
                return IndexStreamItem.DirBegin(dirItem.path)
            }

            private fun end(dirItem: IndexItem.DirectoryIndexItem): IndexStreamItem.DirEnd {
                return IndexStreamItem.DirEnd(dirItem.path)
            }

            private fun addReadItem() {
                when (itemRead) {
                    is IndexItem.DirectoryIndexItem -> {
                        outputQueue.add(begin(itemRead as IndexItem.DirectoryIndexItem))
                        inputStack.push(end(itemRead as IndexItem.DirectoryIndexItem))
                    }
                    is IndexItem.FileIndexItem,
                    is IndexItem.SymlinkIndexItem -> {
                        outputQueue.add(itemRead as IndexStreamItem)
                    }
                }
            }

        }
        return StreamSupport.stream(
                Spliterators.spliterator(iterator, Long.MAX_VALUE, Spliterator.ORDERED),
                false)
    }
}