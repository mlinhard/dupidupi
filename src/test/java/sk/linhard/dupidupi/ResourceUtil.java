package sk.linhard.dupidupi;

import com.google.common.io.Resources;

public class ResourceUtil {
    public static String resPath(String resPath) {
        return Resources.getResource(resPath).getPath();
    }
}
