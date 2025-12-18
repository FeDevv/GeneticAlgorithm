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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p><strong>Concrete Exporter: Microsoft Excel (.xlsx)</strong></p>
 * <p>Cleaned up version with separated Styles and Logic.</p>
 */
public class ExcelExporter extends BaseExporter {

    private static final String[] HEADERS = {"ID", "TYPE", "LABEL", "X", "Y", "RADIUS"};

    // Costanti di Layout per garantire il rapporto 1:1 su Mac e Windows
    private static final float ROW_HEIGHT = 26.3f;
    private static final int CHART_COL_WIDTH_UNITS = 3000;
    private static final double CHART_ASPECT_RATIO = 2.7; //2.5 is better for Excel on windows, 2.7 looks better for Numbers on Mac

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
            int chartWidthCols = chartDimensions[0];
            int chartHeightRows = chartDimensions[1];

            // 6. Generazione Grafico
            createMultiSeriesChart(sheet, rangeMap, headerRowIndex, chartWidthCols, chartHeightRows);

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
            String summary = String.format(Locale.US, "%s (%s): %d units (r=%.2f)",
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

        Map<PlantType, int[]> rangeMap = new HashMap<>();
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
        // A. Auto-size colonne dati
        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, (int) (currentWidth * 1.2) + 500);
        }

        // B. Setup Canvas Grafico (16 colonne x 40 righe)
        int chartStartCol = 7;
        int chartWidthCols = 16;
        int chartHeightRows = (int) (chartWidthCols * CHART_ASPECT_RATIO);

        // Forza larghezza colonne grafico a 3000 unità (Canvas Fisso)
        for (int i = 0; i < chartWidthCols; i++) {
            sheet.setColumnWidth(chartStartCol + i, CHART_COL_WIDTH_UNITS);
        }

        return new int[]{chartWidthCols, chartHeightRows};
    }

    private void createMultiSeriesChart(XSSFSheet sheet, Map<PlantType, int[]> rangeMap, int anchorRow, int chartWidthCols, int chartHeightRows) {
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        int colStart = 7;

        // Anchor Quadrato basato sui calcoli precedenti
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0,
                colStart, anchorRow,
                colStart + chartWidthCols, anchorRow + chartHeightRows
        );

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Cultivation Map (By Species)");
        chart.setTitleOverlay(true); // Overlay Titolo

        // Legenda in Overlay
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);
        legend.setOverlay(true);

        // Layout "Blindato" (Quadrato interno all'80%)
        XDDFManualLayout layout = chart.getOrAddManualLayout();
        layout.setXMode(LayoutMode.EDGE);
        layout.setYMode(LayoutMode.EDGE);
        layout.setWidthMode(LayoutMode.FACTOR);
        layout.setHeightMode(LayoutMode.FACTOR);

        layout.setX(0.10);
        layout.setY(0.10);
        layout.setWidthRatio(0.80);
        layout.setHeightRatio(0.80);

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
            series.setMarkerSize((short) 8);

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
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value != null) {
            cell.setCellValue(String.valueOf(value));
        } else {
            cell.setCellValue("");
        }
        if (style != null) cell.setCellStyle(style);
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
        }
    }
}