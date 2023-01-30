package sk.linhard.dupidupi;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@AllArgsConstructor
@Slf4j
public class Deduper {

    public ResultRepository run(Walker walker, Config config) {
        FileItemSizeSorter sizeSorter = new FileItemSizeSorter();
        walker.run(sizeSorter);
        int n = sizeSorter.numSizeBuckets();
        log.info("Found {} files with {} different sizes", sizeSorter.getCount(), n);

        File outputDir = config.ensureOutputDir();
        WalkFileSerializer wfs = new WalkFileSerializer();
        File walkFile = new File(outputDir, "walk.tsv.gz");
        log.debug("Storing walk file {}", walkFile.getAbsolutePath());
        wfs.store(sizeSorter, walkFile);

        FileChannelRepository fileChannelRepository = new FileChannelRepository(config.getMaxOpenFiles(), config.getBufferSize());
        ResultRepository resultRepository = new ResultRepository();
        FileItemPrefixSorter prefixSorter = new FileItemPrefixSorter(resultRepository, fileChannelRepository);

        int i = 1;
        for (FileBucket sizeBucket : sizeSorter.getSizeBuckets()) {
            if (!sizeBucket.isSingleton() && sizeBucket.fileSize() != 0) {
                log.info("Sorting size-{} bucket with {} files ({}/{})", sizeBucket.fileSize(), sizeBucket.size(), i, n);
            }
            prefixSorter.sort(sizeBucket);
            i++;
        }
        return resultRepository;
    }


}
