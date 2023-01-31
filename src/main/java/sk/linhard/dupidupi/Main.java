package sk.linhard.dupidupi;


import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "dupidupi", mixinStandardHelpOptions = true, version = "1.0",
        description = "Deduplicates a set of files defined by given configuration")
@Slf4j
public class Main implements Callable<Integer> {

    @Option(names = {"-c", "--config"}, paramLabel = "CONFIG", description = "the configuration file", required = true)
    File configFile;

    @Option(names = {"-w", "--walk-only"}, description = "Only walk the file system")
    Boolean walkOnly;

    public static void main(String... args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            var config = Config.load(configFile);
            if (walkOnly != null) {
                config.setWalkOnly(walkOnly);
            }
            log.info("Looking for files in");
            for (var root : config.getRoots()) {
                log.info("  " + root);
            }
            log.info("Ignoring");
            for (var ignore : config.getIgnore()) {
                log.info("  " + ignore);
            }
            Walker w = new Walker(config.getRootPaths(), config.getIgnorePaths());

            FileItemSizeSorter sizeSorter = new FileItemSizeSorter();
            w.run(sizeSorter);
            int n = sizeSorter.numSizeBuckets();
            log.info("Found {} files with {} different sizes", sizeSorter.numFiles(), n);

            var results = new Deduper(sizeSorter, config).run();

            log.info("Done sorting. Found {} duplicates in {} duplicate sets, total {} bytes duplicated",
                    results.numDuplicates(), results.duplicates().size(), results.bytesDuplicated());

            File reportFile = new File(config.ensureOutputDir(), "report" + config.getReportType().getExtension());
            log.info("Generating duplicate report to {}", reportFile);
            ReportGenerator.Factory.create(config.getReportType(), reportFile, results).generate();
            return 0;
        } catch (Exception e) {
            log.error("ERROR: {}", e.getMessage(), e);
            return 1;
        }
    }


}
