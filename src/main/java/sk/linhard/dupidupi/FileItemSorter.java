package sk.linhard.dupidupi;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Slf4j
public class FileItemSorter implements Consumer<FileItem> {

    long count = 0l;
    Map<Long, FileSizeBucket> files = new HashMap<>();
    UniqueBucketRepository uniqueBucketRepository = new UniqueBucketRepository();

    @Override
    public void accept(FileItem fileItem) {
        count++;
        files.compute(fileItem.getSize(), (k, existingBucket) -> {
            if (existingBucket == null) {
                return new FileSizeBucket(fileItem);
            } else {
                existingBucket.add(fileItem);
                return existingBucket;
            }
        });
        if (count % 10_000L == 0) {
            log.info("Count {}", count);
        }
    }

    public void sort() {
        for (FileSizeBucket fileSizeBucket : files.values()) {
            if (fileSizeBucket.isSingleton() || fileSizeBucket.isZeroSize()) {
                uniqueBucketRepository.addUniqueBucket(fileSizeBucket);
            } else {
                try (FileChannelRepository fileChannelRepository = new FileChannelRepository(100)) {
                    new FileSizeBucketSorter(fileSizeBucket, uniqueBucketRepository, fileChannelRepository).sort();
                } catch (IOException e) {
                    log.error("Error while sorting file size bucket {}", fileSizeBucket.fileSize(), e);
                }
            }
        }
    }

}
