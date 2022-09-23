package sk.linhard.dupidupi;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@AllArgsConstructor
@Slf4j
public class Deduper {

    public ResultRepository run(Walker walker, int maxOpenFiles, int bufferSize, String filesReport) {
        FileItemSizeSorter sizeSorter = new FileItemSizeSorter();
        walker.run(sizeSorter);
        int n = sizeSorter.numSizeBuckets();
        log.info("Found {} files with {} different sizes", sizeSorter.getCount(), n);

        if (filesReport != null) {
//            List<FileBucket> existingBuckets = FileBucketSerializationUtil.readJsonl(filesReport);
//            if (existingBuckets != null) {
//                log.info("File report already exists");
//            }
            log.info("Writing file report to {}", filesReport);
            FileBucketSerializationUtil.writeJsonl(filesReport, sizeSorter.getSizeBuckets());
        }

        FileChannelRepository fileChannelRepository = new FileChannelRepository(maxOpenFiles, bufferSize);
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
