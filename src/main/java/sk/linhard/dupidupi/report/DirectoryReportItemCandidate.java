package sk.linhard.dupidupi.report;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DirectoryReportItemCandidate {

    Directory directoryA;
    Directory directoryB;

    public static DirectoryReportItemCandidate fromFiles() {
        return null;
    }

}
