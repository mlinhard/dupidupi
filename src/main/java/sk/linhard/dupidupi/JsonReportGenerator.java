package sk.linhard.dupidupi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JsonReportGenerator implements ReportGenerator {

    File reportPath;
    ResultRepository results;

    public void generate() {
        ObjectMapper mapper = new ObjectMapper();
        try (FileOutputStream out = new FileOutputStream(reportPath)) {
            mapper.writeValue(out, results);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
