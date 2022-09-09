package sk.linhard.dupidupi;

import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
public class FileItemPrefixReader {

    final FileItem item;
    final FileChannelRepository fileChannelRepository;

    long prefixLength;
    long bufferOffset;
    ByteBuffer fileBytes;

    public FileItemPrefixReader(FileItem item, FileChannelRepository fileChannelRepository) {
        this.item = item;
        this.fileChannelRepository = fileChannelRepository;
        this.prefixLength = 0L;
        this.bufferOffset = 0L;
        this.fileBytes = null;
    }

    public Byte nextByte() {
        try {
            FileChannel ch = fileChannelRepository.get(item.getPath());
            if (fileBytes == null) {
                fileBytes = ByteBuffer.allocate(Math.min(32, (int) item.getSize()));
            }
            int bytesRead = ch.read(fileBytes, bufferOffset);
            return null;

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
