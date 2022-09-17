package sk.linhard.dupidupi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
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
public class Config {
    List<String> roots = new ArrayList<>();
    List<String> ignore = new ArrayList<>();
    int maxOpenFiles = 100;
    int bufferSize = 256;
    String report;
    ReportType reportType = ReportType.JSON;

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
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(path, Config.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public enum ReportType {
        JSON,
        TEXT,
        HTML
    }
}
