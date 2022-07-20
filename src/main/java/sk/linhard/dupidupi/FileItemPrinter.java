package sk.linhard.dupidupi;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class FileItemPrinter implements Consumer<FileItem> {

    @Override
    public void accept(FileItem fileItem) {
        log.info("Found: {} size {}", fileItem.getPath(), fileItem.getSize());
    }
}
