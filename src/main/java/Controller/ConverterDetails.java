package Controller;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class ConverterDetails {
    Map<String,Integer> mapErrors400 = new HashMap<String,Integer>();
    Map<String,Integer> mapErrors401 = new HashMap<String,Integer>();

    public void createCSVFile(String inputFile, String outputFile) {
        try {
            FileInputStream fileInputStream = new FileInputStream(inputFile);
            GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
            InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
               StringTokenizer stringTokenizer = new StringTokenizer(line," - ");
                while(stringTokenizer.hasMoreTokens()) {
                   stringTokenizer.nextToken();
                }
                if (line.contains("StatusCode =  - 400")) {
                    processCsvLine(line, mapErrors400);
                } else if (line.contains("StatusCode =  - 401")) {
                    processCsvLine(line, mapErrors401);
                }
            }
            bufferedReader.close();
            createCsv400(mapErrors400, outputFile);
            createCsv401(mapErrors401, outputFile);
            System.out.println("Conversione completata.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDetails(String line){
        int opzione = 0;
        StringTokenizer stringTokenizer = new StringTokenizer(line," - ");
        switch (opzione){
            case (0):
                if(line.contains(" RequestBody = "))
              line = stringTokenizer.nextToken();
              break;
            case (1):
                if(line.contains(" requestUrl ="))
                    line = stringTokenizer.nextToken();
                break;
            case (2):
                if(line.contains(" headers ="))
                    line = stringTokenizer.nextToken();
                break;
            case (3):
                if(line.contains(" ResponseFoundFromException "))
                    line = stringTokenizer.nextToken();
                break;
            default:
                break;
        }
        return line;
    }
    public String getRequestBody(String split){
        String requestBody = "";
        if(split.contains(" RequestBody =") && split.contains(" - ")){
            requestBody = split.substring(split.indexOf("RequestBody ="),split.lastIndexOf(" - "));
        }
        return requestBody;
    }

    public String getRequestUrl(String split){
        String requestUrl = "";
        if(split.contains(" requestUrl =") && split.contains(" - ")){
            requestUrl = split.substring(split.indexOf(" requestUrl ="), split.lastIndexOf(" - "));
        }
        return requestUrl;
    }

    public String getHeaders(String split){
        String headers = "";
        if(split.contains(" headers =") && split.contains(" - ")){
            headers = split.substring(split.indexOf(" headers ="), split.lastIndexOf(" - "));
        }
        return headers;
    }

    public String getResponseFoundFromException(String split){
        String responseFoundFromException = "";
        if(split.contains(" ResponseFoundFromException ") && split.contains(" - ")){
            responseFoundFromException  = split.substring(split.indexOf(" ResponseFoundFromException "), split.lastIndexOf(" - "));
        }
        return  responseFoundFromException ;
    }

    private void createCsv400(Map<String, Integer> mapErrors400, String outputFile) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        if (mapErrors400!=null){
            try {
                bufferedWriter.append("StatusCode - 400").append("\n");
                for (Map.Entry<String,Integer> entry : mapErrors400.entrySet()){
                    bufferedWriter.append(entry.getKey()).append("\n");
                }
                bufferedWriter.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void createCsv401(Map<String, Integer> mapErrors401, String outputFile) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile, true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        if (mapErrors401 != null) {
            try {
                bufferedWriter.append("\n");
                bufferedWriter.append("StatusCode - 401").append("\n");
                for (Map.Entry<String, Integer> entry : mapErrors401.entrySet()) {
                    bufferedWriter.append(entry.getKey()).append("\n");
                }
                bufferedWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void processCsvLine(String csvLine, Map<String, Integer> mapErrors) {
        String requestBody = "";
        String requestUrl = "";
        String headers = "";
        String responseFoundFromException = "";
        String[] splits = csvLine.split(" \\| ");
        List<String> response = new ArrayList<String>();
        for (String split : splits) {
            requestBody = getRequestBody(split);
            requestUrl = getRequestUrl(split);
            headers = getHeaders(split);
            responseFoundFromException = getResponseFoundFromException(split);
            response.add(requestBody);
            response.add(requestUrl);
            response.add(headers);
            response.add(responseFoundFromException);
        }
        if (!requestBody.isEmpty()) {
            if (mapErrors.containsKey(requestBody)) {
                Integer count = mapErrors.get(requestBody);
                count = count + 1;
                mapErrors.put(requestBody, count);
            } else {
                mapErrors.put(requestBody, 1);
            }
        }
    }
}
