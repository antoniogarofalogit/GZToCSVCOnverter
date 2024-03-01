package Controller;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Model.ErroriConstants;
import org.apache.log4j.Logger;

import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class ConverterDetailsCSV {
    private static final Logger LOGGER = Logger.getLogger(ConverterDetailsCSV.class);
    List<String> listaResponse = new ArrayList<>();

    public void createCSVFileDetails(List<String> inputFilePath, String outputFile) {
        for (String inputFile : inputFilePath) {
            try {
                FileInputStream fileInputStream = new FileInputStream(inputFile);
                GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
                InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("StatusCode =  - 400")) {
                        processCsvLine(line, outputFile);
                    }
                }
                bufferedReader.close();
                writeCSVFile(listaResponse, outputFile);
            } catch (IOException | ParseException e) {
                logger(e);
            }
        }
    }

    public String getDay(String split) throws ParseException {
        SimpleDateFormat simpleDateFormatInput = new SimpleDateFormat("yyyy-MM-dd");
        Date date = simpleDateFormatInput.parse(split.substring(0, 10));
        String day = simpleDateFormatInput.format(date);
        if (split.contains(" ERROR ")) {
            return day;
        } else {
            throw new IllegalArgumentException("La stringa non contiene la data");
        }
    }

    public String getHour(String split) throws ParseException {
        SimpleDateFormat simpleDateFormatInput = new SimpleDateFormat("HH:mm:ss");
        Date date = simpleDateFormatInput.parse(split.substring(11, 19));
        String hour = simpleDateFormatInput.format(date);
        if (split.contains(" ERROR ")) {
            return hour;
        } else {
            throw new IllegalArgumentException("La stringa non contiene l'ora");
        }
    }

    public String getRequestBody(String split) {
        String requestBody;
        if (split.contains(" RequestBody =")) {
            int requestUrl = split.indexOf("- requestUrl");
            requestBody = split.substring(split.indexOf("RequestBody ="), requestUrl);
            return requestBody;
        } else {
            throw new IllegalArgumentException("La stringa non contiene RequestBody");
        }
    }

    public String getRequestUrl(String split) {
        String requestUrl;
        if (split.contains(" requestUrl =")) {
            int headers = split.indexOf("- headers");
            requestUrl = split.substring(split.indexOf("requestUrl ="), headers);
            return requestUrl;
        } else {
            throw new IllegalArgumentException("La stringa non contiene RequestUrl");
        }
    }

    public String getHeaders(String split) {
        String headers;
        if (split.contains(" headers =")) {
            int responseFoundFromException = (split.indexOf(" - method"));
            headers = split.substring(split.indexOf("headers ="), responseFoundFromException);
            return headers;
        } else {
            throw new IllegalArgumentException("La stringa non contiene Headers");
        }
    }

    public String getResponseFoundFromException(String split) {
        String responseFoundFromException;
        if (split.contains(" ResponseFoundFromException ")) {
            responseFoundFromException = split.substring(split.indexOf("ResponseFoundFromException "));
            return responseFoundFromException;
        } else {
            throw new IllegalArgumentException("La stringa non contiene ResponseFoundFromException");
        }
    }

    public String getErrorCode(String split) {
        String errorCode = StringUtils.EMPTY;
        for (Field field : ErroriConstants.class.getDeclaredFields()) {
            try {
                String constantValue = (String) field.get(null);
                if (split.contains(constantValue) && constantValue.length() > errorCode.length()) {
                    errorCode = constantValue;
                }
            } catch (IllegalAccessException e) {
                logger(e);
            }
        }
        return errorCode;
    }

    private String processCsvLine(String csvLine, String outputFile) throws IOException, ParseException {
        String[] splits = csvLine.split(" \\| ");
        if (ArrayUtils.isNotEmpty(splits)) {
            for (String split : splits) {
                String day = getDay(split);
                String hour = getHour(split);
                String errorCode = getErrorCode(split);
                String requestBody = getRequestBody(split);
                String requestUrl = getRequestUrl(split);
                String headers = getHeaders(split);
                String responseFoundFromException = getResponseFoundFromException(split);
                List<String> formattedInformation = new ArrayList<>();
                formattedInformation.add(day);
                formattedInformation.add(hour);
                formattedInformation.add(errorCode);
                formattedInformation.add(requestBody);
                formattedInformation.add(requestUrl);
                formattedInformation.add(headers);
                formattedInformation.add(responseFoundFromException);
                String formattedLine = returnFormattedString(formattedInformation);
                listaResponse.add(formattedLine);
            }
        }
        return outputFile;
    }

    private String returnFormattedString(List<String> information) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String info : information) {
            stringBuilder.append(info).append(";");
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private void writeCSVFile(List<String> listaResponse, String outputFile) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.append("Day").append(";").append("Hour").append(";").append("ErrorCode").append(";").append("RequestBody").append(";").append("RequestUrl").append(";").append("Headers").append(";").append("ResponseFoundFromException").append("\n");
        try {
            bufferedWriter.append(listaResponse.toString()).append("\n");
        } catch (Exception e) {
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
