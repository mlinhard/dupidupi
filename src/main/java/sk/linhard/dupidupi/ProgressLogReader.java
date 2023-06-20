package sk.linhard.dupidupi;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ProgressLogReader implements ProgressLogInput {

    final File progressLog;
    BufferedReader reader;
    MutableFileBucket currentBucketCandidate;
    Integer currentBucketId;
    int currentLine;

    public ResultRepository parseResultsFromFinishedLog(Set<Long> sizesToCheck) {
        Set<Long> remainingSizes = new HashSet<>(sizesToCheck);
        ResultRepository resultRepository = new ResultRepository();
        ProcessedSizeBucket sizeBucket;
        while ((sizeBucket = nextProcessedSizeBucket()) != null) {
            for (FileBucket fileBucket : sizeBucket) {
                resultRepository.addDuplicateBucket(fileBucket);
            }
            checkState(remainingSizes.remove(sizeBucket.fileSize()), "inconsistent log, unexpected size bucket size " + sizeBucket.fileSize());
        }
        checkState(remainingSizes.isEmpty(), "inconsistent log, remaining " + remainingSizes.size() + " unprocessed size-buckets");
        return resultRepository;
    }

    @Override
    public Iterator<ProcessedSizeBucket> iterator() {
        return new ProcessedSizeBucketIterator();
    }

    ProcessedSizeBucket nextProcessedSizeBucket() {
        try {
            List<MutableFileBucket> duplicates = new LinkedList<>();
            Item item;
            while ((item = nextItem()) != null) {
                switch (item.type) {
                    case FILE -> {
                        var fileItem = new FileItem(item.path, item.fileSize);
                        if (currentBucketCandidate == null) {
                            currentBucketId = item.bucketId;
                            currentBucketCandidate = new MutableFileBucket(fileItem);
                        } else {
                            checkState(Objects.equals(item.bucketId, currentBucketId), "inconsistent log on line " + currentLine);
                            checkState(item.fileSize == currentBucketCandidate.fileSize(), "inconsistent log on line " + currentLine);
                            currentBucketCandidate.add(fileItem);
                        }
                    }
                    case DONE_D -> {
                        checkState(Objects.equals(item.bucketId, currentBucketId), "inconsistent log on line " + currentLine);
                        checkState(currentBucketCandidate != null, "inconsistent log on line " + currentLine);
                        duplicates.add(currentBucketCandidate);
                        currentBucketCandidate = null;
                    }
                    case DONE_S -> {
                        checkState(currentBucketCandidate == null, "inconsistent log on line " + currentLine);
                        return new ProcessedSizeBucket(item.fileSize, duplicates.stream()
                                .map(MutableFileBucket::toImmutable)
                                .collect(toImmutableList()));
                    }
                    default -> throw new IllegalStateException("unknown item type");
                }
            }
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Item nextItem() throws IOException {
        ensureReader();
        var nextLine = reader.readLine();
        currentLine++;
        return nextLine == null ? null : parseItem(nextLine);
    }

    private Item parseItem(String line) {
        String[] tuple = line.split("\t");
        if ("D".equals(tuple[0])) {
            checkArgument(tuple.length == 2, "wrong progress log line format");
            return new Item(ItemType.DONE_D, Integer.parseInt(tuple[1]), null, null);
        } else if ("S".equals(tuple[0])) {
            checkArgument(tuple.length == 2, "wrong progress log line format");
            return new Item(ItemType.DONE_S, null, Long.parseLong(tuple[1]), null);
        } else {
            checkArgument(tuple.length == 3, "wrong progress log line format");
            return new Item(ItemType.FILE, Integer.parseInt(tuple[0]), Long.parseLong(tuple[1]), tuple[2]);
        }
    }

    private void ensureReader() throws IOException {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(progressLog), StandardCharsets.UTF_8));
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

    private enum ItemType {
        DONE_D,
        DONE_S,
        FILE
    }

    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static class Item {
        ItemType type;
        Integer bucketId;
        Long fileSize;
        String path;
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    private class ProcessedSizeBucketIterator implements Iterator<ProcessedSizeBucket> {

        ProcessedSizeBucket next = ProgressLogReader.this.nextProcessedSizeBucket();

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public ProcessedSizeBucket next() {
            var toReturn = next;
            next = ProgressLogReader.this.nextProcessedSizeBucket();
            return toReturn;
        }
    }
}
