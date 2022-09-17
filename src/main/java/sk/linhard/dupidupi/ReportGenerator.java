package sk.linhard.dupidupi;

public interface ReportGenerator {

    void generate();

    class Factory {
        public static ReportGenerator create(Config.ReportType type, String reportPath, ResultRepository results) {
            return switch (type) {
                case JSON -> new JsonReportGenerator(reportPath, results);
                case TEXT -> throw new UnsupportedOperationException("Text not yet supported");
                case HTML -> new HtmlReportGenerator(reportPath, results);
            };
        }
    }
}
