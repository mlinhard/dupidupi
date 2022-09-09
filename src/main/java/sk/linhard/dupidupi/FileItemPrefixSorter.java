package sk.linhard.dupidupi;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.LinkedList;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileItemPrefixSorter {

    final ResultRepository resultRepository;
    final FileChannelRepository fileChannelRepository;

    public void sort(FileItemBucket bucket) {
        if (bucket.isSingleton()) {
            return; // no need to sort this one, this is a unique file
        }
        long fileSize = bucket.fileSize();
        if (fileSize == 0L) {
            // files with zero size are trivial duplicates
            resultRepository.addDuplicateBucket(bucket);
            return;
        }
        LinkedList<PrefixSortTask> sortTasks = new LinkedList<>();
        sortTasks.add(createRootTask(bucket));
        while (!sortTasks.isEmpty()) {
            splitByNextByte(sortTasks.removeFirst(), sortTasks);
        }
    }

    private PrefixSortTask createRootTask(FileItemBucket bucket) {
        FileItemPrefixReader[] readers = new FileItemPrefixReader[bucket.getFiles().size()];
        int i = 0;
        for (FileItem item : bucket.getFiles()) {
            readers[i++] = new FileItemPrefixReader(item, fileChannelRepository);
        }
        return new PrefixSortTask(1L, readers);
    }

    private void splitByNextByte(PrefixSortTask task, LinkedList<PrefixSortTask> sortTasks) {
        // TODO: pruning logic, if singleton, if at the end, etc...
        var readersByNextByte = new HashMap<Byte, LinkedList<FileItemPrefixReader>>();
        for (FileItemPrefixReader reader : task.readers) {
            readersByNextByte.compute(reader.nextByte(), (k, existingBucket) -> {
                if (existingBucket == null) {
                    LinkedList<FileItemPrefixReader> newBucket = new LinkedList<>();
                    newBucket.add(reader);
                    return newBucket;
                } else {
                    existingBucket.add(reader);
                    return existingBucket;
                }
            });
        }

       // TODO: add newly discovered tasks
    }

    @AllArgsConstructor
    private static class PrefixSortTask {
        long prefixLength;
        FileItemPrefixReader[] readers;
    }


    public void sort() {
//        for (FileItemBucket fileItemBucket : files.values()) {
//            if (fileItemBucket.isSingleton() || fileItemBucket.isZeroSize()) {
//                resultRepository.addUniqueBucket(fileItemBucket);
//            } else {
//                try (FileChannelRepository fileChannelRepository = new FileChannelRepository(100)) {
//                    new FileItemPrefixSorter(fileItemBucket, resultRepository, fileChannelRepository).sort();
//                } catch (IOException e) {
//                    log.error("Error while sorting file size bucket {}", fileItemBucket.fileSize(), e);
//                }
//            }
//        }
    }
    /*
     * How to build the dedup algorithm
     *
     * For size 1 size-buckets convert them to unique buckets (with long ids)
     * For size >1 size-buckets put each through size-bucket sorter
     *
     * Read each size-bucket member byte-by-byte and sort them into prefix-buckets
     * Recursively sort prefix-buckets until they are size-1 and can be converted to unique bucket
     *
     * */

}
