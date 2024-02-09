package Controller;

import Model.ErroriConstants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class Converter {
    Map<String,Integer> map = new HashMap<String,Integer>();
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
                    if(csvLine.contains("\"ERR38\"")){
                        if(map.containsKey(ErroriConstants.ERR38)){
                            Integer count = map.get(ErroriConstants.ERR38);
                            count = count + 1;
                            map.put(ErroriConstants.ERR38,count);
                        }else {
                            map.put(ErroriConstants.ERR38,1);
                        }
                    }
                    else if(csvLine.contains("\"ERR4\"")){
                        if(map.containsKey(ErroriConstants.ERR4)){
                            Integer count = map.get(ErroriConstants.ERR4);
                            count = count + 1;
                            map.put(ErroriConstants.ERR4,count);
                        }else {
                            map.put(ErroriConstants.ERR4,1);
                        }
                    }
               }
            }
            // Chiudi tutti i file
            bufferedReader.close();
            createCsv(map, outputFile);
            System.out.println("Conversione completata.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCsv(Map<String, Integer> map, String outputFile) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        if (map!=null){
            try {
                bufferedWriter.append("Errors,Occurrence").append("\n");
                for (Map.Entry<String,Integer> entry : map.entrySet()){
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

    public void readJson(){
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
    }
}
