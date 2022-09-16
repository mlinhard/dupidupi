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
}
