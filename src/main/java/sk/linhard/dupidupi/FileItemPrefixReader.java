package sk.linhard.dupidupi;

import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
public class FileItemPrefixReader implements AutoCloseable {

    final static int BUF_SIZE = 32;

    @Getter
    final FileItem item;

    final FileInputStream fileInputStream;
    final BufferedInputStream byteStream;

    public FileItemPrefixReader(FileItem item) {
        try {
            this.item = item;
            this.fileInputStream = new FileInputStream(item.getPath());
            this.byteStream = new BufferedInputStream(fileInputStream, BUF_SIZE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public int nextByte() {
        try {
            return byteStream.read();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        try {
            this.byteStream.close();
            this.fileInputStream.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
