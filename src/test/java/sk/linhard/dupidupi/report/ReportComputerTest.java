package sk.linhard.dupidupi.report;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
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

    @Test
    void compute_sortByPath() {
        givenDuplicates(11L, "/a/f1.txt", "/b/f1.txt", "/c/f1.txt");
        givenDuplicates(12L, "/a/f2.txt", "/b/f2.txt", "/d/f2.txt");
        givenDuplicates(13L, "/a/f3.txt", "/d/f3.txt", "/c/f3.txt");
        givenPreferredPaths("/b");
        givenSortBySize(false);

        computeReport();

        assertReport(
                file(26L, "/a/f3.txt", "/c/f3.txt", "/d/f3.txt"),
                file(22L, "/b/f1.txt", "/a/f1.txt", "/c/f1.txt"),
                file(24L, "/b/f2.txt", "/a/f2.txt", "/d/f2.txt")
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

    private void givenSortBySize(boolean value) {
        this.sortBySize = value;
    }

    private void givenPreferredPaths(String... preferredPaths) {
        this.preferredPaths = List.of(preferredPaths);
    }
}
