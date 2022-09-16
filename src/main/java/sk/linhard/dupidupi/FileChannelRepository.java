package sk.linhard.dupidupi;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.READ;
import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
public class FileChannelRepository implements AutoCloseable {

    @Getter
    final int bufferSize;
    final LoadingCache<String, FileChannel> openChannels;

    public FileChannelRepository(int maxSize, int bufferSize) {
        this.bufferSize = bufferSize;
        this.openChannels = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .removalListener(FileChannelRepository::removeChannel)
                .build(new CacheLoader<>() {
                    @Override
                    public FileChannel load(String path) {
                        return openChannel(path);
                    }
                });
    }

    public FileChannel get(String path) {
        return openChannels.getUnchecked(path);
    }

    @Override
    public void close() {
        openChannels.invalidateAll();
    }

    private static void removeChannel(RemovalNotification<String, FileChannel> notification) {
        try {
            notification.getValue().close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static FileChannel openChannel(String path) {
        try {
            return FileChannel.open(Path.of(path), READ);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
