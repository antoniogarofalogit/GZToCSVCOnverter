package View;

import Controller.Converter;
import Controller.ConverterDetails;

public class GzToCSVConverter {
    public static void main(String[] args) {
       /* String inputFolderPath = "C:\\Users\\a.garofalo\\Documents\\LogFolder";
        List<String> gzFilePaths = getGzFiles(inputFolderPath);
        for (String gzFilePath : gzFilePaths) {
            processGzFile(gzFilePath);
        }*/
        String inputFolderPath = "C:\\Users\\a.garofalo\\Documents\\LogFolder\\npaintegration.log.2024-01-16.gz";
        String outputFilePath = "C:\\Users\\a.garofalo\\Documents\\npaintegration.log.2024-01-16.StatusCode400.401.csv";
        String outputFilePathDetails = "C:\\Users\\a.garofalo\\Documents\\npaintegration.log.2024-01-16.DetailsStatusCode400.401.csv";
        Converter converter = new Converter();
        ConverterDetails converterDetails = new ConverterDetails();
        converter.createCSVFile(inputFolderPath,outputFilePath);
        converterDetails.createCSVFile(inputFolderPath,outputFilePathDetails);
    }
}
