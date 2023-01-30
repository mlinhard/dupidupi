package sk.linhard.dupidupi;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

// TODO implement storage and loading from GZipped TSV file, then comparison of file
public class WalkFileSerializer {

    public void store(FileItemSizeSorter sizeBuckets, File walkFile) {
        try (GzipCompressorOutputStream gzipStream = new GzipCompressorOutputStream(new FileOutputStream(walkFile));
             PrintWriter w = new PrintWriter(gzipStream, false, StandardCharsets.UTF_8)) {
            for (FileBucket sizeBucket : sizeBuckets.getSizeBuckets()) {
                long size = sizeBucket.fileSize();
                for (String path : sizeBucket.getSortedPaths()) {
                    w.print(size);
                    w.print("\t");
                    w.print(path);
                    w.print("\n");
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public FileItemSizeSorter load(File walkFile) {
        var sorter = new FileItemSizeSorter();
        try (GzipCompressorInputStream gzipStream = new GzipCompressorInputStream(new FileInputStream(walkFile));
             BufferedReader reader = new BufferedReader(new InputStreamReader(gzipStream, StandardCharsets.UTF_8))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                var tabIdx = line.indexOf('\t');
                var size = Long.parseLong(line.substring(0, tabIdx));
                var path = line.substring(tabIdx + 1);
                sorter.accept(new FileItem(path, size));
            }

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sorter;
    }
}
