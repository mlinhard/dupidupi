package sk.linhard.dupidupi.report;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sk.linhard.dupidupi.FileItem;
import sk.linhard.dupidupi.ImmutableFileBucket;
import sk.linhard.dupidupi.ResultRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.arraycopy;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportComputerTest {

    ResultRepository results;
    Report report;

    @Test
    void compute1() {
        givenDuplicates(11l, "/a/f1.txt", "/b/f1.txt", "/c/f1.txt");
        givenDuplicates(12l, "/a/f2.txt", "/b/f2.txt", "/d/f2.txt");
        givenDuplicates(13l, "/a/f3.txt", "/d/f3.txt", "/c/f3.txt");

        computeReport();

        assertReport(
                file(26l, "/a/f3.txt", "/c/f3.txt", "/d/f3.txt"),
                file(24l, "/a/f2.txt", "/b/f2.txt", "/d/f2.txt"),
                file(22l, "/a/f1.txt", "/b/f1.txt", "/c/f1.txt")
        );
    }


    @Test
    void compute2() {
        givenDuplicates(0l,
                "/home/d001/f001.txt",
                "/home/d001/f002.txt");
        givenDuplicates(1l,
                "/home/d003/f003.txt",
                "/home/d004/f003.txt");
        givenDuplicates(5l,
                "/home/d005/f004.txt",
                "/home/d006/f004.txt");
        givenDuplicates(5l,
                "/home/d005/f005.txt",
                "/home/d006/f005.txt");
        givenDuplicates(10l,
                "/home/d005/f006.txt",
                "/home/d006/f006.txt");

        computeReport();

        assertReport(
                dir(20l, List.of(
                                file(10l,
                                        "/home/d005/f006.txt",
                                        "/home/d006/f006.txt"),
                                file(5l,
                                        "/home/d005/f004.txt",
                                        "/home/d006/f004.txt"),
                                file(5l,
                                        "/home/d005/f005.txt",
                                        "/home/d006/f005.txt")),
                        "/home/d005",
                        "/home/d006"),
                dir(1l, List.of(
                                file(1l,
                                        "/home/d003/f003.txt",
                                        "/home/d004/f003.txt")),
                        "/home/d003",
                        "/home/d004"),
                file(0l,
                        "/home/d001/f001.txt",
                        "/home/d001/f002.txt")
        );
    }

    private void computeReport() {
        report = new ReportComputer(results).compute();
    }

    private void assertReport(ReportItem... items) {
        assertThat(report.items()).containsExactly(items);
    }

    private ReportItem file(long size, String... paths) {
        return reportItem(ReportItem.ReportItemType.FILE, size, null, paths);
    }

    private ReportItem dir(long size, List<ReportItem> subItems, String... paths) {
        return reportItem(ReportItem.ReportItemType.DIRECTORY, size, null, paths);
    }

    private ReportItem reportItem(ReportItem.ReportItemType type, long size, List<ReportItem> subItems, String... paths) {
        String origPath = paths[0];
        String[] dupPaths = new String[paths.length - 1];
        arraycopy(paths, 1, dupPaths, 0, dupPaths.length);
        return new ReportItem(type, origPath, Arrays.asList(dupPaths), size, subItems);
    }

    private void givenDuplicates(long size, String... paths) {
        if (results == null) {
            results = new ResultRepository();
        }
        results.addDuplicateBucket(new ImmutableFileBucket(ImmutableList.copyOf(
                Arrays.asList(paths).stream()
                        .map(path -> new FileItem(path, size))
                        .collect(Collectors.toList())
        )));
    }
}
