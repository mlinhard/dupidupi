package sk.linhard.dupidupi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

public interface FileBucket {

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
    default List<String> getPaths() {
        return getFiles().stream()
                .map(FileItem::getPath)
                .collect(Collectors.toList());
    }
}
