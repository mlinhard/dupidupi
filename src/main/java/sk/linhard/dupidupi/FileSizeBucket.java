package sk.linhard.dupidupi;

import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.LinkedList;

import static lombok.AccessLevel.PRIVATE;

@Getter
@FieldDefaults(level = PRIVATE)
public class FileSizeBucket {
    final LinkedList<FileItem> files;

    public FileSizeBucket(FileItem firstItem) {
        files = new LinkedList<>();
        files.add(firstItem);
    }

    public void add(FileItem item) {
        files.add(item);
    }

    public long fileSize() {
        return files.getFirst().getSize();
    }

    public boolean isSingleton() {
        return files.size() == 1;
    }

    public boolean isZeroSize() {
        return files.getFirst().getSize() == 0L;
    }
}