package View;

import Controller.*;

import java.util.List;

public class GzToCSVConverter {
    public static void main(String[] args) {
        String inputFolderPath = "C:\\Users\\a.garofalo\\Documents\\LogFolder\\";
        String outputFilePath = "C:\\Users\\a.garofalo\\Documents\\ErrorsOccurrence.csv";
        String outputFilePathDetails = "C:\\Users\\a.garofalo\\Documents\\DetailsErrors.csv";
        GzFiles gzFiles = new GzFiles();
        List<String> inputFilePaths = gzFiles.getAllGzFiles(inputFolderPath);
        Converter converter = new Converter();
        converter.createCSVFile(inputFilePaths,outputFilePath);
        ConverterDetails converterDetails = new ConverterDetails();
        converterDetails.createCSVFile(inputFilePaths, outputFilePathDetails);
    }
}
