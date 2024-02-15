package Controller;

import Model.ErroriConstants;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class Converter {
    Map<String, Map<String, Integer>> mapErrorsByDate = new LinkedHashMap<>();
    private static final Logger LOGGER = Logger.getLogger(Converter.class);

    public void createCSVFile(List<String> inputFilePath, String outputFile) {
        for (String inputFile : inputFilePath) {
            try {
                FileInputStream fileInputStream = new FileInputStream(inputFile);
                GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
                InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("StatusCode =  - 400")) {
                        processCsvLine400(line);
                    }
                }
                bufferedReader.close();
                createCsv400(outputFile);
            } catch (IOException e) {
                logger(e);
            }
        }
    }

    private void createCsv400(String outputFile) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.append("StatusCode").append(";").append("Day").append(";").append("ErrorsCode").append(";").append("Occurence").append("\n");
        try{
        for (Map.Entry<String, Map<String, Integer>> dateEntry : mapErrorsByDate.entrySet()) {
            String day = dateEntry.getKey();
            Map<String, Integer> errors = dateEntry.getValue();
            for (Map.Entry<String, Integer> errorEntry : errors.entrySet()) {
                String errorCode = errorEntry.getKey();
                Integer occurrence = errorEntry.getValue();
                bufferedWriter.append("400").append(";").append(day).append(";").append(errorCode).append(";").append(String.valueOf(occurrence)).append("\n");
            }
        }
        }catch (Exception e){
            logger(e);
        }
        bufferedWriter.close();
    }

    private void processCsvLine400(String csvLine) {
        SimpleDateFormat simpleDateFormatInput = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = simpleDateFormatInput.parse(csvLine.substring(0, 10));
            String day = simpleDateFormatInput.format(date);
            Map<String, Integer> mapErrors400 = mapErrorsByDate.getOrDefault(day, new LinkedHashMap<>());
            for (Field field : ErroriConstants.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                    String fieldValue = (String) field.get(null);
                    if (csvLine.contains("\"" + fieldValue + "\"")) {
                        mapErrors400.put(fieldValue, mapErrors400.getOrDefault(fieldValue, 0) + 1);
                    }
                }
            }
            mapErrorsByDate.put(day, mapErrors400);
        } catch (ParseException | IllegalAccessException e) {
            logger(e);
            throw new RuntimeException(e);
        }
    }

    private void logger(Exception e) {
        LOGGER.error(e.getMessage());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(e.getMessage(), e);
        }
    }
}
