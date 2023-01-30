package sk.linhard.dupidupi;

import com.google.common.io.Files;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

@AllArgsConstructor
@Slf4j
public class Deduper {


    public ResultRepository run(FileItemSizeSorter sizeSorter, Config config) {
        try {
            var restorable = resumeProgress(sizeSorter, config);

            // TODO here when there is stuff to restore from and it's wanted
            // create real reader
            // otherwise create a empty progress reader
            // if there is a need to store progress then create a real writer
            // otherwise create

            if (restorable) {
                try (var progressLogReader = new ProgressLogReader(config.progressLogInputPath())) {
                    runInternal(sizeSorter, config);
                }
            } else {
                runInternal(sizeSorter, config);
            }

            var finishedLogReader = new ProgressLogReader(config.progressLogPath());
            return finishedLogReader.parseResultsFromFinishedLog(sizeSorter.getBucketFileSizes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void runInternal(FileItemSizeSorter sizeSorter, Config config) {
        int n = sizeSorter.numSizeBuckets();

        try (var fileChannelRepository = new FileChannelRepository(config.getMaxOpenFiles(), config.getBufferSize());
             var progressLogWriter = new ProgressLogWriter(config.progressLogPath())) {

            var prefixSorter = new FileItemPrefixSorter(progressLogWriter, fileChannelRepository);

            int i = 1;
            for (FileBucket sizeBucket : sizeSorter.getSizeBuckets()) {
                if (!sizeBucket.isSingleton() && sizeBucket.fileSize() != 0) {
                    log.info("Sorting size-{} bucket with {} files ({}/{})", sizeBucket.fileSize(), sizeBucket.size(), i, n);
                }
                prefixSorter.sort(sizeBucket);
                progressLogWriter.addSizeBucketCompletion(sizeBucket.fileSize());
                i++;
            }
        }
    }

    private boolean resumeProgress(FileItemSizeSorter sizeSorter, Config config) throws IOException {
        if (!config.isResumable()) {
            return false;
        }
        var walkFilesMatch = compareOrCreateWalkFiles(sizeSorter, config);

        File progressLogInputPath = config.progressLogInputPath();
        if (walkFilesMatch) {
            Files.copy(config.progressLogPath(), progressLogInputPath);
            return true;
        } else {
            java.nio.file.Files.deleteIfExists(progressLogInputPath.toPath());
            return false;
        }
    }

    private boolean compareOrCreateWalkFiles(FileItemSizeSorter sizeSorter, Config config) {
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
}
