package sk.linhard.dupidupi;


import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import sk.linhard.dupidupi.report.HtmlReportGenerator;
import sk.linhard.dupidupi.report.ReportComputer;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "tsvgz2html", mixinStandardHelpOptions = true, version = "1.0",
        description = "Converts a TSV report to HTML report")
@Slf4j
public class TsvGz2Html implements Callable<Integer> {

    @CommandLine.Parameters
    File tsvReport;
    @CommandLine.Parameters
    File htmlReport;
    @CommandLine.Option(names = {"-P", "--prefer"}, description = "Preferred path for original file")
    List<String> preferredPaths;
    @CommandLine.Option(names = {"-S", "--sort-by-size"}, defaultValue = "false", description = "Preferred path for original file")
    boolean sortBySize;

    public static void main(String... args) {
        int exitCode = new CommandLine(new TsvGz2Html()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            log.info("Converting report");
            log.info("TSV Input: {}", tsvReport.getAbsolutePath());
            log.info("HTML Output: {}", htmlReport.getAbsolutePath());
            try (TsvGzReportReader tsvGzReportReader = new TsvGzReportReader(tsvReport)) {
                var results = tsvGzReportReader.parseResults();
                var report = new ReportComputer(results, sortBySize, preferredPaths).compute();
                var reportGenerator = new HtmlReportGenerator(htmlReport, report);
                reportGenerator.generate();
            }
            return 0;
        } catch (Exception e) {
            log.error("ERROR: {}", e.getMessage(), e);
            return 1;
        }
    }
}
