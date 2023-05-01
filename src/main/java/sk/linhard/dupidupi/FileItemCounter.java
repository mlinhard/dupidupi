package sk.linhard.dupidupi;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Slf4j
public class FileItemCounter implements Consumer<FileItem> {

    long count = 0l;
    Set<Long> sizes = new HashSet<>();

    @Override
    public void accept(FileItem fileItem) {
        count++;
        sizes.add(fileItem.size());
        if (count % 10_000L == 0) {
            log.info("Count {}", count);
        }
    }
}
