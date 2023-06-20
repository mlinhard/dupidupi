package sk.linhard.dupidupi;


import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import sk.linhard.dupidupi.report.ReportComputer;
import sk.linhard.dupidupi.report.TsvReportGenerator;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "tsvgz2tsv", mixinStandardHelpOptions = true, version = "1.0",
        description = "Converts a TSV duplicate report to TSV deletion report")
@Slf4j
public class TsvGz2Tsv implements Callable<Integer> {

    @CommandLine.Parameters
    File tsvInputDuplicateReport;
    @CommandLine.Parameters
    File tsvOutputDeletionReport;
    @CommandLine.Option(names = {"-P", "--prefer"}, description = "Preferred path for original file")
    List<String> preferredPaths;
    @CommandLine.Option(names = {"-S", "--sort-by-size"}, defaultValue = "false", description = "Preferred path for original file")
    boolean sortBySize;

    public static void main(String... args) {
        int exitCode = new CommandLine(new TsvGz2Tsv()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            log.info("Converting duplicate report to deletion report");
            log.info("TSV Input: {}", tsvInputDuplicateReport.getAbsolutePath());
            log.info("TSV Output: {}", tsvOutputDeletionReport.getAbsolutePath());
            try (TsvGzReportReader tsvGzReportReader = new TsvGzReportReader(tsvInputDuplicateReport)) {
                var results = tsvGzReportReader.parseResults();
                var report = new ReportComputer(results, sortBySize, preferredPaths).compute();
                var reportGenerator = new TsvReportGenerator(tsvOutputDeletionReport, report);
                reportGenerator.generate();
            }
            return 0;
        } catch (Exception e) {
            log.error("ERROR: {}", e.getMessage(), e);
            return 1;
        }
    }
}
