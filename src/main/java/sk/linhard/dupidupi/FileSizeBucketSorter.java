package sk.linhard.dupidupi;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileSizeBucketSorter {

    final FileSizeBucket bucket;
    final UniqueBucketRepository uniqueBucketRepository;
    final FileChannelRepository fileChannelRepository;

    public void sort() throws IOException {
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
