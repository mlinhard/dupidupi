package sk.linhard.dupidupi;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class Deduper {

    public ResultRepository run(Walker walker) {
        FileItemSizeSorter sizeSorter = new FileItemSizeSorter();
        walker.run(sizeSorter);
        log.info("Found {} files with {} different sizes", sizeSorter.getCount(), sizeSorter.numSizeBuckets());

        ResultRepository resultRepository = new ResultRepository();
        FileItemPrefixSorter prefixSorter = new FileItemPrefixSorter(resultRepository);
        for (FileBucket sizeBucket : sizeSorter.getSizeBuckets()) {
            prefixSorter.sort(sizeBucket);
        }
        return resultRepository;
    }
}
