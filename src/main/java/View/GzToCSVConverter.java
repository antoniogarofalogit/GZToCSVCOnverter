package View;

import Controller.Converter;

public class GzToCSVConverter {
    public static void main(String[] args) {
        String inputFilePath = "C:\\Users\\a.garofalo\\Documents\\npaintegration.log.2024-01-16.gz";
        String outputFilePath = "C:\\Users\\a.garofalo\\Documents\\npaintegration.log.2024-01-16.csv";
        Converter converter = new Converter();
        converter.createCSVFile(inputFilePath,outputFilePath);
    }
}
