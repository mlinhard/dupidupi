package sk.linhard.dupidupi;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Iterator;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class ProcessedSizeBucket implements Iterable<FileBucket> {

    long fileSize;
    List<FileBucket> duplicates;

    public List<FileBucket> duplicates() {
        return duplicates;
    }

    @Override
    public Iterator<FileBucket> iterator() {
        return duplicates.iterator();
    }

    public long fileSize() {
        return fileSize;
    }
}
