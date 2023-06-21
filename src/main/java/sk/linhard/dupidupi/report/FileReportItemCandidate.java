package sk.linhard.dupidupi.report;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import sk.linhard.dupidupi.FileBucket;

import java.util.List;


@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileReportItemCandidate {

    FileBucket bucket;

    public ReportItem toReportItem() {
        var paths = bucket.getSortedPaths();
        var original = paths.get(0);
        var duplicates = ImmutableList.copyOf(paths.subList(1, paths.size()));
        return new ReportItem(
                ReportItem.ReportItemType.FILE,
                original,
                duplicates,
                bucket.duplicatedBytes(),
                null);
    }

    public ReportItem toReportItem(List<String> preferredPaths) {
        for (String preferredPath : preferredPaths) {
            var foundPath = bucket.firstPathStartingWith(preferredPath);
            if (foundPath != null) {
                return new ReportItem(
                        ReportItem.ReportItemType.FILE,
                        foundPath,
                        bucket.getSortedPathsWithout(foundPath),
                        bucket.duplicatedBytes(),
                        null);
            }
        }
        return toReportItem();
    }
}
