package sk.linhard.dupidupi;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Getter
@FieldDefaults(level = PRIVATE)
public class ImmutableFileBucket implements FileBucket {

    final ImmutableList<FileItem> files;

    public ImmutableFileBucket(ImmutableList<FileItem> files) {
        this.files = files;
    }

    public long fileSize() {
        return files.get(0).getSize();
    }

    public boolean isSingleton() {
        return files.size() == 1;
    }

    public int size() {
        return files.size();
    }
}