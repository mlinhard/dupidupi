package sk.linhard.dupidupi;

import lombok.experimental.FieldDefaults;

import java.util.LinkedList;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
public class SizeBucket {
    final LinkedList<FileItem> files;

    public SizeBucket(FileItem firstItem) {
        files = new LinkedList<>();
        files.add(firstItem);
    }

    public void add(FileItem item) {
        files.add(item);
    }
}
