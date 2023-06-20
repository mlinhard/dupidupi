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

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportComputerTest {

    ResultRepository results;
    Report report;

    boolean sortBySize = true;
    List<String> preferredPaths = List.of();

    @Test
    void compute1() {
        givenDuplicates(11L, "/a/f1.txt", "/b/f1.txt", "/c/f1.txt");
        givenDuplicates(12L, "/a/f2.txt", "/b/f2.txt", "/d/f2.txt");
        givenDuplicates(13L, "/a/f3.txt", "/d/f3.txt", "/c/f3.txt");

        computeReport();

        assertReport(
                file(26L, "/a/f3.txt", "/c/f3.txt", "/d/f3.txt"),
                file(24L, "/a/f2.txt", "/b/f2.txt", "/d/f2.txt"),
                file(22L, "/a/f1.txt", "/b/f1.txt", "/c/f1.txt")
        );
    }

    @Test
    void compute2() {
        givenDuplicates(11L, "/a/f1.txt", "/b/f1.txt", "/c/f1.txt");
        givenDuplicates(12L, "/a/f2.txt", "/b/f2.txt", "/d/f2.txt");
        givenDuplicates(13L, "/a/f3.txt", "/d/f3.txt", "/c/f3.txt");
        givenPreferredPaths("/b");

        computeReport();

        assertReport(
                file(26L, "/a/f3.txt", "/c/f3.txt", "/d/f3.txt"),
                file(24L, "/b/f2.txt", "/a/f2.txt", "/d/f2.txt"),
                file(22L, "/b/f1.txt", "/a/f1.txt", "/c/f1.txt")
        );
    }

    @Disabled("Doesn't work yet")
    @Test
    void compute3() {
        givenDuplicates(0L,
                "/home/d001/f001.txt",
                "/home/d001/f002.txt");
        givenDuplicates(1L,
                "/home/d003/f003.txt",
                "/home/d004/f003.txt");
        givenDuplicates(5L,
                "/home/d005/f004.txt",
                "/home/d006/f004.txt");
        givenDuplicates(5L,
                "/home/d005/f005.txt",
                "/home/d006/f005.txt");
        givenDuplicates(10L,
                "/home/d005/f006.txt",
                "/home/d006/f006.txt");

        computeReport();

        assertReport(
                dir(20L, List.of(
                                file(10L,
                                        "/home/d005/f006.txt",
                                        "/home/d006/f006.txt"),
                                file(5L,
                                        "/home/d005/f004.txt",
                                        "/home/d006/f004.txt"),
                                file(5L,
                                        "/home/d005/f005.txt",
                                        "/home/d006/f005.txt")),
                        "/home/d005",
                        "/home/d006"),
                dir(1L, List.of(
                                file(1L,
                                        "/home/d003/f003.txt",
                                        "/home/d004/f003.txt")),
                        "/home/d003",
                        "/home/d004"),
                file(0L,
                        "/home/d001/f001.txt",
                        "/home/d001/f002.txt")
        );
    }

    private void computeReport() {
        report = new ReportComputer(results, sortBySize, preferredPaths).compute();
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
                Arrays.stream(paths)
                        .map(path -> new FileItem(path, size))
                        .collect(Collectors.toList())
        )));
    }

    private void givenPreferredPaths(String... preferredPaths) {
        this.preferredPaths = List.of(preferredPaths);
    }
}
