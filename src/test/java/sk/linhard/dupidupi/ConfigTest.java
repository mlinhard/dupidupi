package sk.linhard.dupidupi;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigTest {

    @Test
    void load() {
        File testConfigFile = new File("src/test/resources/test-config.json");
        Config cfg = Config.load(testConfigFile.getAbsoluteFile());
        assertEquals(2, cfg.getRoots().size());
        assertEquals("/home/mlinhard/dev/projects/other/dupidupi/src/test/src/main/resources/testdir/root1", cfg.getRoots().get(0));
        assertEquals("/home/mlinhard/dev/projects/other/dupidupi/src/test/src/main/resources/testdir/root2", cfg.getRoots().get(1));
        assertEquals(1, cfg.getIgnore().size());
        assertEquals("/home/mlinhard/dev/projects/other/dupidupi/src/test/src/main/resources/testdir/root2/f", cfg.getRoots().get(0));
    }
}
