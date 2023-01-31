package sk.linhard.dupidupi;

public interface ProgressLogInput extends AutoCloseable {

    ProcessedSizeBucket nextProcessedSizeBucket();

    @Override
    default void close() {
        // nothing to do
    }
}
