package sk.linhard.dupidupi;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class FileItemSizeSorter implements Consumer<FileItem> {

    long fileCount = 0l;
    Map<Long, MutableFileBucket> files = new HashMap<>();

    @Override
    public void accept(FileItem fileItem) {
        fileCount++;
        files.compute(fileItem.getSize(), (k, existingBucket) -> {
            if (existingBucket == null) {
                return new MutableFileBucket(fileItem);
            } else {
                existingBucket.add(fileItem);
                return existingBucket;
            }
        });
        if (fileCount % 10_000L == 0) {
            log.info("Count {}", fileCount);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileItemSizeSorter that = (FileItemSizeSorter) o;
        return fileCount == that.fileCount && Objects.equals(files, that.files);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileCount, files);
    }

    public long numFiles() {
        return fileCount;
    }

    public int numSizeBuckets() {
        return files.size();
    }

    public Iterable<FileBucket> getSizeBuckets() {
        return files.values().stream()
                .map(MutableFileBucket::toImmutable)
                .sorted(comparing(FileBucket::fileSize))
                .collect(Collectors.toList());
    }

    public Set<Long> getBucketFileSizes() {
        return files.values().stream()
                .map(MutableFileBucket::fileSize)
                .collect(Collectors.toSet());
    }
}
