package Controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GzFiles {
    public static List<String> getAllGzFiles(String folderPath) {
        List<String> gzFiles = new ArrayList<>();
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".gz")) {
                    gzFiles.add(file.getAbsolutePath());
                }
            }
        }
        return gzFiles;
    }
}
