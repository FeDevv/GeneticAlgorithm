package org.agroplanner.persistence.implementations.gasystem;

import org.agroplanner.access.model.User;
import org.agroplanner.domainsystem.model.DomainDefinition;
import org.agroplanner.domainsystem.model.DomainType;
import org.agroplanner.gasystem.dao.SolutionDAOContract;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.LoadedSession;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.gasystem.model.SolutionMetadata;
import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.shared.exceptions.DataPersistenceException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * File-based implementation for Solution storage.
 * <p>
 * Saves individual sessions as discrete CSV files in the {@code data/solutions} directory.
 * Embeds metadata (Title, Domain Params) in file headers prefixed with {@code #}.
 * </p>
 */
public class FileSolutionDAO implements SolutionDAOContract {

    private static final String DIR_PATH = "data/solutions";
    // Pattern: sol_u{USER_ID}_{TITLE}_{DATE}.csv
    private static final Pattern FILENAME_PATTERN = Pattern.compile("sol_u(\\d+)_(.*)_(\\d{8}_\\d{6})\\.csv");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public void initStorage() {
        try {
            Path path = Paths.get(DIR_PATH);
            if (!Files.exists(path)) Files.createDirectories(path);
        } catch (IOException e) {
            throw new DataPersistenceException("Unable to init solution directory.", e);
        }
    }

    @Override
    public boolean saveSolution(Individual solution, User owner, String title, DomainDefinition domainDef) {
        String safeTitle = title.replaceAll("[^a-zA-Z0-9]", "_");
        String timestamp = LocalDateTime.now().format(DATE_FMT);
        String fileName = String.format("sol_u%d_%s_%s.csv", owner.getId(), safeTitle, timestamp);
        File file = new File(DIR_PATH + File.separator + fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Metadata Header
            writer.write("# UserID: " + owner.getId()); writer.newLine();
            writer.write("# Title: " + title); writer.newLine();
            writer.write(String.format(Locale.US, "# Final Fitness: %.6f", solution.getFitness())); writer.newLine();

            // Domain Context
            writer.write("# DomainType: " + domainDef.getType().name()); writer.newLine();
            writer.write("# DomainParams: " + mapToString(domainDef.getParameters())); writer.newLine();

            writer.write("# Date: " + LocalDateTime.now()); writer.newLine();
            writer.write("VARIETY_ID;VARIETY_NAME;PLANT_TYPE;X;Y;RADIUS");
            writer.newLine();

            // Phenotype Data
            for (Point p : solution.getChromosomes()) {
                String line = String.format(Locale.US, "%d;%s;%s;%.4f;%.4f;%.4f",
                        p.getVarietyId(),
                        p.getVarietyName(),
                        p.getType().name(),
                        p.getX(),
                        p.getY(),
                        p.getRadius()
                );
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            throw new DataPersistenceException("Error saving solution to file.", e);
        }
    }

    @Override
    public List<SolutionMetadata> findByUser(User user) {
        List<SolutionMetadata> list = new ArrayList<>();
        File folder = new File(DIR_PATH);
        if (!folder.exists()) return list;

        File[] files = folder.listFiles((dir, name) -> name.startsWith("sol_u" + user.getId() + "_") && name.endsWith(".csv"));
        if (files == null) return list;

        for (File f : files) {
            Matcher m = FILENAME_PATTERN.matcher(f.getName());
            if (m.matches()) {
                try {
                    String titleRaw = m.group(2).replace("_", " ");
                    LocalDateTime date = LocalDateTime.parse(m.group(3), DATE_FMT);
                    double fitness = extractFitnessFromHeader(f);
                    // Use filename hash as pseudo-ID for retrieval
                    int fakeId = f.getName().hashCode();
                    list.add(new SolutionMetadata(fakeId, titleRaw, date, fitness));
                } catch (Exception _) {
                    // Log but don't crash
                    System.err.println("Skipping malformed file: " + f.getName());
                }
            }
        }
        // Sort by newest first
        list.sort((a, b) -> b.getCreationDate().compareTo(a.getCreationDate()));
        return list;
    }

    @Override
    public Optional<LoadedSession> loadSolution(int solutionId) {
        File folder = new File(DIR_PATH);
        File[] files = folder.listFiles();
        if (files == null) return Optional.empty();

        // search file comparing hash
        Optional<File> targetOpt = java.util.Arrays.stream(files)
                .filter(f -> f.getName().hashCode() == solutionId)
                .findFirst();

        if (targetOpt.isEmpty()) return Optional.empty();
        File target = targetOpt.get();


        List<Point> points = new ArrayList<>();
        double loadedFitness = 0.0;
        // default
        DomainType docType = DomainType.RECTANGLE;
        Map<String, Double> docParams = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(target))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (!line.isEmpty()) {
                    // if not empty, analyze

                    if (line.startsWith("#")) {
                        // metadata
                        if (line.startsWith("# Final Fitness:")) {
                            loadedFitness = Double.parseDouble(line.split(":")[1].trim());
                        }
                        else if (line.startsWith("# DomainType:")) {
                            try {
                                docType = DomainType.valueOf(line.split(":")[1].trim());
                            } catch (IllegalArgumentException _) {
                                // ignore unkown type, use default
                            }
                        }
                        else if (line.startsWith("# DomainParams:")) {
                            docParams = stringToMap(line.split(":")[1].trim());
                        }
                        // if starts with # and reached here, it is a comment

                    } else if (!line.startsWith("VARIETY_ID")) {
                        // data
                        String[] p = line.split(";");
                        if (p.length >= 6) {
                            int vId = Integer.parseInt(p[0]);
                            String vName = p[1];
                            PlantType type = PlantType.valueOf(p[2]);
                            double x = Double.parseDouble(p[3]);
                            double y = Double.parseDouble(p[4]);
                            double r = Double.parseDouble(p[5]);
                            points.add(new Point(x, y, r, type, vId, vName));
                        }
                    }
                }
            }

            Individual ind = new Individual(points, loadedFitness);
            DomainDefinition def = new DomainDefinition(docType, docParams);
            return Optional.of(new LoadedSession(ind, def));

        } catch (Exception e) {
            throw new DataPersistenceException("Error reading solution file: " + target.getName(), e);
        }
    }

    // --- HELPER ---
    private String mapToString(Map<String, Double> map) {
        return map.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(";"));
    }

    private Map<String, Double> stringToMap(String s) {
        Map<String, Double> map = new HashMap<>();
        if (s == null || s.isEmpty()) return map;
        String[] pairs = s.split(";");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                try {
                    map.put(kv[0], Double.parseDouble(kv[1]));
                } catch (NumberFormatException _) {}
            }
        }
        return map;
    }

    private double extractFitnessFromHeader(File f) {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            int limit = 0;
            while ((line = br.readLine()) != null && limit < 10) {
                if (line.startsWith("# Final Fitness:")) {
                    return Double.parseDouble(line.split(":")[1].trim());
                }
                limit++;
            }
        } catch (Exception _) {}
        return 0.0;
    }
}
