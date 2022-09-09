package sk.linhard.dupidupi;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.Consumer;

import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class Walker {

    List<Path> roots;
    List<Path> ignore;

    public void run(Consumer<FileItem> consumer) {
        try {
            for (Path root : roots) {
                Files.walkFileTree(root, new FileVisitorImpl(ignore, consumer));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Value
    @Slf4j
    private static class FileVisitorImpl implements FileVisitor<Path> {

        List<Path> ignore;
        Consumer<FileItem> consumer;

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (ignore != null) {
                for (Path ignorePath : ignore) {
                    if (ignorePath.isAbsolute()) {
                        if (ignorePath.equals(dir)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    } else {
                        if (dir.endsWith(ignorePath)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            consumer.accept(new FileItem(file.toString(), attrs.size()));
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            log.error("Error while visiting {}", file, exc);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}
