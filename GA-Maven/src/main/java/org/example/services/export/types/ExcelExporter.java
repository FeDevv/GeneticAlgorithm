package org.example.services.export.types;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.Individual;
import org.example.model.Point;
import org.example.model.domains.Domain;
import org.example.services.export.BaseExporter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ExcelExporter extends BaseExporter {

    private static final String[] HEADERS = {"ID", "X", "Y"};

    @Override
    protected String getExtension() {
        return ".xlsx"; // Formato Excel moderno
    }

    @Override
    protected void performExport(Individual individual, Domain domain, double radius, Path path) throws IOException {

        // 1. Creiamo il Workbook (il file Excel in memoria)
        try (Workbook workbook = new XSSFWorkbook()) {

            // 2. Creiamo un Foglio (Sheet)
            Sheet sheet = workbook.createSheet("Solution Data");

            // --- METADATI (Righe iniziali) ---
            int rowIndex = 0;

            // Riga 0: Info Dominio
            Row domainRow = sheet.createRow(rowIndex++);
            domainRow.createCell(0).setCellValue("Domain Info:");
            domainRow.createCell(1).setCellValue(domain.toString());

            // Riga 1: Raggio (distanza tra i punti)
            Row radiusRow = sheet.createRow(rowIndex++);
            radiusRow.createCell(0).setCellValue("Target Distance:");
            radiusRow.createCell(1).setCellValue(radius);

            // Riga 2: Numero otale di punti
            Row pointsRow = sheet.createRow(rowIndex++);
            pointsRow.createCell(0).setCellValue("Total Points:");
            pointsRow.createCell(1).setCellValue(individual.getDimension());

            // Riga 3: Fitness
            Row fitnessRow = sheet.createRow(rowIndex++);
            fitnessRow.createCell(0).setCellValue("Fitness:");
            fitnessRow.createCell(1).setCellValue(individual.getFitness());

            // Riga 4: Vuota (spaziatrice)
            rowIndex++;

            // --- HEADER TABELLA DATI ---
            Row headerRow = sheet.createRow(rowIndex++);

            // Stile per l'header (Grassetto) - Opzionale ma carino
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // --- DATI (Punti) ---
            List<Point> points = individual.getChromosomes();
            int idCounter = 0;

            for (Point point : points) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(idCounter++); // ID
                row.createCell(1).setCellValue(point.getX()); // X
                row.createCell(2).setCellValue(point.getY()); // Y
            }

            // --- AUTO-SIZE COLONNE ---
            // Adatta la larghezza delle colonne al contenuto (così si legge bene)
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 3. Scrittura fisica su disco
            // BaseExporter ci ha dato il 'path', noi apriamo uno stream verso quel path.
            try (OutputStream fileOut = Files.newOutputStream(path)) {
                workbook.write(fileOut);
            }
        }
    }
}
