package Controller;

import Model.ErroriConstants;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class ConverterPDF {
    private static final Logger LOGGER = Logger.getLogger(ConverterPDF.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public Map<String, Integer> extractERROccurrences(List<String> inputFilePaths, String startDate, String endDate, String errorCode) {
        Map<String, Integer> errOccurrences = new LinkedHashMap<>();
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            for (String filePath : inputFilePaths) {
                FileInputStream fileInputStream = new FileInputStream(filePath);
                GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
                InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.matches(".*\\b" + errorCode + "\\b.*") && line.contains("StatusCode =  - 400")) {
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
        try {
            String dateString = logLine.substring(0, 10);
            return sdf.format(sdf.parse(dateString));
        } catch (ParseException | StringIndexOutOfBoundsException e) {
            logger(e);
            return null;
        }
    }

    private static void createHistogramAndAddToPDF(PDDocument document, Map<String, Integer> errOccurrences, String errorCode, String startDate, String endDate) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            for (Date date = start; date.compareTo(end) <= 0; date.setTime(date.getTime() + 86400000)) {
                String day = sdf.format(date);
                int occurrences = errOccurrences.getOrDefault(day, 0);
                dataset.addValue(occurrences, errorCode, day);
            }
        } catch (Exception e) {
            logger(e);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                errorCode + " Occurrences",
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
        renderer.setSeriesPaint(0, new Color(138, 43, 226));
        NumberAxis yAxis = (NumberAxis) chart.getCategoryPlot().getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        BufferedImage image = chart.createBufferedImage(800, 600);
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)) {
            String description = getDescriptionFromErrorCode(errorCode);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText(errorCode + ": " + description);
            contentStream.endText();
            contentStream.drawImage(PDImageXObject.createFromByteArray(document, toByteArray(image), ""), 50, 300, 500, 400);
        }
    }

    private static String getDescriptionFromErrorCode(String errorCode) {
        try {
            String errorJsonFilePath = "C:\\Users\\a.garofalo\\Accenture\\Java\\GzToCSVConverter\\src\\main\\resources\\errori.json";
            JsonElement jsonElement = new Gson().fromJson(new FileReader(errorJsonFilePath), JsonElement.class);
            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                for (JsonElement element : jsonArray) {
                    JsonObject errorObject = element.getAsJsonObject();
                    if (errorObject.has("codice") && errorObject.get("codice").getAsString().equals(errorCode)) {
                        JsonObject descriptionObject = errorObject.getAsJsonObject("descrizione");
                        if (descriptionObject.has("it")) {
                            return descriptionObject.get("it").getAsString();
                        } else {
                            return StringUtils.EMPTY;
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger(e);
        }
        return StringUtils.EMPTY;
    }

    private static byte[] toByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    public void createPDF(String inputFolderPath, String pdfFilePath, String startDate, String endDate) {
        try (PDDocument document = new PDDocument()) {
            GzFiles gzFiles = new GzFiles();
            Map<String, Map<String, Integer>> allErrorOccurrences = extractAllErrorOccurrences(gzFiles.getAllGzFiles(inputFolderPath), startDate, endDate);
            for (String errorCode : getAllErrorCodes()) {
                Map<String, Integer> errOccurrences = allErrorOccurrences.getOrDefault(errorCode, Collections.emptyMap());
                if (!errOccurrences.isEmpty()) {
                    createHistogramAndAddToPDF(document, errOccurrences, errorCode, startDate, endDate);
                }
            }
            addTableToPDF(document, allErrorOccurrences, startDate, endDate);
            document.save(pdfFilePath);
            System.out.println("PDF creato con successo!");
        } catch (IOException e) {
            logger(e);
        }
    }

    private Map<String, Map<String, Integer>> extractAllErrorOccurrences(List<String> inputFilePaths, String startDate, String endDate) {
        Map<String, Map<String, Integer>> allErrorOccurrences = new LinkedHashMap<>();
        for (String errorCode : getAllErrorCodes()) {
            Map<String, Integer> errOccurrences = extractERROccurrences(inputFilePaths, startDate, endDate, errorCode);
            allErrorOccurrences.put(errorCode, errOccurrences);
        }
        return allErrorOccurrences;
    }

    private void addTableToPDF(PDDocument document, Map<String, Map<String, Integer>> allErrorOccurrences, String startDate, String endDate) throws IOException {
        int cellWidth = 63;
        int cellHeight = 20;
        int x = 50;
        int y = 750;

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)) {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            drawCell(contentStream, x, y, cellWidth, cellHeight, "Errori", true);
            for (Date date = sdf.parse(startDate); date.compareTo(sdf.parse(endDate)) <= 0; date.setTime(date.getTime() + 86400000)) {
                String day = sdf.format(date);
                drawCell(contentStream, x + cellWidth, y, cellWidth, cellHeight, day, true);
                x += cellWidth;
            }
            drawCell(contentStream, x + cellWidth, y, cellWidth, cellHeight, "Totale", true);
            x = 50;
            y -= cellHeight;

            List<String> errorCodes = getAllErrorCodes();

            Map<String, Integer> totalRow = new HashMap<>();
            for (String errorCode : errorCodes) {
                Map<String, Integer> errOccurrences = allErrorOccurrences.getOrDefault(errorCode, Collections.emptyMap());

                boolean hasOccurrences = errOccurrences.entrySet().stream()
                        .anyMatch(entry -> {
                            try {
                                Date date = sdf.parse(entry.getKey());
                                return date.compareTo(sdf.parse(startDate)) >= 0 && date.compareTo(sdf.parse(endDate)) <= 0 && entry.getValue() > 0;
                            } catch (ParseException e) {
                                logger(e);
                                return false;
                            }
                        });

                if (hasOccurrences) {
                    drawCell(contentStream, x, y, cellWidth, cellHeight, errorCode, true);

                    int rowTotal = 0;
                    for (Date date = sdf.parse(startDate); date.compareTo(sdf.parse(endDate)) <= 0; date.setTime(date.getTime() + 86400000)) {
                        String day = sdf.format(date);
                        int occurrences = errOccurrences.getOrDefault(day, 0);
                        rowTotal += occurrences;
                        drawCell(contentStream, x + cellWidth, y, cellWidth, cellHeight, String.valueOf(occurrences), false);
                        x += cellWidth;
                        totalRow.put(day, totalRow.getOrDefault(day, 0) + occurrences);
                    }
                    drawCell(contentStream, x + cellWidth, y, cellWidth, cellHeight, String.valueOf(rowTotal), false);
                    totalRow.put("Totale", totalRow.getOrDefault("Totale", 0) + rowTotal);

                    x = 50;
                    y -= cellHeight;
                }
            }
            drawCell(contentStream, x, y, cellWidth, cellHeight, "Totale", true);
            for (Date date = sdf.parse(startDate); date.compareTo(sdf.parse(endDate)) <= 0; date.setTime(date.getTime() + 86400000)) {
                String day = sdf.format(date);
                drawCell(contentStream, x + cellWidth, y, cellWidth, cellHeight, String.valueOf(totalRow.getOrDefault(day, 0)), false);
                x += cellWidth;
            }
            drawCell(contentStream, x + cellWidth, y, cellWidth, cellHeight, String.valueOf(totalRow.getOrDefault("Totale", 0)), false);
        } catch (IOException | ParseException e) {
            logger(e);
        }
    }

    private void drawCell(PDPageContentStream contentStream, int x, int y, int width, int height, String text, boolean isHeader) throws IOException {
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(x, y);
        contentStream.lineTo(x + width, y);
        contentStream.stroke();

        contentStream.moveTo(x, y - height);
        contentStream.lineTo(x + width, y - height);
        contentStream.stroke();

        contentStream.moveTo(x, y);
        contentStream.lineTo(x, y - height);
        contentStream.stroke();

        contentStream.moveTo(x + width, y);
        contentStream.lineTo(x + width, y - height);
        contentStream.stroke();

        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);

        String[] textLines = StringUtils.splitByWholeSeparatorPreserveAllTokens(text, " ");
        float lineHeight = PDType1Font.HELVETICA_BOLD.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * 12;
        float textY = y - (height / 2f) - (lineHeight * textLines.length / 2f);
        for (String line : textLines) {
            contentStream.beginText();
            contentStream.newLineAtOffset(x + (width / 2f) - (line.length() * 3), textY);
            contentStream.showText(line);
            contentStream.endText();
            textY += lineHeight;
        }
    }

    private List<String> getAllErrorCodes() {
        List<String> errorCodes = new ArrayList<>();
        Field[] fields = ErroriConstants.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType() == String.class) {
                try {
                    errorCodes.add((String) field.get(null));
                } catch (IllegalAccessException e) {
                    logger(e);
                }
            }
        }
        return errorCodes;
    }

    private static void logger(Exception e) {
        LOGGER.error(e.getMessage());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(e.getMessage(), e);
        }
    }
}
