package sk.linhard.dupidupi.report;

import java.io.File;

public interface Path {

    String path();

    long size(); // size in terms of sum of duplicated items

    default Path getParent() {
        var parent = new File(this.path()).getParent();
        return parent == null ? null : new Directory(parent);
    }
}
