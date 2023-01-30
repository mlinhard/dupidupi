package sk.linhard.dupidupi;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.sort;

@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HtmlReportGenerator implements ReportGenerator {

    File reportPath;
    ResultRepository results;

    public void generate() {
        List<FileBucket> duplicates = results.duplicates();

        List<String> allDuplicatedPaths = new ArrayList<>();
        Map<String, PrintBucket> path2bucket = new HashMap<>();

        for (FileBucket duplicate : duplicates) {
            PrintBucket printBucket = new PrintBucket(duplicate);
            for (FileItem file : duplicate.getFiles()) {
                allDuplicatedPaths.add(file.getPath());
                var prevBucket = path2bucket.put(file.getPath(), printBucket);
                checkState(prevBucket == null, "File " + file.getPath() + " mapped twice");
            }
        }

        sort(allDuplicatedPaths);

        try (PrintWriter w = new PrintWriter(new FileOutputStream(reportPath))) {
            w.print("""
                    <html>
                        <head>
                            <title>Duplicate report</title>
                            <style>
                            table {
                              border-collapse: collapse;
                            }
                            table, th, td {
                              border: 1px solid;
                            }
                            </style>
                        </head>
                        <body>
                            <table>
                                <thead>
                                    <tr>
                                        <th>Path</th>
                                        <th>Size</th>
                                        <th>Duplicates</th>
                                    </tr>
                                </thead>
                                <tbody>
                    """);
            for (String path : allDuplicatedPaths) {
                PrintBucket printBucket = path2bucket.get(path);
                if (!printBucket.printed) {
                    w.print("            <tr><td><a href=\"file://");
                    w.print(path);
                    w.print("\">");
                    w.print(path);
                    w.print("</a></td><td>");
                    w.print(printBucket.bucket.fileSize());
                    w.print("</td><td>");
                    w.println(String.join(" ", printBucket.bucket.getFiles()
                            .stream()
                            .map(FileItem::getPath)
                            .map(p -> "<a href=\"file://" + p + "\">" + p + "</a>")
                            .filter(p -> !p.equals(path))
                            .collect(Collectors.toList())));
                    w.print("</td></tr>\n");
                    printBucket.printed = true;
                }
            }

            w.print("""
                                </tbody>
                            </table>
                        </body>
                    </html>""");
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    @RequiredArgsConstructor
    static class PrintBucket {
        final FileBucket bucket;
        boolean printed;
    }

}
