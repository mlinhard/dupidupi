package sk.linhard.dupidupi;

import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
@AllArgsConstructor
public class FileItemPrefixReader {

    FileItem item;
    FileChannelRepository fileChannelRepository;

    public Byte nextByte() {
        // TODO implement buffered fetching of bytes
        return null;
    }
}
