package Controller;

import Model.ErroriConstants;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class Converter {
    Map<String,Integer> mapErrors400 = new HashMap<String,Integer>();
    Map<String,Integer> mapErrors401 = new HashMap<String,Integer>();
    public void createCSVFile(String inputFile,String outputFile){
        try {
            // Apri il file gz in input
            FileInputStream fileInputStream = new FileInputStream(inputFile);
            GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
            InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // Elabora la riga e trasformala in formato csv
                String[] arrays = splitLine(line);
                String csvLine = createCSVLine(arrays);
                // Verifica se la riga contiene la stringa indicata
             if (csvLine.contains("StatusCode =  - 400")) {
                 processCsvLine400(csvLine, mapErrors400);
                }
             else if (csvLine.contains("StatusCode =  - 401")){
                 processCsvLine401(csvLine, mapErrors401);
                }
            }
            // Chiudi tutti i file
            bufferedReader.close();
            createCsv400(mapErrors400, outputFile);
            createCsv401(mapErrors401, outputFile);
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
                bufferedWriter.append("ErrorsCode,Occurrence").append("\n");
                for (Map.Entry<String,Integer> entry : mapErrors400.entrySet()){
                    bufferedWriter.append(entry.getKey()).append(",").append(String.valueOf(entry.getValue())).append("\n");
                }
                bufferedWriter.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void createCsv401(Map<String, Integer> mapErrors401, String outputFile) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile, true); // Imposta il flag "true" per appendere al file
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        if (mapErrors401 != null) {
            try {
                bufferedWriter.append("\n");
                bufferedWriter.append("StatusCode - 401").append("\n");
                bufferedWriter.append("Detail,Occurrence").append("\n");
                for (Map.Entry<String, Integer> entry : mapErrors401.entrySet()) {
                    bufferedWriter.append(entry.getKey()).append(",").append(String.valueOf(entry.getValue())).append("\n");
                }
                bufferedWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String[] splitLine(String line) {
        return line.split(",");
        // Restituisce un array di tipo Stringhe contenente i segmenti separati della riga.
    }

    private static String createCSVLine(String[] splits){
        // Trasforma i dati nella riga nel formato desiderato per il file csv
        StringBuilder csvLineBuilder = new StringBuilder();
        for (String split : splits) {
            csvLineBuilder.append(split.trim());
            csvLineBuilder.append(",");
        }
        csvLineBuilder.setLength(csvLineBuilder.length() - 1); // Rimuovi l'ultima virgola
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

    public void processCsvLine401(String csvLine, Map<String, Integer> mapErrors401) {
            // Estrai il contenuto del campo "detail"
            String detail = "";
            String[] csvArray = csvLine.split(",");
            for (int i = 0; i < csvArray.length - 1; i++) {
                if (csvArray[i].trim().equals("\"detail\"")) {
                    detail = csvArray[i + 1].trim().replace("\"", "");
                    break;
                }
            }
            // Aggiorna la mappa con l'occorrenza corrispondente
            if (!detail.isEmpty()) {
                if (mapErrors401.containsKey(detail)) {
                    Integer count = mapErrors401.get(detail);
                    count = count + 1;
                    mapErrors401.put(detail, count);
                } else {
                    mapErrors401.put(detail, 1);
                }
            }
    }

   /* public void readJson(){
        try {
            // Percorso del file JSON
            Path filePath = Paths.get("C:\\Users\\a.garofalo\\Accenture\\Java\\GzToCSVConverter\\src\\main\\resources\\errori.json");

            // Leggi tutte le righe dal file
            List<String> lines = Files.readAllLines(filePath);

            // Unisci le righe in una singola stringa
            String jsonContent = String.join("", lines);
              System.out.println(jsonContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
