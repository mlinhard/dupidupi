package sk.linhard.dupidupi;

import com.google.common.io.Files;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

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
        var canUseSavedProgressLog = resumeProgress();
        if (canUseSavedProgressLog && config.isResumable()) {
            return new ProgressLogReader(config.progressLogInputPath());
        } else {
            return new EmptyProgressLogInput();
        }
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

            int i = 1;
            for (FileBucket sizeBucket : sizeSorter.getSizeBuckets()) {
                if (!sizeBucket.isSingleton() && sizeBucket.fileSize() != 0) {
                    log.info("Sorting size-{} bucket with {} files ({}/{})", sizeBucket.fileSize(), sizeBucket.size(), i, n);
                }
                prefixSorter.sort(sizeBucket);
                pLogWriter.addSizeBucketCompletion(sizeBucket.fileSize());
                i++;
            }
        }
    }

    private boolean resumeProgress() throws IOException {
        var walkFilesMatch = compareOrCreateWalkFiles();

        File progressLogInputPath = config.progressLogInputPath();
        if (walkFilesMatch) {
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
            var previousSizeSorter = wfs.load(walkFile);
            var walkFilesMatch = previousSizeSorter.equals(sizeSorter);
            if (!walkFilesMatch) {
                log.warn("Walk file stored at {} doesn't match current file walk", walkFile.getAbsolutePath());
            }
            return walkFilesMatch;
        } else {
            log.debug("Storing walk file {}", walkFile.getAbsolutePath());
            wfs.store(sizeSorter, walkFile);
            return false;
        }
    }

    private static class EmptyProgressLogInput implements ProgressLogInput {
        @Override
        public ProcessedSizeBucket nextProcessedSizeBucket() {
            return null;
        }
    }
}
