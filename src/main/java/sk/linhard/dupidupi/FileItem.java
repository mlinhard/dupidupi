package sk.linhard.dupidupi;

import lombok.Value;

@Value
public class FileItem {
    String path;
    long size;

    @Override
    public String toString() {
        return path + ", size=" + size;
    }
}
