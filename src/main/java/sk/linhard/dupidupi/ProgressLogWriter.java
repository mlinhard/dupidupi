package sk.linhard.dupidupi;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ProgressLogWriter implements AutoCloseable, ProgressLog {

    final File progressLog;
    PrintWriter writer;
    int duplicateBucketId = 0;

    @Override
    public void addDuplicateBucket(FileBucket bucket) {
        ensureWriter();
        long size = bucket.fileSize();
        for (String path : bucket.getSortedPaths()) {
            writer.print(duplicateBucketId);
            writer.print("\t");
            writer.print(size);
            writer.print("\t");
            writer.print(path);
            writer.print("\n");
        }
        writer.print("D\t");
        writer.print(duplicateBucketId);
        writer.print("\n");
        writer.flush();
        duplicateBucketId++;
    }

    @Override
    public void addSizeBucketCompletion(long bucketSize) {
        ensureWriter();
        writer.print("S\t");
        writer.print(bucketSize);
        writer.print("\n");
        writer.flush();
    }

    private void ensureWriter() {
        if (writer == null) {
            try {
                writer = new PrintWriter(progressLog, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}
