package sk.linhard.dupidupi;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Slf4j
public class FileItemSorter implements Consumer<FileItem> {

    long count = 0l;
    Map<Long, LinkedList<FileItem>> files = new HashMap<>();

    @Override
    public void accept(FileItem fileItem) {
        count++;
        files.compute(fileItem.getSize(), (k, oldList) -> {
            if (oldList == null) {
                return new LinkedList<>(List.of(fileItem));
            } else {
                oldList.add(fileItem);
                return oldList;
            }
        });
        if (count % 10_000L == 0) {
            log.info("Count {}", count);
        }
    }
}
