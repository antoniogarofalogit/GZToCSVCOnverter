package View;

import Controller.Converter;
import java.util.List;
import static Controller.GzFiles.getGzFiles;
import static Controller.GzFiles.processGzFile;

public class GzToCSVConverter {
    public static void main(String[] args) {
       /* String inputFolderPath = "C:\\Users\\a.garofalo\\Documents\\LogFolder";
        List<String> gzFilePaths = getGzFiles(inputFolderPath);
        for (String gzFilePath : gzFilePaths) {
            processGzFile(gzFilePath);
        }*/
        String inputFolderPath = "C:\\Users\\a.garofalo\\Documents\\LogFolder\\npaintegration.log.2024-01-16.gz";
        String outputFilePath = "C:\\Users\\a.garofalo\\Documents\\npaintegration.log.csv";
        Converter converter = new Converter();
        converter.createCSVFile(inputFolderPath,outputFilePath);
    }
}
