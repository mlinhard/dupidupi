package sk.linhard.dupidupi.report;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import sk.linhard.dupidupi.FileBucket;
import sk.linhard.dupidupi.FileItem;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileReportItemCandidate implements Iterable<FileItem> {

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

    @Override
    public Iterator<FileItem> iterator() {
        return bucket.iterator();
    }

    public Set<Edge> cliqueEdges() {
        var edgeSetBuilder = ImmutableSet.<Edge>builder();
        var fileList = bucket.getFiles();
        for (int i = 0; i < fileList.size(); i++) {
            for (int j = i + 1; j < fileList.size(); j++) {
                edgeSetBuilder.add(new Edge(fileList.get(i), fileList.get(j)));
            }
        }
        return edgeSetBuilder.build();
    }

    public Set<Edge> edgesBetweenParents() {
        return cliqueEdges().stream()
                .map(Edge::parents)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
