package sk.linhard.dupidupi;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
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
        this.fileBytes = ByteBuffer.allocate(Math.min(32, (int) item.getSize()));
    }

    public Byte nextByte() throws IOException {
        FileChannel ch = fileChannelRepository.get(item.getPath());
        int bytesRead = ch.read(fileBytes, bufferOffset);
        return null;
    }
}
