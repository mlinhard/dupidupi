package sk.linhard.dupidupi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public interface FileBucket extends Iterable<FileItem> {

    List<FileItem> getFiles();

    @JsonProperty
    long fileSize();

    @JsonIgnore
    boolean isSingleton();

    @JsonIgnore
    int size();

    @JsonIgnore
    default long duplicatedBytes() {
        return (size() - 1) * fileSize();
    }

    @JsonProperty("files")
    default List<String> getSortedPaths() {
        return getFiles().stream()
                .map(FileItem::path)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    default Iterator<FileItem> iterator() {
        return getFiles().iterator();
    }
}
