package sk.linhard.dupidupi;


import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkState;

@Command(name = "dupidupi", mixinStandardHelpOptions = true, version = "1.0", description = "Deduplicate a set of files")
@Slf4j
public class Main implements Callable<Integer> {

    @Option(names = {"-c", "--config"}, paramLabel = "CONFIG", description = "the configuration file")
    File configFile;
    @Option(names = {"-w", "--walk-only"}, description = "Only walk the file system")
    Boolean walkOnly;
    @Option(names = {"--resumable"}, description = "Make the processing resumable. Will create progress log in output folder")
    Boolean resumable;
    @Option(names = {"--ignore"}, description = "A folder (possibly more than one) to ignore")
    List<String> ignoredFolders;
    @Option(names = {"--max-open-files"}, description = "Maximum open files during size-bucket sorting")
    Integer maxOpenFiles;
    @Option(names = {"--buffer-size"}, description = "Buffer size to use when sorting size-buckets")
    Integer bufferSize;
    @Option(names = {"--report-type"}, description = "Report type")
    Config.ReportType reportType;
    @Option(names = {"-o", "--output-folder"}, description = "Output folder")
    String outputFolder;
    @CommandLine.Parameters(paramLabel = "ROOT_FOLDER", description = "The folder (possibly more than one) to deduplicate")
    List<String> rootFolders;


    public static void main(String... args) {
        int exitCode = new CommandLine(new Main()).setCaseInsensitiveEnumValuesAllowed(true).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            var config = prepareConfig();
            log.info("Deduplicating files in\n   {}", String.join("\n   ", config.getRoots()));
            log.info("Ignoring files in\n   {}", String.join("\n   ", config.getIgnore()));
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

    private Config prepareConfig() {
        var config = Config.load(configFile);
        if (rootFolders != null && !rootFolders.isEmpty()) {
            config.getRoots().addAll(rootFolders);
        }
        checkState(!config.getRoots().isEmpty(), "No folders to deduplicate");
        if (ignoredFolders != null && !ignoredFolders.isEmpty()) {
            config.getIgnore().addAll(ignoredFolders);
        }
        if (walkOnly != null) {
            config.setWalkOnly(walkOnly);
        }
        if (bufferSize != null) {
            config.setBufferSize(bufferSize);
        }
        if (maxOpenFiles != null) {
            config.setMaxOpenFiles(maxOpenFiles);
        }
        if (resumable != null) {
            config.setResumable(resumable);
        }
        if (reportType != null) {
            config.setReportType(reportType);
        }
        if (outputFolder != null) {
            config.setOutputDir(outputFolder);
        }
        return config;
    }
}
