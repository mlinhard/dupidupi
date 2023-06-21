package sk.linhard.dupidupi;

public record FileItem(String path, long size) implements Comparable<FileItem> {

    @Override
    public String toString() {
        return path + ", size=" + size;
    }

    @Override
    public int compareTo(FileItem o) {
        return this.path.compareTo(o.path);
    }
}
