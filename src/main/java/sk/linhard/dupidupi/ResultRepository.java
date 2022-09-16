package sk.linhard.dupidupi;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.LinkedList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResultRepository {

    LinkedList<FileBucket> duplicates = new LinkedList<>();

    public void addDuplicateBucket(FileBucket duplicateBucket) {
        duplicates.add(duplicateBucket);
    }

    public List<FileBucket> duplicates() {
        return duplicates;
    }

    public int numDuplicates() {
        return duplicates.stream().mapToInt(b -> b.size() - 1).sum();
    }

    public long bytesDuplicated() {
        return duplicates.stream().mapToLong(b -> b.duplicatedBytes()).sum();
    }
}
