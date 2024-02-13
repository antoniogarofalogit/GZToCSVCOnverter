package Controller;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class ConverterDetails {
    Map<String,Integer> mapErrors400 = new LinkedHashMap<String,Integer>();
    Map<String,Integer> mapErrors401 = new LinkedHashMap<String,Integer>();

    public void createCSVFile(String inputFile, String outputFile) {
        try {
            FileInputStream fileInputStream = new FileInputStream(inputFile);
            GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
            InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
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

    public String getRequestBody(String split){
        String requestBody = "";
        if(split.contains(" RequestBody =")){
            int requestUrl = split.indexOf("- requestUrl");
            requestBody = split.substring(split.indexOf("RequestBody ="),requestUrl);
        }
        return requestBody;
    }

    public String getRequestUrl(String split){
        String requestUrl = "";
        if(split.contains(" requestUrl =")){
            int headers = split.indexOf("- headers");
            requestUrl = split.substring(split.indexOf("requestUrl ="),headers);
        }
        return requestUrl;
    }

    public String getHeaders(String split){
        String headers = "";
        if(split.contains(" headers =")){
            int responseFoundFromException = split.indexOf("- method");
            headers = split.substring(split.indexOf("headers ="),responseFoundFromException);
        }
        return headers;
    }

    public String getResponseFoundFromException(String split){
        String responseFoundFromException = "";
        if(split.contains(" ResponseFoundFromException ")){
            responseFoundFromException  = split.substring(split.indexOf("ResponseFoundFromException "));
        }
        return  responseFoundFromException ;
    }

    private void createCsv400(Map<String, Integer> mapErrors400, String outputFile) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        if (mapErrors400!=null){
            try {
                bufferedWriter.append("StatusCode").append(";").append("RequestBody").append(";").append("RequestUrl").append(";").append("Headers").append(";").append("ResponseFoundFromException").append("\n");
                for (Map.Entry<String,Integer> entry : mapErrors400.entrySet()){
                    bufferedWriter.append("400").append(";");
                    bufferedWriter.append(entry.getKey());
                    bufferedWriter.append("\n");
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
                bufferedWriter.append("StatusCode - 401").append("\n");
                for (Map.Entry<String, Integer> entry : mapErrors401.entrySet()) {
                    bufferedWriter.append(entry.getKey()).append("\n");
                    bufferedWriter.append("\n");
                }
                bufferedWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void processCsvLine(String csvLine, Map<String,Integer> mapErrors) {
        String requestBody = "";
        String requestUrl = "";
        String headers = "";
        String responseFoundFromException = "";
        String[] splits = csvLine.split(" \\| ");
        for (String split : splits) {
            requestBody = getRequestBody(split);
            mapErrors.put(requestBody,1);
            requestUrl = getRequestUrl(split);
            mapErrors.put(requestUrl,1);
            headers = getHeaders(split);
            mapErrors.put(headers,1);
            responseFoundFromException = getResponseFoundFromException(split);
            mapErrors.put(responseFoundFromException,1);
        }
    }
}
