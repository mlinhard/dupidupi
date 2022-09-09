package sk.linhard.dupidupi;

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;

public class DeduperTest {

    @Test
    void run() {
        Walker walker = new Walker(
                List.of(Path.of(resPath("testdir/example"))),
                List.of());

        Deduper deduper = new Deduper();
        ResultRepository results = deduper.run(walker);
        List<FileItemBucket> duplicates = results.getDuplicates();

        assertThat(duplicates).hasSize(3);

        Map<Long, FileItemBucket> bucketsByFileSize = duplicates.stream()
                .collect(Collectors.toMap(FileItemBucket::fileSize, identity()));

        FileItemBucket size0 = bucketsByFileSize.get(0L);
        FileItemBucket size1 = bucketsByFileSize.get(1L);
        FileItemBucket size2 = bucketsByFileSize.get(2L);

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

    private String resPath(String resPath) {
        return Resources.getResource(resPath).getPath();
    }

    private List<String> filePaths(FileItemBucket bucket) {
        return bucket.getFiles().stream()
                .map(FileItem::getPath)
                .collect(Collectors.toList());
    }
}
