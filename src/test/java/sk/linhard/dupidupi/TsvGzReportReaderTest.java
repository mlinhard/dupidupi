package sk.linhard.dupidupi;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class TsvGzReportReaderTest {

    static final String TSV = """
            0\t1\ta
            0\t1\tb
            1\t1\tc
            1\t1\td
            2\t3\te
            2\t3\tf
            3\t3\tg
            3\t3\th
            3\t3\ti
            """;

    @Test
    void nextBucket(@TempDir File tempDir) throws IOException {
        var report = new File(tempDir, "report.tsv.gz");
        writeTsvGz(report, TSV);
        var reader = new TsvGzReportReader(report);

        var dup1 = reader.nextBucket();
        var dup2 = reader.nextBucket();
        var dup3 = reader.nextBucket();
        var dup4 = reader.nextBucket();
        var dup5 = reader.nextBucket();

        assertThat(dup1.fileSize()).isEqualTo(1L);
        assertThat(dup2.fileSize()).isEqualTo(1L);
        assertThat(dup1.getSortedPaths()).containsExactly("a", "b");
        assertThat(dup2.getSortedPaths()).containsExactly("c", "d");
        assertThat(dup3.fileSize()).isEqualTo(3L);
        assertThat(dup4.fileSize()).isEqualTo(3L);
        assertThat(dup3.getSortedPaths()).containsExactly("e", "f");
        assertThat(dup4.getSortedPaths()).containsExactly("g", "h", "i");

        assertThat(dup5).isNull();
    }

    @Test
    void parseResults(@TempDir File tempDir) throws IOException {
        var report = new File(tempDir, "report.tsv.gz");
        writeTsvGz(report, TSV);
        var reader = new TsvGzReportReader(report);

        var resultRepository = reader.parseResults();

        assertThat(resultRepository).isNotNull();
        assertThat(resultRepository.duplicates())
                .hasSize(4)
                .extracting(FileBucket::fileSize, FileBucket::getSortedPaths)
                .containsExactly(
                        tuple(1L, List.of("a", "b")),
                        tuple(1L, List.of("c", "d")),
                        tuple(3L, List.of("e", "f")),
                        tuple(3L, List.of("g", "h", "i")));
    }

    private void writeTsvGz(File file, String content) throws IOException {
        try (GzipCompressorOutputStream gzipStream = new GzipCompressorOutputStream(new FileOutputStream(file));
             PrintWriter w = new PrintWriter(gzipStream, false, StandardCharsets.UTF_8)) {
            w.write(content);
        }
    }

}
