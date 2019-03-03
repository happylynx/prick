package com.github.happylynx.prick.cli;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Tmp {
    public static void main(String[] args) throws IOException {
        final var tmp = new Tmp();
        tmp.m("");
    }

    private static void demoCompletableFuture() {
        final CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.complete("foo");
        completableFuture.thenAccept(System.out::println);
    }

    private static void readFile() throws IOException {
        final Path path = Path.of("fileToRead.txt");
        try (final SeekableByteChannel byteChannel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            final ByteBuffer byteBuffer = ByteBuffer.allocate(8);
            int readBytes = 0;
            readBytes = byteChannel.read(byteBuffer);
            while (readBytes > 0) {
                byteBuffer.flip();
                final String readString = new String(byteBuffer.array(), 0, readBytes, StandardCharsets.UTF_8);
                System.out.println("Read string: '" + readString + "'");
                byteBuffer.rewind();
                readBytes = byteChannel.read(byteBuffer);
            }
        }
    }

    private static void writeFile() throws IOException {
        final Path path = Path.of("tmpFile.txt");
        Files.delete(path);
        try (SeekableByteChannel byteChannel = Files.newByteChannel(
                path,
                EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))) {
            final ByteBuffer byteBuffer = ByteBuffer.allocate(32);
            byteBuffer.put("ahoj".getBytes(StandardCharsets.UTF_8));
//            byteBuffer.put("ropuchamalaropuchamalaropuchamala".getBytes(StandardCharsets.UTF_8));
            byteBuffer.flip();
            byteChannel.write(byteBuffer);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void m(String s) {
        System.out.println("string");
    }

    private void m(int i) {
        System.out.println("int");
    }
}
