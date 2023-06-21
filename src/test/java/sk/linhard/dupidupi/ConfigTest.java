package sk.linhard.dupidupi;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static sk.linhard.dupidupi.ResourceUtil.resPath;


public class ConfigTest {

    @Test
    void load() {
        File testConfigFile = new File(resPath("test-config.json"));
        Config cfg = Config.load(testConfigFile.getAbsoluteFile());
        assertThat(cfg).isNotNull();
        assertThat(cfg.getRoots()).containsExactly("testdir/root1", "testdir/root2");
        assertThat(cfg.getIgnore()).containsExactly("testdir/root2/f", "c");
        assertThat(cfg.getMaxOpenFiles()).isEqualTo(20);
        assertThat(cfg.getBufferSize()).isEqualTo(8);
        assertThat(cfg.getOutputDir()).isEqualTo("output-data");
        assertThat(cfg.isWalkOnly()).isFalse();
        assertThat(cfg.isResumable()).isTrue();
        assertThat(cfg.getReportType()).isEqualTo(Config.ReportType.HTML);
    }

    @Test
    void load_empty() {
        Config cfg = Config.load(null);
        assertThat(cfg).isNotNull();
        assertThat(cfg.getRoots()).isNotNull().isEmpty();
        assertThat(cfg.getIgnore()).isNotNull().isEmpty();
        assertThat(cfg.getMaxOpenFiles()).isEqualTo(100);
        assertThat(cfg.getBufferSize()).isEqualTo(256);
        assertThat(cfg.getOutputDir()).isEqualTo("output");
        assertThat(cfg.isWalkOnly()).isFalse();
        assertThat(cfg.isResumable()).isFalse();
        assertThat(cfg.getReportType()).isEqualTo(Config.ReportType.TSV);
    }
}

