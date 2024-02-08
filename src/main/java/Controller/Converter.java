package Controller;


import Model.ErroriConstants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class Converter {
    public void createCSVFile(String inputFile,String outputFile){
        try {
            // Apri il file gz in input
            FileInputStream fileInputStream = new FileInputStream(inputFile);
            GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
            InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            // Crea il file csv in output
            FileWriter fileWriter = new FileWriter(outputFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // Elabora la riga e trasformala in formato csv
                String[] arrays = splitLine(line);
                String csvLine = createCSVLine(arrays);
                // Verifica se la riga contiene la stringa indicata
             if (csvLine.contains("errori")) {
                    // Scrivi la riga nel file csv solo se contiene la stringa indicata
                    bufferedWriter.write(csvLine);
                    bufferedWriter.newLine();
               }
            }
            // Chiudi tutti i file
            bufferedReader.close();
            bufferedWriter.close();

            System.out.println("Conversione completata.");
        } catch (IOException e) {
            e.printStackTrace();
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
