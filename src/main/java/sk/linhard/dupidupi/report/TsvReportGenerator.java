package sk.linhard.dupidupi.report;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import sk.linhard.dupidupi.ReportGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.text.DecimalFormat;

@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TsvReportGenerator implements ReportGenerator {

    File reportPath;
    Report report;

    public void generate() {
        try (PrintWriter w = new PrintWriter(new FileOutputStream(reportPath))) {
            DecimalFormat df = new DecimalFormat("#,###");
            for (ReportItem item : report.items()) {
                w.print(item.original());
                w.print("\t");
                w.print(ReportItem.ReportItemType.FILE.equals(item.type()) ? "F" : "D");
                w.print("\t");
                w.print(df.format(item.numBytesDuplicated()));
                w.print("\t");
                w.println(String.join(":", item.duplicates()));
            }
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }
}
