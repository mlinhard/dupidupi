package sk.linhard.dupidupi;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.READ;
import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
public class FileChannelRepository implements AutoCloseable {

    final LoadingCache<String, FileChannel> openChannels;

    public FileChannelRepository(int maxSize) {
        openChannels = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .removalListener(FileChannelRepository::removeChannel)
                .build(new CacheLoader<>() {
                    @Override
                    public FileChannel load(String path) {
                        return openChannel(path);
                    }
                });
    }

    public FileChannel get(String path) throws IOException {
        try {
            return openChannels.getUnchecked(path);
        } catch (InternalIoException e) {
            throw e.ioException;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            openChannels.invalidateAll();
        } catch (InternalIoException e) {
            throw e.ioException;
        }
    }

    private static void removeChannel(RemovalNotification<String, FileChannel> notification) {
        try {
            notification.getValue().close();
        } catch (IOException e) {
            throw new InternalIoException(e);
        }
    }

    private static FileChannel openChannel(String path) {
        try {
            return FileChannel.open(Path.of(path), READ);
        } catch (IOException e) {
            throw new InternalIoException(e);
        }
    }


    @AllArgsConstructor
    private static class InternalIoException extends RuntimeException {
        IOException ioException;
    }
}
