package sk.linhard.dupidupi;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class FileItemSizeSorter implements Consumer<FileItem> {

    @Getter
    long count = 0l;
    Map<Long, MutableFileBucket> files = new HashMap<>();

    @Override
    public void accept(FileItem fileItem) {
        count++;
        files.compute(fileItem.getSize(), (k, existingBucket) -> {
            if (existingBucket == null) {
                return new MutableFileBucket(fileItem);
            } else {
                existingBucket.add(fileItem);
                return existingBucket;
            }
        });
        if (count % 10_000L == 0) {
            log.info("Count {}", count);
        }
    }

    public int numSizeBuckets() {
        return files.size();
    }

    public Iterable<FileBucket> getSizeBuckets() {
        return files.values().stream()
                .map(MutableFileBucket::toImmutable)
                .collect(Collectors.toList());
    }
}
