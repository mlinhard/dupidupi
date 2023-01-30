package sk.linhard.dupidupi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.LinkedList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResultRepository implements ProgressLog {

    LinkedList<FileBucket> duplicates = new LinkedList<>();

    @Override
    public void addDuplicateBucket(FileBucket duplicateBucket) {
        duplicates.add(duplicateBucket);
    }

    @Override
    public void addSizeBucketCompletion(long bucketSize) {
        // we don't care about this here
    }

    @JsonProperty
    public List<FileBucket> duplicates() {
        return duplicates;
    }

    @JsonIgnore
    public int numDuplicates() {
        return duplicates.stream().mapToInt(b -> b.size() - 1).sum();
    }

    @JsonIgnore
    public long bytesDuplicated() {
        return duplicates.stream().mapToLong(b -> b.duplicatedBytes()).sum();
    }
}
