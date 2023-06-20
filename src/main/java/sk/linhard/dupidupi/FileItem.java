package sk.linhard.dupidupi;

import sk.linhard.dupidupi.report.Path;

public record FileItem(String path,
                       long size) implements Path, Comparable<FileItem> {


    @Override
    public String toString() {
        return path + ", size=" + size;
    }

    @Override
    public int compareTo(FileItem o) {
        return this.path.compareTo(o.path);
    }
}
