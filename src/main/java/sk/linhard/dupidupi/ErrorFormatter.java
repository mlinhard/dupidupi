package sk.linhard.dupidupi;

import org.slf4j.helpers.MessageFormatter;

public class ErrorFormatter {

    public static String format(String fmtMessage, Object... params) {
        return MessageFormatter.arrayFormat(fmtMessage, params).getMessage();
    }
}
