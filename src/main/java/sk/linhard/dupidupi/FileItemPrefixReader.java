package sk.linhard.dupidupi;

import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static lombok.AccessLevel.PRIVATE;


/**
 * When reading next byte
 * - check whether we read all file
 * - check whether we still have something in the buffer
 */
@FieldDefaults(level = PRIVATE)
public class FileItemPrefixReader {

    @Getter
    final FileItem item;
    final FileChannelRepository fileChannelRepository;

    FileChannel fileChannel;
    ByteBuffer buffer;
    long readFile;
    long nextBufferReadOffset;


    public FileItemPrefixReader(FileItem item, FileChannelRepository fileChannelRepository) {
        this.item = item;
        this.fileChannelRepository = fileChannelRepository;
        this.readFile = 0L;
        this.nextBufferReadOffset = 0L;
    }

    public int nextByte() {
        try {
            if (readFile >= item.size()) {
                return -1;
            }
            if (buffer == null) {
                buffer = ByteBuffer.allocate(fileChannelRepository.getBufferSize());
                buffer.flip();
            }
            if (buffer.hasRemaining()) {
                readFile++;
                return buffer.get() & 0xff;
            } else {
                buffer.clear();
                while (fileChannel == null || !fileChannel.isOpen()) {
                    fileChannel = fileChannelRepository.get(item.path());
                }
                fileChannel.position(nextBufferReadOffset);
                int ret = fileChannel.read(buffer);
                buffer.flip();
                if (ret == -1) {
                    nextBufferReadOffset = item.size();
                    return -1;
                } else {
                    nextBufferReadOffset += ret;
                    readFile++;
                    return buffer.get() & 0xff;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
