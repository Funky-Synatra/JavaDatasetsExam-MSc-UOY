import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.apache.commons.csv.*;

public class SimulatedAnnealingMerge {

    private static final double INITIAL_TEMPERATURE = 100.0;
    private static final double COOLING_RATE = 0.95;
    private static final int NUM_ITERATIONS = 10;

    public static void main(String[] args) throws IOException {
        String filePath1 = "/Users/dontronolone/Downloads/Child_mortality_rates_Global.csv";
        String filePath2 = "/Users/dontronolone/Downloads/Infant_nutrition_data_by_country.csv";
        String outputPath = "/Users/dontronolone/Downloads/merged_output.csv";

        List<Map<String, String>> data1 = readCsv(filePath1);
        List<Map<String, String>> data2 = readCsv(filePath2);

        List<Map<String, String>> mergedData = simulatedAnnealingMerge(data1, data2);
        writeCsv(mergedData, outputPath);
    }

    public static List<Map<String, String>> readCsv(String filePath) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : csvParser) {
                Map<String, String> row = new HashMap<>();
                record.toMap().forEach((k, v) -> row.put(k, v == null ? "" : v));
                data.add(row);
            }
        }
        return data;
    }

    public static List<Map<String, String>> simulatedAnnealingMerge(List<Map<String, String>> data1, List<Map<String, String>> data2) {
        Set<String> headers = new HashSet<>(data1.get(0).keySet());
        headers.addAll(data2.get(0).keySet());

        List<Map<String, String>> currentMerge = outerJoin(data1, data2, headers);
        List<Map<String, String>> bestMerge = new ArrayList<>(currentMerge);
        double bestMissingDataScore = calculateMissingDataScore(bestMerge, headers);

        double temperature = INITIAL_TEMPERATURE;

        for (int i = 0; i < NUM_ITERATIONS; i++) {
            List<Map<String, String>> tempMerge = new ArrayList<>(currentMerge);
            String columnToDrop = selectColumnToDrop(tempMerge, headers);

            if (columnToDrop != null) {
                dropColumn(tempMerge, columnToDrop);
                double newMissingDataScore = calculateMissingDataScore(tempMerge, headers);

                double deltaScore = newMissingDataScore - bestMissingDataScore;
                double acceptanceProbability = deltaScore > 0 ? Math.exp(-deltaScore / temperature) : 1.0;

                if (Math.random() < acceptanceProbability) {
                    currentMerge = new ArrayList<>(tempMerge);
                    if (newMissingDataScore < bestMissingDataScore) {
                        bestMerge = new ArrayList<>(currentMerge);
                        bestMissingDataScore = newMissingDataScore;
                    }
                }
            }
            temperature *= COOLING_RATE;
        }
        return bestMerge;
    }

    public static List<Map<String, String>> outerJoin(List<Map<String, String>> data1, List<Map<String, String>> data2, Set<String> headers) {
        List<Map<String, String>> merged = new ArrayList<>();
        Map<String, Map<String, String>> data2Map = new HashMap<>();

        for (Map<String, String> row : data2) {
            String key = row.get("Country") + "-" + row.get("Year");
            data2Map.put(key, row);
        }

        for (Map<String, String> row : data1) {
            String key = row.get("Country") + "-" + row.get("Year");
            Map<String, String> mergedRow = new HashMap<>(row);

            if (data2Map.containsKey(key)) {
                Map<String, String> data2Row = data2Map.get(key);
                for (String header : headers) {
                    mergedRow.putIfAbsent(header, data2Row.getOrDefault(header, ""));
                }
            } else {
                headers.forEach(header -> mergedRow.putIfAbsent(header, ""));
            }
            merged.add(mergedRow);
        }

        for (Map.Entry<String, Map<String, String>> entry : data2Map.entrySet()) {
            if (!merged.stream().anyMatch(row -> (row.get("Country") + "-" + row.get("Year")).equals(entry.getKey()))) {
                Map<String, String> mergedRow = new HashMap<>(entry.getValue());
                headers.forEach(header -> mergedRow.putIfAbsent(header, ""));
                merged.add(mergedRow);
            }
        }
        return merged;
    }

    public static double calculateMissingDataScore(List<Map<String, String>> data, Set<String> headers) {
        int missingValues = 0;
        for (Map<String, String> row : data) {
            for (String header : headers) {
                if (!row.containsKey(header) || row.get(header).isEmpty()) {
                    missingValues++;
                }
            }
        }
        return missingValues;
    }

    public static String selectColumnToDrop(List<Map<String, String>> data, Set<String> headers) {
        Map<String, Integer> nullCounts = new HashMap<>();
        headers.forEach(header -> nullCounts.put(header, 0));

        for (Map<String, String> row : data) {
            for (String header : headers) {
                if (row.get(header) == null || row.get(header).isEmpty()) {
                    nullCounts.put(header, nullCounts.get(header) + 1);
                }
            }
        }

        return nullCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public static void dropColumn(List<Map<String, String>> data, String column) {
        for (Map<String, String> row : data) {
            row.remove(column);
        }
    }

    public static void writeCsv(List<Map<String, String>> data, String outputPath) throws IOException {
        if (data.isEmpty()) return;

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(data.get(0).keySet().toArray(new String[0])))) {
            for (Map<String, String> row : data) {
                csvPrinter.printRecord(row.values());
            }
        }
    }
}
