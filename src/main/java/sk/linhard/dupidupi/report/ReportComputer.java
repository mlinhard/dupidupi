package sk.linhard.dupidupi.report;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import sk.linhard.dupidupi.ResultRepository;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportComputer {

    ResultRepository resultRepository;
    boolean sortBySize;
    List<String> preferredPaths;

    public Report compute() {
        return new Report(resultRepository.duplicates().stream()
                .map(FileReportItemCandidate::new)
                .map(preferredPaths == null || preferredPaths.isEmpty()
                        ? FileReportItemCandidate::toReportItem
                        : item -> item.toReportItem(preferredPaths))
                .sorted(sortBySize
                        ? Comparator.comparing(ReportItem::numBytesDuplicated).thenComparing(ReportItem::original).reversed()
                        : Comparator.comparing(ReportItem::original))
                .collect(ImmutableList.toImmutableList()));
    }
}
