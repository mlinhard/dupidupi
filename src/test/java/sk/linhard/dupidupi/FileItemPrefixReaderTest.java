package sk.linhard.dupidupi;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static sk.linhard.dupidupi.ResourceUtil.resPath;

public class FileItemPrefixReaderTest {

    @Test
    void testReadFile() {
        try (FileItemPrefixReader reader = new FileItemPrefixReader(
                new FileItem(resPath("testdir/example/10.txt"), 3L))) {
            assertThat(reader.nextByte()).isEqualTo(97);
            assertThat(reader.nextByte()).isEqualTo(98);
            assertThat(reader.nextByte()).isEqualTo(99);
            assertThat(reader.nextByte()).isEqualTo(-1);
            assertThat(reader.nextByte()).isEqualTo(-1);
        }
    }
}
