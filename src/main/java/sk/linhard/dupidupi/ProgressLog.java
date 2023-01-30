package sk.linhard.dupidupi;

public interface ProgressLog {

    void addDuplicateBucket(FileBucket bucket);

    void addSizeBucketCompletion(long bucketSize);
}
