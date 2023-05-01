package sk.linhard.dupidupi.report;

import java.util.LinkedList;
import java.util.List;

public record Directory(String path, List<Path> children) implements Path {

    public Directory(String path) {
        this(path, new LinkedList<>());
    }

    @Override
    public long size() {
        return children.stream().mapToLong(Path::size).sum();
    }
}
