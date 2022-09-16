package sk.linhard.dupidupi;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sk.linhard.dupidupi.ResourceUtil.resPath;


public class ConfigTest {

    @Test
    void load() {
        File testConfigFile = new File(resPath("test-config.json"));
        Config cfg = Config.load(testConfigFile.getAbsoluteFile());
        assertEquals(2, cfg.getRoots().size());
        assertEquals("testdir/root1", cfg.getRoots().get(0));
        assertEquals("testdir/root2", cfg.getRoots().get(1));
        assertEquals(2, cfg.getIgnore().size());
        assertEquals("testdir/root2/f", cfg.getIgnore().get(0));
        assertEquals("c", cfg.getIgnore().get(1));
        assertEquals(20, cfg.getMaxOpenFiles());
        assertEquals(8, cfg.getBufferSize());
    }
}
