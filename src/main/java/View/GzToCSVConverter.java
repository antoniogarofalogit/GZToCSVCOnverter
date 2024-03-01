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
        ConverterOccurenceCSV converterOccurenceCSV = new ConverterOccurenceCSV();
        converterOccurenceCSV.createCSVFile(inputFilePaths, outputFilePath);
       ConverterDetailsCSV converterDetailsCSV = new ConverterDetailsCSV();
        converterDetailsCSV.createCSVFileDetails(inputFilePaths, outputFilePathDetails);
       /* ConverterPDF converterPDF = new ConverterPDF();
        String startDate = "2024-02-21";
        String endDate = "2024-02-26";
        String pdfFilePath = "C:\\Users\\a.garofalo\\Documents\\ErrorHistograms.pdf";
        converterPDF.createPDF(inputFolderPath, pdfFilePath, startDate, endDate);*/
    }
}
