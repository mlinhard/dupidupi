package sk.linhard.dupidupi.report;

import org.junit.jupiter.api.Test;
import sk.linhard.dupidupi.FileItem;

import static org.assertj.core.api.Assertions.assertThat;

public class PathTest {

    @Test
    void parent() {
        assertFileParent("/home/a", "/home");
        assertFileParent("/home", "/");
        assertFileParent("/", null);

        assertDirectoryParent("/home/a", "/home");
        assertDirectoryParent("/home", "/");
        assertDirectoryParent("/", null);
    }

    private void assertFileParent(String path, String parent) {
        var file = new FileItem(path, 0l);
        if (parent == null) {
            assertThat(file.getParent()).isNull();
        } else {
            assertThat(file.getParent().path()).isEqualTo(parent);
        }
    }

    private void assertDirectoryParent(String path, String parent) {
        var file = new Directory(path);
        if (parent == null) {
            assertThat(file.getParent()).isNull();
        } else {
            assertThat(file.getParent().path()).isEqualTo(parent);
        }
    }
}
