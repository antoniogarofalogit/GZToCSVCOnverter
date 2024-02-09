package Controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class GzFiles {
    public static List<String> getGzFiles(String folderPath) {
        List<String> gzFilePaths = new ArrayList<>();

        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".gz")) {
                    gzFilePaths.add(file.getAbsolutePath());
                }
            }
        }
        return gzFilePaths;
    }

    public static void processGzFile(String gzFilePath) {
        try (FileInputStream file = new FileInputStream(gzFilePath);
             GZIPInputStream gzip = new GZIPInputStream(file)) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzip.read(buffer)) > 0) {
                String line = new String(buffer, 0, len);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
