package Controller;

import Model.ErroriConstants;
import com.sun.xml.internal.ws.util.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class Converter {
    Map<String,Integer> mapErrors400 = new LinkedHashMap<>();
    Map<String,Integer> dateError = new LinkedHashMap<>();

    public void createCSVFile(String inputFile, String outputFile) {
        try {
            FileInputStream fileInputStream = new FileInputStream(inputFile);
            GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
            InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("StatusCode =  - 400")) {
                    processCsvLine400(line, mapErrors400);}
            }
            bufferedReader.close();
            createCsv400(mapErrors400, dateError, outputFile);
            System.out.println("Conversione completata.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCsv400(Map<String, Integer> mapErrors400, Map<String,Integer> dateError, String outputFile) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.append("StatusCode").append(";").append("Day").append(";").append("ErrorsCode").append(";").append("Occurence").append("\n");
        String day="";
        for(int i = 0; i < dateError.size(); i++){
            try {
                for (Map.Entry<String,Integer> entry : dateError.entrySet()){
                    day = entry.getKey();
                }

                for (Map.Entry<String,Integer> entry : mapErrors400.entrySet()){
                    bufferedWriter.append("400").append(";").append(day).append(";");
                    bufferedWriter.append(entry.getKey()).append(";").append(String.valueOf(entry.getValue())).append("\n");
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        bufferedWriter.close();
    }

    public void processCsvLine400(String csvLine, Map<String, Integer> mapErrors400) {
        for (Field field : ErroriConstants.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                SimpleDateFormat simpleDateFormatInput = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date date = simpleDateFormatInput.parse(csvLine.substring(0,10));
                    String fieldValue = (String) field.get(null);
                    if (csvLine.contains("\"" + fieldValue + "\"")) {
                        if (mapErrors400.containsKey(fieldValue)) {
                            Integer count = mapErrors400.get(fieldValue);
                            count = count + 1;
                            mapErrors400.put(fieldValue, count);
                        } else {
                            mapErrors400.put(fieldValue, 1);
                            String output = simpleDateFormatInput.format(date);
                            dateError.put(output,1);
                        }
                    }
                } catch (IllegalAccessException | ParseException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
