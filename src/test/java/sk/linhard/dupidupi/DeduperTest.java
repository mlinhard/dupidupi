package sk.linhard.dupidupi;

import com.google.common.collect.ImmutableList;
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

        var config = new Config()
                .setResumable(false)
                .setOutputDir(tempDir.getAbsolutePath())
                .setMaxOpenFiles(10)
                .setBufferSize(32);

        var results = new Deduper(sizeSorter, config).run();

        assertExpectedResult(results);
    }

    @Test
    void run_resume_noProgressLog(@TempDir File tempDir) {
        Walker walker = new Walker(
                List.of(Path.of(resPath("testdir/example"))),
                List.of());

        FileItemSizeSorter sizeSorter = new FileItemSizeSorter();
        walker.run(sizeSorter);

        var config = new Config()
                .setResumable(true)
                .setOutputDir(tempDir.getAbsolutePath())
                .setMaxOpenFiles(10)
                .setBufferSize(32);

        new WalkFileSerializer().store(sizeSorter, config.walkFilePath());

        var results = new Deduper(sizeSorter, config).run();

        assertExpectedResult(results);
    }


    @Test
    void run_resume_progressLog(@TempDir File tempDir) {
        Walker walker = new Walker(
                List.of(Path.of(resPath("testdir/example"))),
                List.of());

        FileItemSizeSorter sizeSorter = new FileItemSizeSorter();
        walker.run(sizeSorter);

        var config = new Config()
                .setResumable(true)
                .setOutputDir(tempDir.getAbsolutePath())
                .setMaxOpenFiles(10)
                .setBufferSize(32);

        new WalkFileSerializer().store(sizeSorter, config.walkFilePath());

        var plWriter = new ProgressLogWriter(config.progressLogPath());
        plWriter.addDuplicateBucket(new ImmutableFileBucket(ImmutableList.of(
                new FileItem(resPath("testdir/example/01.txt"), 0L),
                new FileItem(resPath("testdir/example/02.txt"), 0L)
        )));
        plWriter.addSizeBucketCompletion(0L);
        plWriter.addDuplicateBucket(new ImmutableFileBucket(ImmutableList.of(
                new FileItem(resPath("testdir/example/03.txt"), 1L),
                new FileItem(resPath("testdir/example/04.txt"), 1L)
        )));
        plWriter.addSizeBucketCompletion(1L);

        var results = new Deduper(sizeSorter, config).run();

        assertExpectedResult(results);
    }

    private void assertExpectedResult(ResultRepository results) {
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
                .map(FileItem::path)
                .collect(Collectors.toList());
    }
}
