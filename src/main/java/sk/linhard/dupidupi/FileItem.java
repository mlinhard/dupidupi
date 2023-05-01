package sk.linhard.dupidupi;

import lombok.EqualsAndHashCode;
import lombok.Value;
import sk.linhard.dupidupi.report.Path;

public record FileItem(String path,
                       long size) implements Path {


    @Override
    public String toString() {
        return path + ", size=" + size;
    }
}
