package sk.linhard.dupidupi;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;

import static com.google.common.base.Preconditions.checkState;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileItemPrefixSorter {

    final ResultRepository resultRepository;
    final FileChannelRepository fileChannelRepository;

    public void sort(FileItemBucket bucket) {
        try {
            long fileSize = bucket.fileSize();
            checkState(fileSize != 0L, "Trying to sort file size bucket for file size 0");
            FileItemPrefixReader[] readers = new FileItemPrefixReader[bucket.getFiles().size()];
            int i = 0;
            for (FileItem item : bucket.getFiles()) {
                readers[i++] = new FileItemPrefixReader(item, fileChannelRepository);
            }

            for (int prefixLength = 1; prefixLength <= fileSize; prefixLength++) {
                var prefixes = new HashMap<Byte, FileItemPrefixReader>();
                for (FileItemPrefixReader reader : readers) {
                    prefixes.put(reader.nextByte(), reader);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
