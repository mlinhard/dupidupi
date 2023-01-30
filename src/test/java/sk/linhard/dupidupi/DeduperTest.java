package sk.linhard.dupidupi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static sk.linhard.dupidupi.ResourceUtil.resPath;

public class DeduperTest {

    @Test
    void run(@TempDir File tempDir) {
        Walker walker = new Walker(
                List.of(Path.of(resPath("testdir/example"))),
                List.of());

        FileItemSizeSorter sizeSorter = new FileItemSizeSorter();
        walker.run(sizeSorter);
        Deduper deduper = new Deduper();

        var config = new Config()
                .setOutputDir(tempDir.getAbsolutePath())
                .setMaxOpenFiles(10)
                .setBufferSize(32);

        var results = deduper.run(sizeSorter, config);

        List<FileBucket> duplicates = results.duplicates();

        assertThat(duplicates).hasSize(3);

        Map<Long, FileBucket> bucketsByFileSize = duplicates.stream()
                .collect(Collectors.toMap(FileBucket::fileSize, identity()));

        FileBucket size0 = bucketsByFileSize.get(0L);
        FileBucket size1 = bucketsByFileSize.get(1L);
        FileBucket size2 = bucketsByFileSize.get(2L);

        assertThat(size0).isNotNull();
        assertThat(size0.getFiles()).hasSize(2);
        assertThat(filePaths(size0)).contains(
                resPath("testdir/example/01.txt"),
                resPath("testdir/example/02.txt"));

        assertThat(size1).isNotNull();
        assertThat(size1.getFiles()).hasSize(2);
        assertThat(filePaths(size1)).contains(
                resPath("testdir/example/03.txt"),
                resPath("testdir/example/04.txt"));

        assertThat(size2).isNotNull();
        assertThat(size2.getFiles()).hasSize(2);
        assertThat(filePaths(size2)).contains(
                resPath("testdir/example/08.txt"),
                resPath("testdir/example/09.txt"));
    }

    private List<String> filePaths(FileBucket bucket) {
        return bucket.getFiles().stream()
                .map(FileItem::getPath)
                .collect(Collectors.toList());
    }
}
