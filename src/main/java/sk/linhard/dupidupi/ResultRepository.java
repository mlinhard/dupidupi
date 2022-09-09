package sk.linhard.dupidupi;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.LinkedList;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResultRepository {

    @Getter
    LinkedList<FileItemBucket> duplicates = new LinkedList<>();

    public void addDuplicateBucket(FileItemBucket duplicateBucket) {
        duplicates.add(duplicateBucket);
    }

    public void addUniqueBucket(FileItemBucket fileItemBucket) {
        // nothing
        // TODO : probably won't even be needed
    }
}
