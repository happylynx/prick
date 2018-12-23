package com.github.happylynx.prick.lib;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    private static final int MEGA = 1024 * 1024;

    private Utils() {}

    String hashPath(final Path path) {
        return hashToString(hashPathToBytes(path));
    }

    private byte[] hashPathToBytes(final Path path) {
        try (SeekableByteChannel byteChannel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            return hashByteChannelToBytes(byteChannel);
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    private byte[] hashByteChannelToBytes(ByteChannel byteChannel) throws NoSuchAlgorithmException, IOException {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(2 * MEGA);
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        int read = byteChannel.read(byteBuffer);
        while (read > 0) {
            byteBuffer.flip();
            messageDigest.update(byteBuffer);
            byteBuffer.clear();
            read = byteChannel.read(byteBuffer);
        }
        return messageDigest.digest();
    }

    String hashToString(byte[] binaryHash) {
        final StringBuilder builder = new StringBuilder(binaryHash.length * 2);
        for (byte hashByte : binaryHash) {
            builder.append(Integer.toHexString(0xff & hashByte));
        }
        return builder.toString();
    }
}
