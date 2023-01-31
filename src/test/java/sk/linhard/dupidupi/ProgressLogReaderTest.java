package sk.linhard.dupidupi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class ProgressLogReaderTest {

    static final String LOG_TSV = """
            0\t1\ta
            0\t1\tb
            D\t0
            1\t1\tc
            1\t1\td
            D\t1
            S\t1
            2\t3\te
            2\t3\tf
            D\t2
            3\t3\tg
            3\t3\th
            3\t3\ti
            D\t3
            S\t3
            """;

    @Test
    void nextProcessedSizeBucket(@TempDir File tempDir) throws IOException {
        File log = new File(tempDir, "log.tsv");
        Files.writeString(log.toPath(), LOG_TSV);
        var reader = new ProgressLogReader(log);

        var sizeBucket1 = reader.nextProcessedSizeBucket();
        var sizeBucket2 = reader.nextProcessedSizeBucket();
        var sizeBucket3 = reader.nextProcessedSizeBucket();

        assertThat(sizeBucket1).isNotNull();
        assertThat(sizeBucket1.fileSize()).isEqualTo(1L);
        assertThat(sizeBucket1).hasSize(2);
        var dup1 = sizeBucket1.duplicates().get(0);
        var dup2 = sizeBucket1.duplicates().get(1);
        assertThat(dup1.fileSize()).isEqualTo(1L);
        assertThat(dup2.fileSize()).isEqualTo(1L);
        assertThat(dup1.getSortedPaths()).containsExactly("a", "b");
        assertThat(dup2.getSortedPaths()).containsExactly("c", "d");

        assertThat(sizeBucket2).isNotNull();
        assertThat(sizeBucket2.fileSize()).isEqualTo(3L);
        assertThat(sizeBucket2).hasSize(2);
        var dup3 = sizeBucket2.duplicates().get(0);
        var dup4 = sizeBucket2.duplicates().get(1);
        assertThat(dup3.fileSize()).isEqualTo(3L);
        assertThat(dup4.fileSize()).isEqualTo(3L);
        assertThat(dup3.getSortedPaths()).containsExactly("e", "f");
        assertThat(dup4.getSortedPaths()).containsExactly("g", "h", "i");

        assertThat(sizeBucket3).isNull();
    }

    @Test
    void parseResultsFromFinishedLog(@TempDir File tempDir) throws IOException {
        File log = new File(tempDir, "log.tsv");
        Files.writeString(log.toPath(), LOG_TSV);
        var reader = new ProgressLogReader(log);

        var resultRepository = reader.parseResultsFromFinishedLog(Set.of(1l, 3l));

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

}
