package sk.linhard.dupidupi;

public interface ProgressLog extends AutoCloseable {

    void addDuplicateBucket(FileBucket bucket);

    void addSizeBucketCompletion(long bucketSize);

    @Override
    default void close() {
        // nothing to do
    }
}
