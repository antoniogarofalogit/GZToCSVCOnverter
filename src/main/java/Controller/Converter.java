package Controller;

import Model.ErroriConstants;
import com.sun.javafx.scene.control.skin.ColorPalette;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
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
        try {
            for (Map.Entry<String, Map<String, Integer>> dateEntry : mapErrorsByDate.entrySet()) {
                String day = dateEntry.getKey();
                Map<String, Integer> errors = dateEntry.getValue();
                for (Map.Entry<String, Integer> errorEntry : errors.entrySet()) {
                    String errorCode = errorEntry.getKey();
                    Integer occurrence = errorEntry.getValue();
                    bufferedWriter.append("400").append(";").append(day).append(";").append(errorCode).append(";").append(String.valueOf(occurrence)).append("\n");
                }
            }
        } catch (Exception e) {
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

    public Map<String, Integer> extractERROccurrences(List<String> inputFilePaths, String startDate, String endDate) {
        Map<String, Integer> errOccurrences = new LinkedHashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            for (String filePath : inputFilePaths) {
                FileInputStream fileInputStream = new FileInputStream(filePath);
                GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
                InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.matches(".*\\bERR15\\b.*")) {
                        String day = extractDateFromLogLine(line);
                        if (day != null) {
                            errOccurrences.put(day, errOccurrences.getOrDefault(day, 0) + 1);
                        }
                    }
                }
                bufferedReader.close();
            }
        } catch (IOException | ParseException e) {
            logger(e);
        }
        return errOccurrences;
    }

    private String extractDateFromLogLine(String logLine) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            String dateString = logLine.substring(0, 10);
            return dateFormat.format(dateFormat.parse(dateString));
        } catch (ParseException | StringIndexOutOfBoundsException e) {
            logger(e);
            return null;
        }
    }

    public void createIstogram(Map<String, Integer> err4Occurrences, String startDate, String endDate, String outputHistogramFilePath) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            for (Date date = start; date.compareTo(end) <= 0; date.setTime(date.getTime() + 86400000)) {
                String day = sdf.format(date);
                int occurrences = err4Occurrences.getOrDefault(day, 0);
                dataset.addValue(occurrences, "ERR15", day);
            }
        } catch (Exception e) {
            logger(e);
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "ERR15 Occurrences",
                "Day",
                "Occurrences",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, Color.BLACK);
        NumberAxis yAxis = (NumberAxis) chart.getCategoryPlot().getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        BufferedImage image = chart.createBufferedImage(800, 600);
        try {
            File outputfile = new File(outputHistogramFilePath);
            javax.imageio.ImageIO.write(image, "jpeg", outputfile);
        } catch (IOException e) {
            logger(e);
        }
    }

    private void logger(Exception e) {
        LOGGER.error(e.getMessage());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(e.getMessage(), e);
        }
    }
}
