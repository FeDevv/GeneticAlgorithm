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

public class ExcelExporter extends BaseExporter {

    private static final String[] HEADERS = {"ID", "X", "Y"};

    @Override
    protected String getExtension() {
        return ".xlsx"; // Formato Excel
    }

    @Override
    protected void performExport(Individual individual, Domain domain, double radius, Path path) throws IOException {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Nota: Dobbiamo usare XSSFSheet (specifico XML) per il supporto drawing
            XSSFSheet sheet = workbook.createSheet("Solution Data");

            // --- STILI ---
            CellStyle sixDecimalStyle = workbook.createCellStyle();
            sixDecimalStyle.setDataFormat(workbook.createDataFormat().getFormat("0.000000"));

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // --- METADATI ---
            int rowIndex = 0;

            // Riga 0: Info Dominio
            Row domainRow = sheet.createRow(rowIndex++);
            domainRow.createCell(0).setCellValue("Domain Info:");
            domainRow.createCell(1).setCellValue(domain.toString());

            // Riga 1: Raggio
            Row radiusRow = sheet.createRow(rowIndex++);
            radiusRow.createCell(0).setCellValue("Target Distance:");
            radiusRow.createCell(1).setCellValue(radius);

            // Riga 2: Totale Punti
            Row pointsRow = sheet.createRow(rowIndex++);
            pointsRow.createCell(0).setCellValue("Total Points:");
            pointsRow.createCell(1).setCellValue(individual.getChromosomes().size());

            // Riga 3: Fitness
            Row fitnessRow = sheet.createRow(rowIndex++);
            fitnessRow.createCell(0).setCellValue("Fitness:");
            Cell fitnessCell = fitnessRow.createCell(1);
            fitnessCell.setCellValue(individual.getFitness());
            fitnessCell.setCellStyle(sixDecimalStyle);

            rowIndex++; // Riga vuota

            // --- HEADER TABELLA ---
            Row headerRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // --- DATI ---
            List<Point> points = individual.getChromosomes();
            int idCounter = 0;
            int firstDataRow = rowIndex; // Prima riga di dati veri (per il grafico)

            for (Point point : points) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(idCounter++);
                row.createCell(1).setCellValue(point.getX());
                row.createCell(2).setCellValue(point.getY());
            }

            int lastDataRow = rowIndex - 1; // Ultima riga di dati

            // --- AUTO-SIZE COLONNE ---
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // --- GENERAZIONE GRAFICO ---
            // Creiamo il grafico
            createScatterChart(sheet, firstDataRow, lastDataRow);

            // --- SALVATAGGIO ---
            try (OutputStream fileOut = Files.newOutputStream(path)) {
                workbook.write(fileOut);
            }
        }
    }

    /**
     * Metodo helper per creare il grafico a dispersione (Scatter Plot).
     * Disegna un grafico X/Y collegato ai dati delle colonne B (X) e C (Y).
     *
     * @param sheet Il foglio di lavoro su cui disegnare.
     * @param firstRow Indice della prima riga contenente dati numerici.
     * @param lastRow Indice dell'ultima riga contenente dati numerici.
     */
    private void createScatterChart(XSSFSheet sheet, int firstRow, int lastRow) {

        // 1. Creiamo l'area di disegno (Drawing Patriarch)
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        // 2. Definiamo la posizione e dimensione del grafico (Anchor)
        // Colonna 4 (E) -> Colonna 14 (P) | Riga 0 -> Riga 28
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 0, 14, 28);

        // 3. Creiamo l'oggetto grafico vuoto
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Solution Distribution");
        chart.setTitleOverlay(false);

        // 4. Creiamo la Legenda
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);

        // 5. Definiamo gli Assi
        // Scatter Plot richiede DUE assi di valore (Value Axis), non Categorie.
        XDDFValueAxis bottomAxis = chart.createValueAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("X Coordinate");

        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Y Coordinate");

        // 6. Definiamo le Sorgenti Dati (Data Sources)
        // Usiamo XDDFNumericalDataSource per specificare che sono numeri Double
        // Colonna 1 = X, Colonna 2 = Y
        XDDFNumericalDataSource<Double> xs = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(firstRow, lastRow, 1, 1));
        XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(firstRow, lastRow, 2, 2));

        // 7. Configuriamo i dati del grafico
        XDDFScatterChartData data = (XDDFScatterChartData) chart.createData(ChartTypes.SCATTER, bottomAxis, leftAxis);
        XDDFChartData.Series series = data.addSeries(xs, ys);
        series.setTitle("Chromosomes", null);

        // Cast sicuro e configurazione stile
        if (series instanceof XDDFScatterChartData.Series scatterSeries) {

            // Stile dei punti (Pallini)
            scatterSeries.setMarkerStyle(MarkerStyle.CIRCLE);
            scatterSeries.setMarkerSize((short) 5);

            // --- RIMUOVERE LA LINEA DI CONNESSIONE ---
            // 1. Creiamo un oggetto per le proprietà della forma
            XDDFShapeProperties properties = new XDDFShapeProperties();

            // 2. Creiamo un oggetto per le proprietà della linea
            XDDFLineProperties lineProperties = new XDDFLineProperties();

            // 3. Diciamo che il riempimento della linea è "NESSUNO" (Invisibile)
            lineProperties.setFillProperties(new XDDFNoFillProperties());

            // 4. Applichiamo le proprietà alla serie
            properties.setLineProperties(lineProperties);
            scatterSeries.setShapeProperties(properties);
            // ------------------------------------------
        }

        // 8. Disegniamo il grafico
        chart.plot(data);
    }


}