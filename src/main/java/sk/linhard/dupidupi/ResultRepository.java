package sk.linhard.dupidupi;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.LinkedList;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResultRepository {

    @Getter
    LinkedList<FileBucket> duplicates = new LinkedList<>();

    public void addDuplicateBucket(FileBucket duplicateBucket) {
        duplicates.add(duplicateBucket);
    }

    public void addUniqueBucket(FileBucket fileItemBucket) {
        // nothing
        // TODO : probably won't even be needed
    }
}
