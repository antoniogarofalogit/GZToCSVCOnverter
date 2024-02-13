package View;

import Controller.*;

public class GzToCSVConverter {
    public static void main(String[] args) {
        String inputFolderPath = "C:\\Users\\a.garofalo\\Documents\\LogFolder\\npaintegration.log.2024-01-16.gz";
        String outputFilePath = "C:\\Users\\a.garofalo\\Documents\\ErrorsOccurrence.csv";
        String outputFilePathDetails = "C:\\Users\\a.garofalo\\Documents\\DetailsErrors.csv";
        Converter converter = new Converter();
        converter.createCSVFile(inputFolderPath,outputFilePath);
        ConverterDetails converterDetails = new ConverterDetails();
        converterDetails.createCSVFile(inputFolderPath,outputFilePathDetails);
    }
}
