package sk.linhard.dupidupi;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkState;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TsvGzReportReader implements Iterable<FileBucket>, AutoCloseable {

    final File dupReport;
    BufferedReader reader;
    MutableFileBucket currentBucketCandidate;
    Integer currentBucketId;
    Item lookAheadItem;

    public ResultRepository parseResults() {
        ResultRepository resultRepository = new ResultRepository();
        for (FileBucket fileBucket : this) {
            resultRepository.addDuplicateBucket(fileBucket);
        }
        return resultRepository;
    }

    @Override
    public Iterator<FileBucket> iterator() {
        return new FileBucketIterator();
    }

    FileBucket nextBucket() {
        try {
            Item item;
            int lineNum = 1;
            while ((item = nextItem()) != null) {
                var fileItem = new FileItem(item.path, item.fileSize);
                if (currentBucketCandidate == null) {
                    currentBucketId = item.bucketId;
                    currentBucketCandidate = new MutableFileBucket(fileItem);
                } else {
                    checkState(currentBucketId != null);
                    if (currentBucketId == item.bucketId) {
                        checkState(item.fileSize == currentBucketCandidate.fileSize(), "inconsistent log on line " + lineNum);
                        currentBucketCandidate.add(fileItem);
                    } else {
                        pushBack(item);
                        break;
                    }
                }
                lineNum++;
            }
            var nextBucket = currentBucketCandidate;
            currentBucketCandidate = null;
            return nextBucket;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Item nextItem() throws IOException {
        if (lookAheadItem == null) {
            ensureReader();
            var nextLine = reader.readLine();
            return nextLine == null ? null : parseItem(nextLine);
        } else {
            var nextItem = lookAheadItem;
            lookAheadItem = null;
            return nextItem;
        }
    }

    private void pushBack(Item item) {
        lookAheadItem = item;
    }

    private Item parseItem(String line) {
        String[] tuple = line.split("\t");
        return new Item(Integer.parseInt(tuple[0]), Long.parseLong(tuple[1]), tuple[2]);
    }

    private void ensureReader() throws IOException {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(new GzipCompressorInputStream(new FileInputStream(dupReport)), StandardCharsets.UTF_8));
        }
    }

    @Override
    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static class Item {
        int bucketId;
        long fileSize;
        String path;
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    private class FileBucketIterator implements Iterator<FileBucket> {

        FileBucket next = nextBucket();

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public FileBucket next() {
            var prev = next;
            next = nextBucket();
            return prev;
        }
    }
}
