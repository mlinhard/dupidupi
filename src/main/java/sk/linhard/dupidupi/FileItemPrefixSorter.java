package sk.linhard.dupidupi;

import com.google.common.collect.ImmutableList;
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

    public void sort(FileBucket bucket) {
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

    private PrefixSortTask createRootTask(FileBucket bucket) {
        FileItemPrefixReader[] readers = new FileItemPrefixReader[bucket.getFiles().size()];
        int i = 0;
        for (FileItem item : bucket.getFiles()) {
            readers[i++] = new FileItemPrefixReader(item, fileChannelRepository);
        }
        return new PrefixSortTask(0L, readers);
    }

    private void splitByNextByte(PrefixSortTask task, LinkedList<PrefixSortTask> sortTasks) {
        if (task.isSingleton()) {
            return;
        }
        if (task.fileSize() == task.prefixLength) {
            resultRepository.addDuplicateBucket(task.toBucket());
            return;
        }
        var readersByNextByte = new HashMap<Integer, LinkedList<FileItemPrefixReader>>();
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
        for (LinkedList<FileItemPrefixReader> readerList : readersByNextByte.values()) {
            FileItemPrefixReader[] readerArray = readerList.toArray(new FileItemPrefixReader[readerList.size()]);
            PrefixSortTask newTask = new PrefixSortTask(task.prefixLength + 1L, readerArray);
            sortTasks.add(newTask);
        }
    }

    @AllArgsConstructor
    private static class PrefixSortTask {
        long prefixLength;
        FileItemPrefixReader[] readers;

        boolean isSingleton() {
            return readers.length == 1;
        }

        long fileSize() {
            return readers[0].getItem().getSize();
        }

        FileBucket toBucket() {
            ImmutableList.Builder<FileItem> listBuilder = ImmutableList.builder();
            for (FileItemPrefixReader reader : readers) {
                listBuilder.add(reader.getItem());
            }
            return new ImmutableFileBucket(listBuilder.build());
        }
    }
}
