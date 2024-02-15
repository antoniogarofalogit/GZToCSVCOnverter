package Controller;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import Model.ErroriConstants;
import org.apache.log4j.Logger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class ConverterDetails {
    private static final Logger LOGGER = Logger.getLogger(ConverterDetails.class);
    List<String> listaResponse = new ArrayList<>();

    public void createCSVFile(List<String> inputFilePath, String outputFile) {
        for(String inputFile : inputFilePath){
        try {
            FileInputStream fileInputStream = new FileInputStream(inputFile);
            GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
            InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("StatusCode =  - 400") || line.contains("StatusCode =  - 401")) {
                    processCsvLine(line, outputFile);
                }
            }
            bufferedReader.close();
            writeCSVFile(listaResponse, outputFile);
        } catch (IOException e) {
            logger(e);
        }
    }
}

    public String getRequestBody(String split){
        String requestBody;
        if(split.contains(" RequestBody =")){
            int requestUrl = split.indexOf("- requestUrl");
            requestBody = split.substring(split.indexOf("RequestBody ="),requestUrl);
            return requestBody;
        }else{
            return StringUtils.EMPTY;
        }
    }

    public String getRequestUrl(String split){
        String requestUrl;
        if(split.contains(" requestUrl =")){
            int headers = split.indexOf("- headers");
            requestUrl = split.substring(split.indexOf("requestUrl ="),headers);
            return requestUrl;
        }else {
            return StringUtils.EMPTY;
        }
    }

    public String getHeaders(String split){
        String headers;
        if(split.contains(" headers =")){
            int responseFoundFromException = (split.indexOf(" - method"));
            headers = split.substring(split.indexOf("headers ="),responseFoundFromException);
            return headers;
        }else {
            return StringUtils.EMPTY;
        }
    }

    public String getResponseFoundFromException(String split){
        String responseFoundFromException;
        if(split.contains(" ResponseFoundFromException ")){
            responseFoundFromException = split.substring(split.indexOf("ResponseFoundFromException "));
            return responseFoundFromException;
        } else{
            return StringUtils.EMPTY;
        }
    }

    public String getErrorCode(String split){
        String longestMatch = StringUtils.EMPTY;
        for (Field field : ErroriConstants.class.getDeclaredFields()) {
            try {
                String constantValue = (String) field.get(null);
                if (split.contains(constantValue)&& constantValue.length() > longestMatch.length()) {
                    longestMatch = constantValue;
                }
            } catch (IllegalAccessException e) {
                logger(e);
            }
        }
        return longestMatch;
    }

    private String processCsvLine(String csvLine, String outputFile) throws IOException {
        String[] splits = csvLine.split(" \\| ");
        if (ArrayUtils.isNotEmpty(splits)){
        for (String split : splits) {
            String errorCode = getErrorCode(split);
            String requestBody = getRequestBody(split);
            String requestUrl = getRequestUrl(split);
            String headers = getHeaders(split);
            String responseFoundFromException = getResponseFoundFromException(split);
            String formattedLine = returnFormattedString(errorCode, requestBody, requestUrl, headers, responseFoundFromException);
            listaResponse.add(formattedLine);
        }
        }
        return outputFile;
    }

    private String returnFormattedString(String errorCode, String requestBody, String requestUrl, String headers, String responseFoundFromException){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(errorCode).append(";").append(requestBody).append(";").append(requestUrl).append(";").append(headers).append(";").append(responseFoundFromException).append("\n");
        return stringBuilder.toString();
    }

    private void writeCSVFile(List<String> listaResponse, String outputFile) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.append("ErrorCode").append(";").append("RequestBody").append(";").append("RequestUrl").append(";").append("Headers").append(";").append("ResponseFoundFromException").append("\n");
        try{
            bufferedWriter.append(listaResponse.toString()).append("\n");
        }catch (Exception e){
            logger(e);
        }
        bufferedWriter.close();
    }

    private void logger(Exception e) {
        LOGGER.error(e.getMessage());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(e.getMessage(), e);
        }
    }
}
