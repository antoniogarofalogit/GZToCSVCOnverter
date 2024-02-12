package Controller;

import Model.ErroriConstants;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPInputStream;

public class Converter {
    Map<String,Integer> mapErrors400 = new HashMap<String,Integer>();

    public void createCSVFile(String inputFile, String outputFile) {
        try {
            FileInputStream fileInputStream = new FileInputStream(inputFile);
            GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
            InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] arrays = splitLine(line);
                String csvLine = createCSVLine(arrays);
                if (csvLine.contains("StatusCode =  - 400")) {
                    processCsvLine400(csvLine, mapErrors400);}
            }
            bufferedReader.close();
            createCsv400(mapErrors400, outputFile);
            System.out.println("Conversione completata.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCsv400(Map<String, Integer> mapErrors400, String outputFile) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        if (mapErrors400!=null){
            try {
                bufferedWriter.append("StatusCode - 400").append("\n");
                bufferedWriter.append("ErrorsCode,Occurrence,Day").append("\n");
                for (Map.Entry<String,Integer> entry : mapErrors400.entrySet()){
                    bufferedWriter.append(entry.getKey()).append(",").append(String.valueOf(entry.getValue())).append("\n");
                }
                bufferedWriter.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static String[] splitLine(String line) {
        return line.split(",");
    }

    private static String createCSVLine(String[] splits){
        StringBuilder csvLineBuilder = new StringBuilder();
        for (String split : splits) {
            csvLineBuilder.append(split.trim());
            csvLineBuilder.append(",");
        }
        csvLineBuilder.setLength(csvLineBuilder.length() - 1);
        return csvLineBuilder.toString();
    }

    public void processCsvLine400(String csvLine, Map<String, Integer> mapErrors400) {
            for (Field field : ErroriConstants.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                    try {
                        String fieldValue = (String) field.get(null);
                        if (csvLine.contains("\"" + fieldValue + "\"")) {
                            if (mapErrors400.containsKey(fieldValue)) {
                                Integer count = mapErrors400.get(fieldValue);
                                count = count + 1;
                                mapErrors400.put(fieldValue, count);
                            } else {
                                mapErrors400.put(fieldValue, 1);
                            }
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    private LocalDateTime getDateTime(String dateString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS ");
            return LocalDateTime.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return LocalDateTime.now();
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS ");
            return dateTime.format(formatter);
        } else {
            return "";
        }
    }
}
