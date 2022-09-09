package sk.linhard.dupidupi;

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ConfigTest {

    @Test
    void load() {
        File testConfigFile = new File(Resources.getResource("test-config.json").getPath());
        Config cfg = Config.load(testConfigFile.getAbsoluteFile());
        assertEquals(2, cfg.getRoots().size());
        assertEquals("testdir/root1", cfg.getRoots().get(0));
        assertEquals("testdir/root2", cfg.getRoots().get(1));
        assertEquals(2, cfg.getIgnore().size());
        assertEquals("testdir/root2/f", cfg.getIgnore().get(0));
        assertEquals("c", cfg.getIgnore().get(1));
    }
}
