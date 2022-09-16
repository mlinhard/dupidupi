package sk.linhard.dupidupi;

import java.util.List;

public interface FileBucket {

    List<FileItem> getFiles();

    long fileSize();

    boolean isSingleton();

    int size();

    default long duplicatedBytes() {
        return (size() - 1) * fileSize();
    }
}
