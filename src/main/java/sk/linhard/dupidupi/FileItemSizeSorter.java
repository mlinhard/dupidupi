package sk.linhard.dupidupi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

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
                .sorted(comparing(FileBucket::fileSize))
                .collect(Collectors.toList());
    }

    public void dumpJsonl(String file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(FileBucket.class, new FileBucketSerializer(FileBucket.class));
            objectMapper.registerModule(module);
            try (FileOutputStream fileOut = new FileOutputStream(file);
                 SequenceWriter seq = objectMapper.writer()
                         .withRootValueSeparator("\n") // Important! Default value separator is single space
                         .writeValues(fileOut)) {
                for (FileBucket sizeBucket : getSizeBuckets()) {
                    seq.write(sizeBucket);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
