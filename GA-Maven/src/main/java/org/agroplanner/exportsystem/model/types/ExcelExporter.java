package org.agroplanner.exportsystem.model.types;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.exportsystem.model.BaseExporter;
import org.agroplanner.exportsystem.model.ExportType;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.inventory.model.InventoryEntry;
import org.agroplanner.inventory.model.PlantInventory;
import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.gasystem.model.Point;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.*;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p><strong>Concrete Exporter: Microsoft Excel (.xlsx)</strong></p>
 * <p>Cleaned up version with separated Styles and Logic.</p>
 */
public class ExcelExporter extends BaseExporter {

    private static final String[] HEADERS = {"ID", "TYPE", "LABEL", "X(m)", "Y(m)", "RADIUS(m)"};

    // Costanti di Layout per garantire il rapporto 1:1 su Mac e Windows
    private static final float ROW_HEIGHT = 26.3f;

    @Override
    protected String getExtension() {
        return ExportType.EXCEL.getExtension();
    }

    @Override
    protected void performExport(Individual individual, Domain domain, PlantInventory inventory, Path path) throws IOException {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Optimization Plan");

            // 1. Inizializzazione Stili (separata dalla logica)
            ExcelStyles styles = new ExcelStyles(workbook);

            int rowIndex = 0;

            // 2. Scrittura Metadati
            rowIndex = writeMetadata(sheet, rowIndex, domain, inventory, individual, styles);

            // 3. Scrittura Header Tabella
            int headerRowIndex = rowIndex;
            rowIndex = writeHeader(sheet, rowIndex, styles);

            // 4. Scrittura Dati (Main Loop)
            // Ritorna la mappa dei range per il grafico
            Map<PlantType, int[]> rangeMap = writeDataRows(sheet, rowIndex, individual, styles);

            // 5. Setup Layout (Auto-size e Canvas quadrato per il grafico)
            // Calcoliamo le dimensioni del grafico basandoci sulla griglia creata
            int[] chartDimensions = setupLayoutAndCanvas(sheet);
            int chartCols = chartDimensions[0];
            int chartRows = chartDimensions[1];

            // 6. Generazione Grafico
            createMultiSeriesChart(sheet, rangeMap, headerRowIndex, chartCols, chartRows);

            int disclaimerRow = headerRowIndex + chartRows + 1;
            writeChartDisclaimer(sheet, disclaimerRow, styles);

            // 7. Scrittura su disco
            try (OutputStream fileOut = Files.newOutputStream(path)) {
                workbook.write(fileOut);
            }
        }
    }

    // ==================================================================================
    // LOGIC METHODS (Private step-by-step methods)
    // ==================================================================================

    private int writeMetadata(XSSFSheet sheet, int rowIndex, Domain domain, PlantInventory inventory, Individual individual, ExcelStyles styles) {
        createMetadataRow(sheet, rowIndex++, "Domain:", domain.toString(), styles.header, styles.left);
        createMetadataRow(sheet, rowIndex++, "Inventory Configuration:", "", styles.header, styles.left);

        for (InventoryEntry entry : inventory.getEntries()) {
            String summary = String.format(Locale.US, "%s (%s): %d units (r=%.2fm)",
                    entry.getType().name(), entry.getType().getLabel(), entry.getQuantity(), entry.getRadius());
            createMetadataRow(sheet, rowIndex++, " - " + entry.getType().name(), summary, styles.header, styles.left);
        }

        createMetadataRow(sheet, rowIndex++, "Total Plants:", String.valueOf(inventory.getTotalPopulationSize()), styles.header, styles.left);
        createMetadataRow(sheet, rowIndex++, "Fitness:", String.format("%.6f", individual.getFitness()), styles.header, styles.left);

        return rowIndex + 1; // +1 per lo Spacer
    }

    private int writeHeader(XSSFSheet sheet, int rowIndex, ExcelStyles styles) {
        Row headerRow = sheet.createRow(rowIndex);
        headerRow.setHeightInPoints(ROW_HEIGHT);

        for (int i = 0; i < HEADERS.length; i++) {
            createStyledCell(headerRow, i, HEADERS[i], styles.tableHeader);
        }
        return rowIndex + 1;
    }

    private Map<PlantType, int[]> writeDataRows(XSSFSheet sheet, int rowIndex, Individual individual, ExcelStyles styles) {
        Map<PlantType, List<Point>> groupedPoints = individual.getChromosomes().stream()
                .collect(Collectors.groupingBy(Point::getType));

        Map<PlantType, int[]> rangeMap = new EnumMap<>(PlantType.class);
        int idCounter = 1;

        for (Map.Entry<PlantType, List<Point>> entry : groupedPoints.entrySet()) {
            PlantType type = entry.getKey();
            List<Point> pointsOfType = entry.getValue();

            if (pointsOfType.isEmpty()) continue;

            int startRow = rowIndex;

            for (Point p : pointsOfType) {
                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(ROW_HEIGHT);

                createStyledCell(row, 0, idCounter++, styles.center);
                createStyledCell(row, 1, p.getType().name(), styles.center);
                createStyledCell(row, 2, p.getType().getLabel(), styles.emoji);

                // Arrotondamento reale per evitare numeri sporchi
                createStyledCell(row, 3, p.getX(), styles.decimal);
                createStyledCell(row, 4, p.getY(), styles.decimal);
                createStyledCell(row, 5, p.getRadius(), styles.radius);
            }

            rangeMap.put(type, new int[]{startRow, rowIndex - 1});
        }
        return rangeMap;
    }

    private int[] setupLayoutAndCanvas(XSSFSheet sheet) {
        // A. Auto-size SOLO colonne dati per leggibilità
        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, (int) (currentWidth * 1.2) + 1000); // Padding
        }

        // B. Definiamo le dimensioni del grafico "Hardcoded" ma equilibrate.
        // Non tocchiamo più le larghezze delle colonne sottostanti (Chart Start Col ecc).
        // Usiamo un box di 15 colonne x 30 righe.
        // Dato che hai ROW_HEIGHT a 26.3f (molto alta), 30 righe coprono una buona altezza verticale.
        // Questo crea un "Canvas" approssimativamente quadrato/rettangolare standard.

        int chartCols = 15;
        int chartRows = 25;

        return new int[]{chartCols, chartRows};
    }

    private void createMultiSeriesChart(XSSFSheet sheet, Map<PlantType, int[]> rangeMap, int anchorRow, int chartCols, int chartRows) {
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        int colStart = 7;

        // Anchor Quadrato basato sui calcoli precedenti
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0,
                colStart, anchorRow,
                colStart + chartCols, anchorRow + chartRows
        );

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Cultivation Map (By Species)");
        chart.setTitleOverlay(false); // Overlay Titolo

        // Legenda in Overlay
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);
        legend.setOverlay(false);

        // Assi
        XDDFValueAxis bottomAxis = chart.createValueAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("X Position (m)");
        bottomAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Y Position (m)");
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFScatterChartData data = (XDDFScatterChartData) chart.createData(ChartTypes.SCATTER, bottomAxis, leftAxis);

        // Aggiunta Serie
        for (Map.Entry<PlantType, int[]> entry : rangeMap.entrySet()) {
            PlantType type = entry.getKey();
            int startRow = entry.getValue()[0];
            int endRow = entry.getValue()[1];

            XDDFNumericalDataSource<Double> xs = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(startRow, endRow, 3, 3));
            XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(startRow, endRow, 4, 4));

            XDDFScatterChartData.Series series = (XDDFScatterChartData.Series) data.addSeries(xs, ys);
            series.setTitle(type.getLabel() + " " + type.name(), null);
            series.setMarkerStyle(MarkerStyle.CIRCLE);
            series.setMarkerSize((short) 5);

            XDDFShapeProperties properties = new XDDFShapeProperties();
            XDDFLineProperties lineProperties = new XDDFLineProperties();
            lineProperties.setFillProperties(new XDDFNoFillProperties());
            properties.setLineProperties(lineProperties);
            series.setShapeProperties(properties);
        }
        chart.plot(data);
    }

    // ==================================================================================
    // HELPER METHODS & INNER CLASSES
    // ==================================================================================

    private void createMetadataRow(XSSFSheet sheet, int rowIndex, String label, String value, CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowIndex);
        row.setHeightInPoints(ROW_HEIGHT);

        Cell cellKey = row.createCell(0);
        cellKey.setCellValue(label);
        if (labelStyle != null) cellKey.setCellStyle(labelStyle);

        Cell cellValue = row.createCell(1);
        cellValue.setCellValue(value);
        if (valueStyle != null) cellValue.setCellStyle(valueStyle);
    }

    private void createStyledCell(Row row, int colIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else if (value != null) {
            cell.setCellValue(String.valueOf(value));
        } else {
            cell.setCellValue("");
        }
        if (style != null) cell.setCellStyle(style);
    }

    private void writeChartDisclaimer(XSSFSheet sheet, int startRow, ExcelStyles styles) {
        int startCol = 7;
        // --- RIGA 1: Avviso ---
        // Recuperiamo la riga esistente (se c'è la tabella dati) o la creiamo (se siamo sotto)
        Row row1 = sheet.getRow(startRow);
        if (row1 == null) {
            row1 = sheet.createRow(startRow);
            row1.setHeightInPoints(ROW_HEIGHT); // Altezza standard
        }

        Cell cell1 = row1.createCell(startCol);
        cell1.setCellValue("⚠️ Graph might not be to scale.");
        cell1.setCellStyle(styles.hint); // Usa lo stile hint che hai già

        // --- RIGA 2: Istruzione ---
        Row row2 = sheet.getRow(startRow);
        if (row2 == null) {
            row2 = sheet.createRow(startRow);
            row2.setHeightInPoints(ROW_HEIGHT);
        }

        Cell cell2 = row2.createCell(startCol+1);
        cell2.setCellValue("👉 Pinch/Drag corner to resize."); // Testo più breve
        cell2.setCellStyle(styles.hint);
    }

    /**
     * Inner Class per incapsulare la creazione degli stili.
     * Mantiene il codice principale pulito.
     */
    private static class ExcelStyles {
        final CellStyle center;
        final CellStyle left;
        final CellStyle decimal;
        final CellStyle radius;
        final CellStyle emoji;
        final CellStyle header;
        final CellStyle tableHeader;
        final CellStyle hint;

        ExcelStyles(Workbook workbook) {
            // 1. Center
            center = workbook.createCellStyle();
            center.setAlignment(HorizontalAlignment.CENTER);
            center.setVerticalAlignment(VerticalAlignment.CENTER);

            // 2. Left
            left = workbook.createCellStyle();
            left.setAlignment(HorizontalAlignment.LEFT);
            left.setVerticalAlignment(VerticalAlignment.CENTER);

            // 3. Decimal (4 digits)
            decimal = workbook.createCellStyle();
            decimal.cloneStyleFrom(center);
            decimal.setDataFormat(workbook.createDataFormat().getFormat("0.0000"));

            // 4. Radius (2 digits)
            radius = workbook.createCellStyle();
            radius.cloneStyleFrom(center);
            radius.setDataFormat(workbook.createDataFormat().getFormat("0.00"));

            // 5. Emoji
            emoji = workbook.createCellStyle();
            emoji.cloneStyleFrom(center);
            Font emojiFont = workbook.createFont();
            emojiFont.setFontHeightInPoints((short) 14);
            emoji.setFont(emojiFont);

            // 6. Header
            header = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            header.setFont(headerFont);
            header.setAlignment(HorizontalAlignment.LEFT);
            header.setVerticalAlignment(VerticalAlignment.CENTER);

            // 7. Table Header
            tableHeader = workbook.createCellStyle();
            tableHeader.cloneStyleFrom(center);
            tableHeader.setFont(headerFont);

            // 8. Hint (Stile per il disclaimer)
            hint = workbook.createCellStyle();
            hint.setAlignment(HorizontalAlignment.LEFT);
            hint.setVerticalAlignment(VerticalAlignment.TOP); // Allineato in alto
            hint.setWrapText(true); // FONDAMENTALE per il \n

            Font hintFont = workbook.createFont();
            hintFont.setItalic(true);
            hintFont.setFontHeightInPoints((short) 10); // Un po' più piccolo
            hintFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex()); // Grigio discreto
            hint.setFont(hintFont);
        }
    }
}