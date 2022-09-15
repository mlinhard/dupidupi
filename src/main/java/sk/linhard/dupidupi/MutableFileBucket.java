package sk.linhard.dupidupi;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.LinkedList;

import static lombok.AccessLevel.PRIVATE;


@Getter
@FieldDefaults(level = PRIVATE)
public class MutableFileBucket implements FileBucket {
    final LinkedList<FileItem> files;

    public MutableFileBucket(FileItem firstItem) {
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

    public ImmutableFileBucket toImmutable() {
        return new ImmutableFileBucket(ImmutableList.copyOf(files));
    }
}