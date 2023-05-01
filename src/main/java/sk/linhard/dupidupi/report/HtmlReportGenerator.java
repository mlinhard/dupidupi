package sk.linhard.dupidupi.report;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import sk.linhard.dupidupi.ReportGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.text.DecimalFormat;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HtmlReportGenerator implements ReportGenerator {

    File reportPath;
    Report report;

    public void generate() {
        try (PrintWriter w = new PrintWriter(new FileOutputStream(reportPath))) {
            w.print("""
                    <html>
                        <head>
                            <title>Deletability report</title>
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
            DecimalFormat df = new DecimalFormat("#,###");

            for (ReportItem item : report.items()) {
                w.print("            <tr><td><a href=\"file://");
                w.print(item.original());
                w.print("\">");
                w.print(item.original());
                w.print("</a></td><td>");
                w.print(df.format(item.numBytesDuplicated()));
                w.print("</td><td>");
                w.println(String.join(" ", item.duplicates()
                        .stream()
                        .map(p -> "<a href=\"file://" + p + "\">" + p + "</a>")
                        .collect(Collectors.toList())));
                w.print("</td></tr>\n");
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
}
