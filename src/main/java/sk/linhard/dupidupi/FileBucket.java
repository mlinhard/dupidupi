package sk.linhard.dupidupi;

import java.util.List;

public interface FileBucket {

    List<FileItem> getFiles();

    long fileSize();

    boolean isSingleton();
}
