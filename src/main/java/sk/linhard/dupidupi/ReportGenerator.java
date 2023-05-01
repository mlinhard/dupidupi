package sk.linhard.dupidupi;

import java.io.File;

public interface ReportGenerator {

    void generate();

    class Factory {
        public static ReportGenerator create(Config.ReportType type, File reportPath, ResultRepository results) {
            return switch (type) {
                case JSON -> new JsonReportGenerator(reportPath, results);
                case TEXT -> throw new UnsupportedOperationException("Text not yet supported");
                case HTML -> new OldHtmlReportGenerator(reportPath, results);
                case TSV -> new TsvGzReportGenerator(reportPath, results);
            };
        }
    }
}
