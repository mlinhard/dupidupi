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
            return 0;
        } catch (Exception e) {
            log.error("ERROR: {}", e.getMessage(), e);
            return 1;
        }
    }
}
