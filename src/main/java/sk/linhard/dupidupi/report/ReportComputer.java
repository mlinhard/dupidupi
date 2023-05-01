package sk.linhard.dupidupi.report;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import sk.linhard.dupidupi.ResultRepository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportComputer {

    final ResultRepository resultRepository;

    public Report compute() {

        // TODO: basic algo
        //  so for each dupbucket
        //  create entry edge -> parent directory report item
        //  if new create, if not add subitems
        //  include in report only if pass threshold
        //  if included in reports, don't include subitems

        var fileCandidates = resultRepository.duplicates().stream()
                .map(FileReportItemCandidate::new)
                .toList();

        var directoryCandidates = new HashMap<Edge, DirectoryReportItemCandidate>();

        for (var fileCandidate : fileCandidates) {
            for (var edge : fileCandidate.edgesBetweenParents()) {
                directoryCandidates.compute(edge, (prevEdge, dirCandidate) -> {
                    if (prevEdge == null) {
                        return null;
                    }
                    return null;
                });
            }
        }

        var items = fileCandidates.stream()
                .map(FileReportItemCandidate::toReportItem)
                .collect(Collectors.toList());

        items.sort(Comparator.comparing(ReportItem::numBytesDuplicated)
                .thenComparing(ReportItem::original)
                .reversed());

        return new Report(ImmutableList.copyOf(items));
    }

}
