package sk.linhard.dupidupi;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class Deduper {

    public ResultRepository run(Walker walker, int maxOpenFiles, int bufferSize) {
        FileItemSizeSorter sizeSorter = new FileItemSizeSorter();
        walker.run(sizeSorter);
        log.info("Found {} files with {} different sizes", sizeSorter.getCount(), sizeSorter.numSizeBuckets());

        FileChannelRepository fileChannelRepository = new FileChannelRepository(maxOpenFiles, bufferSize);
        ResultRepository resultRepository = new ResultRepository();
        FileItemPrefixSorter prefixSorter = new FileItemPrefixSorter(resultRepository, fileChannelRepository);
        for (FileBucket sizeBucket : sizeSorter.getSizeBuckets()) {
            prefixSorter.sort(sizeBucket);
        }
        return resultRepository;
    }
}
