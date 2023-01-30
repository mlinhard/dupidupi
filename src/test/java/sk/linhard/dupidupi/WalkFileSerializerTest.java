package sk.linhard.dupidupi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

public class WalkFileSerializerTest {

    @Test
    void serialization(@TempDir File tempDir) {
        var serializer = new WalkFileSerializer();
        var sorter = new FileItemSizeSorter();
        sorter.accept(new FileItem("a", 10));
        sorter.accept(new FileItem("b", 10));
        sorter.accept(new FileItem("c", 20));
        sorter.accept(new FileItem("d", 20));
        sorter.accept(new FileItem("e", 20));
        sorter.accept(new FileItem("f", 30));
        File walkFile = new File(tempDir, "walk.tsv.gz");
        serializer.store(sorter, walkFile);
        var sorted2 = serializer.load(walkFile);

        assertThat(sorted2.numSizeBuckets()).isEqualTo(3);
        assertThat(sorted2.numFiles()).isEqualTo(6);
        var buckets = StreamSupport.stream(sorted2.getSizeBuckets().spliterator(), false).collect(Collectors.toList());
        assertThat(buckets.size()).isEqualTo(3);
        var b10 = buckets.get(0);
        var b20 = buckets.get(1);
        var b30 = buckets.get(2);

        assertThat(b10.fileSize()).isEqualTo(10);
        assertThat(b10.getSortedPaths()).containsExactly("a", "b");
        assertThat(b20.fileSize()).isEqualTo(20);
        assertThat(b20.getSortedPaths()).containsExactly("c", "d", "e");
        assertThat(b30.fileSize()).isEqualTo(30);
        assertThat(b30.getSortedPaths()).containsExactly("f");
    }
}
