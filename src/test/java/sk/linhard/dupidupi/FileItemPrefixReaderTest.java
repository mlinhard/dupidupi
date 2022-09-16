package sk.linhard.dupidupi;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static sk.linhard.dupidupi.ResourceUtil.resPath;

public class FileItemPrefixReaderTest {

    @Test
    void testReadFile() {
        try (FileChannelRepository fchRepo = new FileChannelRepository(2, 32)) {
            FileItemPrefixReader r1 = resReader("testdir/example/07.txt", fchRepo);
            FileItemPrefixReader r2 = resReader("testdir/example/08.txt", fchRepo);
            FileItemPrefixReader r3 = resReader("testdir/example/10.txt", fchRepo);

            assertThat(r1.nextByte()).isEqualTo(97);
            assertThat(r1.nextByte()).isEqualTo(99);
            assertThat(r1.nextByte()).isEqualTo(-1);
            assertThat(r1.nextByte()).isEqualTo(-1);

            assertThat(r2.nextByte()).isEqualTo(98);
            assertThat(r2.nextByte()).isEqualTo(99);
            assertThat(r2.nextByte()).isEqualTo(-1);
            assertThat(r2.nextByte()).isEqualTo(-1);

            assertThat(r3.nextByte()).isEqualTo(97);
            assertThat(r3.nextByte()).isEqualTo(98);
            assertThat(r3.nextByte()).isEqualTo(99);
            assertThat(r3.nextByte()).isEqualTo(-1);
            assertThat(r3.nextByte()).isEqualTo(-1);
        }
    }

    @Test
    void testReadFile_interleaved() {
        try (FileChannelRepository fchRepo = new FileChannelRepository(2, 32)) {
            FileItemPrefixReader r1 = resReader("testdir/example/07.txt", fchRepo);
            FileItemPrefixReader r2 = resReader("testdir/example/08.txt", fchRepo);
            FileItemPrefixReader r3 = resReader("testdir/example/10.txt", fchRepo);

            assertThat(r1.nextByte()).isEqualTo(97);
            assertThat(r2.nextByte()).isEqualTo(98);
            assertThat(r3.nextByte()).isEqualTo(97);

            assertThat(r1.nextByte()).isEqualTo(99);
            assertThat(r2.nextByte()).isEqualTo(99);
            assertThat(r3.nextByte()).isEqualTo(98);

            assertThat(r1.nextByte()).isEqualTo(-1);
            assertThat(r2.nextByte()).isEqualTo(-1);
            assertThat(r3.nextByte()).isEqualTo(99);

            assertThat(r1.nextByte()).isEqualTo(-1);
            assertThat(r2.nextByte()).isEqualTo(-1);
            assertThat(r3.nextByte()).isEqualTo(-1);
        }
    }

    @Test
    void testReadFile_emptyFile() {
        try (FileChannelRepository fchRepo = new FileChannelRepository(2, 32)) {
            FileItemPrefixReader r1 = resReader("testdir/example/01.txt", fchRepo);
            assertThat(r1.nextByte()).isEqualTo(-1);
        }
    }

    @Test
    void testReadFile_threeBufferLoads(@TempDir Path tempDir) {
        try (FileChannelRepository fchRepo = new FileChannelRepository(2, 4)) {
            FileItemPrefixReader r1 = genReader(tempDir, "01.bin", fchRepo, -1, 0, 1, 2, 127, 128, 255, 256, 100, 200);
            assertThat(r1.nextByte()).isEqualTo(255);
            assertThat(r1.nextByte()).isEqualTo(0);
            assertThat(r1.nextByte()).isEqualTo(1);
            assertThat(r1.nextByte()).isEqualTo(2);
            assertThat(r1.nextByte()).isEqualTo(127);
            assertThat(r1.nextByte()).isEqualTo(128);
            assertThat(r1.nextByte()).isEqualTo(255);
            assertThat(r1.nextByte()).isEqualTo(0);
            assertThat(r1.nextByte()).isEqualTo(100);
            assertThat(r1.nextByte()).isEqualTo(200);
            assertThat(r1.nextByte()).isEqualTo(-1);
        }
    }

    @Test
    void testReadFile_withEviction(@TempDir Path tempDir) {
        try (FileChannelRepository fchRepo = new FileChannelRepository(2, 4)) {
            FileItemPrefixReader r1 = genReader(tempDir, "r1.bin", fchRepo, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            FileItemPrefixReader r2 = genReader(tempDir, "r2.bin", fchRepo, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            FileItemPrefixReader r3 = genReader(tempDir, "r3.bin", fchRepo, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

            for (int i = 0; i < 10; i++) {
                assertThat(r1.nextByte()).isEqualTo(i);
                assertThat(r2.nextByte()).isEqualTo(i);
                assertThat(r3.nextByte()).isEqualTo(i);
            }
        }
    }

    private FileItemPrefixReader resReader(String path, FileChannelRepository fileChannelRepository) {
        return new FileItemPrefixReader(new FileItem(resPath(path), 3L), fileChannelRepository);
    }

    private FileItemPrefixReader genReader(Path tempDir, String fileName, FileChannelRepository fileChannelRepository, int... contents) {
        return new FileItemPrefixReader(new FileItem(generateFile(tempDir, fileName, contents), contents.length), fileChannelRepository);
    }

    private String generateFile(Path tempDir, String fileName, int... contents) {
        try {
            String fullPath = tempDir.toString() + "/" + fileName;
            Files.write(Path.of(fullPath), Bytes.toArray(Ints.asList(contents).stream()
                    .map(i -> (Byte.valueOf(Integer.valueOf(i).byteValue())))
                    .collect(Collectors.toList())));
            return fullPath;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
