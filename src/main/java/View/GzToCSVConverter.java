package View;

import Controller.*;

import java.util.List;
import java.util.Map;

public class GzToCSVConverter {
    public static void main(String[] args) {
        String inputFolderPath = "C:\\Users\\a.garofalo\\Documents\\LogFolder\\";
        String outputFilePath = "C:\\Users\\a.garofalo\\Documents\\ErrorsOccurrence.csv";
        String outputFilePathDetails = "C:\\Users\\a.garofalo\\Documents\\DetailsErrors.csv";
        String outputHistogramFilePath = "C:\\Users\\a.garofalo\\Documents\\ERR15Histogram.jpeg";
        GzFiles gzFiles = new GzFiles();
        List<String> inputFilePaths = gzFiles.getAllGzFiles(inputFolderPath);
        Converter converter = new Converter();
        converter.createCSVFile(inputFilePaths, outputFilePath);
        ConverterDetails converterDetails = new ConverterDetails();
        converterDetails.createCSVFile(inputFilePaths, outputFilePathDetails);
        String startDate = "2024-02-21";
        String endDate = "2024-02-26";
        Map<String, Integer> errOccurrences = converter.extractERROccurrences(gzFiles.getAllGzFiles(inputFolderPath), startDate, endDate);
        converter.createIstogram(errOccurrences,startDate,endDate,outputHistogramFilePath);
    }
}
