package sk.linhard.dupidupi;

public interface ProgressLogInput extends AutoCloseable, Iterable<ProcessedSizeBucket> {

    @Override
    default void close() {
        // nothing to do
    }
}
