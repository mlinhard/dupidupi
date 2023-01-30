package sk.linhard.dupidupi;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TsvGzReportGenerator implements ReportGenerator {

    File reportPath;
    ResultRepository results;

    public void generate() {
        try (GzipCompressorOutputStream gzipStream = new GzipCompressorOutputStream(new FileOutputStream(reportPath));
             PrintWriter w = new PrintWriter(gzipStream, false, StandardCharsets.UTF_8)) {
            int bucketId = 0;
            for (FileBucket bucket : results.duplicates()) {
                long size = bucket.fileSize();
                for (String path : bucket.getSortedPaths()) {
                    w.print(bucketId);
                    w.print("\t");
                    w.print(size);
                    w.print("\t");
                    w.print(path);
                    w.print("\n");
                }
                bucketId++;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
