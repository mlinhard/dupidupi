package sk.linhard.dupidupi;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FileBucketSerializationUtilTest {

    @Test
    void readJsonl(@TempDir Path tempDir) throws IOException {
        String fullPath = tempDir.toString() + "/a.jsonl";
        Files.write(Path.of(fullPath), """
                {"size":10,"files":["a.txt","b.txt"]}
                {"size":12,"files":["c.txt","d.txt","e.txt"]}""".getBytes(StandardCharsets.UTF_8));

        var fileBuckets = FileBucketSerializationUtil.readJsonl(fullPath);

        assertThat(fileBuckets).hasSize(2);
        assertThat(fileBuckets.get(0).fileSize()).isEqualTo(10l);
        assertThat(fileBuckets.get(0).getSortedPaths()).containsExactly("a.txt", "b.txt");
        assertThat(fileBuckets.get(1).fileSize()).isEqualTo(12);
        assertThat(fileBuckets.get(1).getSortedPaths()).containsExactly("c.txt", "d.txt", "e.txt");
    }

    @Test
    void writeJsonl(@TempDir Path tempDir) throws IOException {
        String fullPath = tempDir.toString() + "/a.jsonl";
        List<FileBucket> fileBuckets = List.of(new ImmutableFileBucket(ImmutableList.of(
                        new FileItem("a.txt", 10l), new FileItem("b.txt", 10l))),
                new ImmutableFileBucket(ImmutableList.of(
                        new FileItem("c.txt", 12l), new FileItem("d.txt", 12l), new FileItem("e.txt", 12l)
                )));

        FileBucketSerializationUtil.writeJsonl(fullPath, fileBuckets);
        String jsonl = Files.readString(Path.of(fullPath), StandardCharsets.UTF_8);

        assertThat(jsonl).isEqualTo("""
                {"size":10,"files":["a.txt","b.txt"]}
                {"size":12,"files":["c.txt","d.txt","e.txt"]}""");
    }
}
