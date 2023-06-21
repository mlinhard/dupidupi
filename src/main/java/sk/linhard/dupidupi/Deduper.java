package sk.linhard.dupidupi;

import com.google.common.io.Files;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static sk.linhard.dupidupi.ErrorFormatter.format;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class Deduper {

    final FileItemSizeSorter sizeSorter;
    final Config config;
    ResultRepository resultRepository;

    public ResultRepository run() {
        try {
            try (var pLogReader = createProgressLogInput();
                 var pLogWriter = createProgressLog()) {
                runInternal(pLogReader, pLogWriter);
            }
            return createResultRepository();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ResultRepository createResultRepository() {
        if (resultRepository != null) {
            return resultRepository;
        } else {
            var finishedLogReader = new ProgressLogReader(config.progressLogPath());
            return finishedLogReader.parseResultsFromFinishedLog(sizeSorter.getBucketFileSizes());
        }
    }

    private ProgressLogInput createProgressLogInput() throws IOException {
        if (config.isResumable() && canResume()) {
            return new ProgressLogReader(config.progressLogInputPath());
        }
        return new EmptyProgressLogInput();
    }

    private ProgressLog createProgressLog() {
        if (config.isResumable()) {
            return new ProgressLogWriter(config.progressLogPath());
        } else {
            resultRepository = new ResultRepository();
            return resultRepository;
        }
    }

    private void runInternal(ProgressLogInput pLogReader, ProgressLog pLogWriter) {
        int n = sizeSorter.numSizeBuckets();

        try (var fileChannelRepository = new FileChannelRepository(config.getMaxOpenFiles(), config.getBufferSize())) {

            var prefixSorter = new FileItemPrefixSorter(pLogWriter, fileChannelRepository);

            var allSizeBuckets = sizeSorter.streamSizeBuckets().iterator();
            var allProcessedSizeBuckets = pLogReader.iterator();

            int i = 1;
            while (allSizeBuckets.hasNext()) {
                var sizeBucket = allSizeBuckets.next();
                if (allProcessedSizeBuckets.hasNext()) {
                    var processedSizeBucket = allProcessedSizeBuckets.next();
                    checkState(processedSizeBucket.fileSize() == sizeBucket.fileSize(),
                            format("The {}. processed size bucket file size {} doesn't correspond to expected {}",
                                    i, processedSizeBucket.fileSize(), sizeBucket.fileSize()));
                    log.info("Skipping size-{} bucket with {} files ({}/{})", sizeBucket.fileSize(), sizeBucket.size(), i, n);
                    for (var dupBucket : processedSizeBucket.duplicates()) {
                        pLogWriter.addDuplicateBucket(dupBucket);
                    }
                } else {
                    if (!sizeBucket.isSingleton() && sizeBucket.fileSize() != 0) {
                        log.info("Sorting size-{} bucket with {} files ({}/{})", sizeBucket.fileSize(), sizeBucket.size(), i, n);
                    }
                    prefixSorter.sort(sizeBucket);
                }
                i++;
                pLogWriter.addSizeBucketCompletion(sizeBucket.fileSize());
            }
        }
    }

    private boolean canResume() throws IOException {
        var canResume = compareOrCreateWalkFiles();

        File progressLogInputPath = config.progressLogInputPath();
        if (canResume) {
            Files.copy(config.progressLogPath(), progressLogInputPath);
            return true;
        } else {
            java.nio.file.Files.deleteIfExists(progressLogInputPath.toPath());
            return false;
        }
    }

    private boolean compareOrCreateWalkFiles() {
        WalkFileSerializer wfs = new WalkFileSerializer();
        File walkFile = config.walkFilePath();
        if (walkFile.exists()) {
            if (config.progressLogPath().exists()) {
                var previousSizeSorter = wfs.load(walkFile);
                var walkFilesMatch = previousSizeSorter.equals(sizeSorter);
                if (!walkFilesMatch) {
                    log.warn("Walk file stored at {} doesn't match current file walk", walkFile.getAbsolutePath());
                }
                return walkFilesMatch;
            } else {
                log.info("Progress log {} doesn't exist, not restoring", config.progressLogPath().getAbsolutePath());
                return false;
            }
        } else {
            log.debug("Storing walk file {}", walkFile.getAbsolutePath());
            wfs.store(sizeSorter, walkFile);
            return false;
        }
    }

    private static class EmptyProgressLogInput implements ProgressLogInput {
        @Override
        public Iterator<ProcessedSizeBucket> iterator() {
            return List.<ProcessedSizeBucket>of().iterator();
        }
    }
}
