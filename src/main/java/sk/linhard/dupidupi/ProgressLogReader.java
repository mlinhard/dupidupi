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
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ProgressLogReader implements AutoCloseable {

    final File progressLog;
    BufferedReader reader;
    MutableFileBucket currentBucketCandidate;
    Integer currentBucketId;

    public ResultRepository parseResultsFromFinishedLog(Set<Long> sizesToCheck) {
        try {
            Set<Long> remainingSizes = new HashSet<>(sizesToCheck);
            ensureReader();
            ResultRepository resultRepository = new ResultRepository();
            Item item;
            int lineNum = 1;
            while ((item = nextItem()) != null) {
                switch (item.type) {
                    case FILE -> {
                        var fileItem = new FileItem(item.path, item.fileSize);
                        if (currentBucketCandidate == null) {
                            currentBucketId = item.bucketId;
                            currentBucketCandidate = new MutableFileBucket(fileItem);
                        } else {
                            checkState(item.bucketId == currentBucketId, "inconsistent log on line " + lineNum);
                            checkState(item.fileSize == currentBucketCandidate.fileSize(), "inconsistent log on line " + lineNum);
                            currentBucketCandidate.add(fileItem);
                        }
                    }
                    case DONE_D -> {
                        checkState(item.bucketId == currentBucketId, "inconsistent log on line " + lineNum);
                        checkState(currentBucketCandidate != null, "inconsistent log on line " + lineNum);
                        resultRepository.addDuplicateBucket(currentBucketCandidate);
                        currentBucketCandidate = null;
                    }
                    case DONE_S -> {
                        checkState(remainingSizes.remove(item.fileSize), "inconsistent log, unexpected size bucket size " + item.fileSize + " on line " + lineNum);
                    }
                    default -> throw new IllegalStateException("unknown item type");
                }
                lineNum++;
            }

            checkState(remainingSizes.isEmpty(), "inconsistent log, remaining " + remainingSizes.size() + " unprocessed size-buckets");

            return resultRepository;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Item nextItem() throws IOException {
        var nextLine = reader.readLine();
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
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(progressLog), StandardCharsets.UTF_8));
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
}
