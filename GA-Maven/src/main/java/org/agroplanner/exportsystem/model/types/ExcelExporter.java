package org.agroplanner.exportsystem.model.types;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFNoFillProperties;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;

import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.exportsystem.model.BaseExporter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * <p><strong>Concrete Exporter: Microsoft Excel (.xlsx).</strong></p>
 *
 * <p>This strategy generates a rich spreadsheet containing both the raw data and a visual representation.
 * It uses the <strong>Apache POI</strong> library (OOXML) to manipulate the file structure.</p>
 *
 * <p><strong>Key Features:</strong>
 * <ul>
 * <li><strong>Metadata Formatting:</strong> Uses bold fonts and specific number formats for readability.</li>
 * <li><strong>Embedded Visualization:</strong> Automatically generates a Scatter Plot chart to visualize the solution geometry immediately upon opening the file.</li>
 * </ul>
 * </p>
 */
public class ExcelExporter extends BaseExporter {

    private static final String[] HEADERS = {"ID", "X", "Y"};

    @Override
    protected String getExtension() {
        return ".xlsx"; // Formato Excel
    }

    @Override
    protected void performExport(Individual individual, Domain domain, double radius, Path path) throws IOException {

        // Use try-with-resources to ensure the workbook is closed/saved properly.
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            // Must use XSSFSheet (XML-based) to support drawing/charting features.
            XSSFSheet sheet = workbook.createSheet("Solution Data");

            // --- STYLES CONFIGURATION ---
            CellStyle sixDecimalStyle = workbook.createCellStyle();
            sixDecimalStyle.setDataFormat(workbook.createDataFormat().getFormat("0.000000"));

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // --- METADATA WRITING ---
            int rowIndex = 0;

            // Row 0: Domain Info
            Row domainRow = sheet.createRow(rowIndex++);
            domainRow.createCell(0).setCellValue("Domain Info:");
            domainRow.createCell(1).setCellValue(domain.toString());

            // Row 1: Radius
            Row radiusRow = sheet.createRow(rowIndex++);
            radiusRow.createCell(0).setCellValue("Target Distance:");
            radiusRow.createCell(1).setCellValue(radius);

            // Row 2: Total Points
            Row pointsRow = sheet.createRow(rowIndex++);
            pointsRow.createCell(0).setCellValue("Total Points:");
            pointsRow.createCell(1).setCellValue(individual.getChromosomes().size());

            // Row 3: Fitness
            Row fitnessRow = sheet.createRow(rowIndex++);
            fitnessRow.createCell(0).setCellValue("Fitness:");
            Cell fitnessCell = fitnessRow.createCell(1);
            fitnessCell.setCellValue(individual.getFitness());
            fitnessCell.setCellStyle(sixDecimalStyle);

            rowIndex++; // Empty separator row

            // --- DATA TABLE HEADERS ---
            Row headerRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // --- DATA POPULATION ---
            List<Point> points = individual.getChromosomes();
            int idCounter = 0;
            int firstDataRow = rowIndex; // Keep track of start row for Chart Data Range

            for (Point point : points) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(idCounter++);
                row.createCell(1).setCellValue(point.getX());
                row.createCell(2).setCellValue(point.getY());
            }

            int lastDataRow = rowIndex - 1; // Keep track of end row for Chart Data Range

            // UX Polish: Auto-size columns to fit content
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // --- VISUALIZATION ---
            createScatterChart(sheet, firstDataRow, lastDataRow);

            // --- FILE OUTPUT ---
            try (OutputStream fileOut = Files.newOutputStream(path)) {
                workbook.write(fileOut);
            }
        }
    }

    /**
     * Helper method to generate an embedded Scatter Plot (X/Y Chart).
     *
     * <p><strong>Apache POI XDDF Logic:</strong>
     * Constructing a chart requires specific steps: Patriarch -> Anchor -> Chart -> Axes -> Data Sources -> Plot.</p>
     *
     * @param sheet    The worksheet canvas.
     * @param firstRow The index of the first row containing numerical data.
     * @param lastRow  The index of the last row containing numerical data.
     */
    private void createScatterChart(XSSFSheet sheet, int firstRow, int lastRow) {

        // Drawing Patriarch: The top-level container for all shapes/charts on the sheet.
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        // Anchor: Defines where the chart sits.
        // Args: (dx1, dy1, dx2, dy2, col1, row1, col2, row2)
        // Here we place it starting from Column 4 (E), Row 0, extending to Column 16, Row 34.
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 0, 16, 34);

        // Chart Object Creation
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Solution Distribution");
        chart.setTitleOverlay(false);

        // Legend Configuration
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);

        // Axes Definition
        // Scatter Plots require TWO Value Axes (Bottom/X and Left/Y).
        XDDFValueAxis bottomAxis = chart.createValueAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("X Coordinate");

        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Y Coordinate");

        // Data Source Linking
        // We define the range of cells that populate the axes.
        // Col 1 (B) = X values, Col 2 (C) = Y values.
        XDDFNumericalDataSource<Double> xs = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(firstRow, lastRow, 1, 1));
        XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(firstRow, lastRow, 2, 2));

        // Data Series Configuration
        XDDFScatterChartData data = (XDDFScatterChartData) chart.createData(ChartTypes.SCATTER, bottomAxis, leftAxis);
        XDDFChartData.Series series = data.addSeries(xs, ys);
        series.setTitle("Chromosomes", null);

        // Visual Styling ("No-Line")
        // By default, Excel might try to connect points with lines. We strictly want points only.
        if (series instanceof XDDFScatterChartData.Series scatterSeries) {

            // Marker Style: Circle, Size 5
            scatterSeries.setMarkerStyle(MarkerStyle.CIRCLE);
            scatterSeries.setMarkerSize((short) 5);

            // --- LINE REMOVAL LOGIC ---
            // We must explicitly create a LineProperties object and set its fill to "NoFill".
            XDDFShapeProperties properties = new XDDFShapeProperties();
            XDDFLineProperties lineProperties = new XDDFLineProperties();
            lineProperties.setFillProperties(new XDDFNoFillProperties());

            properties.setLineProperties(lineProperties);
            scatterSeries.setShapeProperties(properties);
            // ------------------------------------------
        }

        // Final Plotting
        chart.plot(data);
    }


}