package sk.linhard.dupidupi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@Data
@FieldDefaults(level = PRIVATE)
@Accessors(chain = true)
public class Config {
    List<String> roots = new ArrayList<>();
    List<String> ignore = new ArrayList<>();
    int maxOpenFiles = 100;
    int bufferSize = 256;
    String outputDir = "output";
    boolean walkOnly = false;
    boolean resumable = false;
    ReportType reportType = ReportType.TSV;

    public List<Path> getRootPaths() {
        return roots.stream()
                .map(File::new)
                .map(File::toPath)
                .collect(Collectors.toList());
    }

    public List<Path> getIgnorePaths() {
        return ignore.stream()
                .map(File::new)
                .map(File::toPath)
                .collect(Collectors.toList());
    }

    public static Config load(File path) {
        if (path == null) {
            return new Config(); // return default config
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(path, Config.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public File ensureOutputDir() {
        File outputDir = new File(this.outputDir);
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new RuntimeException("Couldn't create output directory " + outputDir.getAbsolutePath());
        }
        return outputDir;
    }

    public File walkFilePath() {
        return new File(ensureOutputDir(), "walk.tsv.gz");
    }

    public File progressLogPath() {
        return new File(ensureOutputDir(), "progress-log.tsv");
    }

    public File progressLogInputPath() {
        return new File(ensureOutputDir(), "progress-log-input.tsv");
    }

    @FieldDefaults(level = PRIVATE, makeFinal = true)
    @AllArgsConstructor
    public enum ReportType {
        JSON(".json"),
        TEXT(".txt"),
        HTML(".html"),
        TSV(".tsv.gz");

        @Getter
        String extension;
    }
}
