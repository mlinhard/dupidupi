package sk.linhard.dupidupi.report;

import java.util.List;

public record ReportItem(
        ReportItemType type,
        String original,
        List<String> duplicates,
        long numBytesDuplicated,
        List<ReportItem> subItems) {

    public enum ReportItemType {
        FILE, DIRECTORY
    }
}
