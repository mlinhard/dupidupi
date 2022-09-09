package sk.linhard.dupidupi;

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WalkerTest {

    @Test
    void run() throws IOException {
        Walker walker = new Walker(
                List.of(
                        Path.of(resPath("testdir/root1")),
                        Path.of(resPath("testdir/root2"))
                ),
                List.of(
                        Path.of("testdir/root2/f"),
                        Path.of("c")
                )
        );
        List<String> foundPaths = new ArrayList<>();
        walker.run(fileItem -> foundPaths.add(fileItem.getPath()));

        assertThat(foundPaths).hasSize(4);
        assertThat(foundPaths).containsOnly(
                resPath("testdir/root1/a/file1.txt"),
                resPath("testdir/root1/b/file2.txt"),
                resPath("testdir/root2/d/file4.txt"),
                resPath("testdir/root2/e/file5.txt")
        );
    }

    private String resPath(String resPath) {
        return Resources.getResource(resPath).getPath();
    }
}
