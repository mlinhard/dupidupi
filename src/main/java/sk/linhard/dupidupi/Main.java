package sk.linhard.dupidupi;


import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static java.util.Comparator.comparing;

@Command(name = "dupidupi", mixinStandardHelpOptions = true, version = "1.0",
        description = "Deduplicates a set of files defined by given configuration")
@Slf4j
public class Main implements Callable<Integer> {

    @Option(names = {"-c", "--config"}, paramLabel = "CONFIG", description = "the configuration file", required = true)
    File configFile;

    public static void main(String... args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            var config = Config.load(configFile);
            log.info("Looking for files in");
            for (var root : config.getRoots()) {
                log.info("  " + root);
            }
            log.info("Ignoring");
            for (var ignore : config.getIgnore()) {
                log.info("  " + ignore);
            }
            Walker w = new Walker(config.getRootPaths(), config.getIgnorePaths());
            Deduper deduper = new Deduper();
            var results = deduper.run(w, config.getMaxOpenFiles(), config.getBufferSize());

            List<FileBucket> duplicates = results.duplicates();
            Collections.sort(duplicates, comparing(FileBucket::fileSize));

            log.info("Done sorting. Found {} duplicate sets", duplicates.size());

            int i = 0;
            for (FileBucket duplicate : duplicates) {
                log.info("Set {} size {}", i, duplicate.fileSize());
                for (FileItem file : duplicate.getFiles()) {
                    log.info("    {}", file.getPath());
                }
                i++;
            }

            return 0;
        } catch (Exception e) {
            log.error("ERROR: {}", e.getMessage(), e);
            return 1;
        }
    }
}
