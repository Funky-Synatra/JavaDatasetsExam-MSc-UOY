import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.apache.commons.csv.*;

public class SimulatedAnnealingMerge {

    // Initialize the temperature and cooling variables for the simulated annealing algorithm.
    private static final double INITIAL_TEMPERATURE = 100.0;
    private static final double COOLING_RATE = 0.95;
     // Initialize the number of iterations for the simulated annealing algorithm.
    private static final int NUM_ITERATIONS = 10;

    public static void main(String[] args) throws IOException {
        // Defines the input paths for the CSV files
        String filePath1 = "/Users/dontronolone/Downloads/Child_mortality_rates_Global.csv";
        String filePath2 = "/Users/dontronolone/Downloads/Infant_nutrition_data_by_country.csv";
        // Defines the output path for the merged file.
        String outputPath = "/Users/dontronolone/Downloads/merged_output.csv";

        // Loads the data from the two input files
        List<Map<String, String>> data1 = readCsv(filePath1);
        List<Map<String, String>> data2 = readCsv(filePath2);

        // Merges the datasets using a simulated annealing algorithm
        List<Map<String, String>> mergedData = simulatedAnnealingMerge(data1, data2);
        // Writes the merged data to a CSV
        writeCsv(mergedData, outputPath);
    }

    // Reads a CSV file using Apache commons CSV library and returns it as a list of hashmaps, where the keys are the column names.
    public static List<Map<String, String>> readCsv(String filePath) throws IOException {
        // Initializes the list that contains all of the lines of the files as hashmaps
        List<Map<String, String>> data = new ArrayList<>();
        // Uses the try-with-resources to handle the readers and the csvParser, so they're closed automatically.
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
                 // Loops through all the records of the CSV file.
            for (CSVRecord record : csvParser) {
                 // Creates a hashmap to represent the row
                Map<String, String> row = new HashMap<>();
                // Adds the data from the CSVRecord to the HashMap, replacing null values with empty strings
                record.toMap().forEach((k, v) -> row.put(k, v == null ? "" : v));
                // adds the current row to the list of rows
                data.add(row);
            }
        }
         // returns the data from the CSV file
        return data;
    }

    // Merges the two datasets using a simulated annealing approach to find the optimal merge.
    public static List<Map<String, String>> simulatedAnnealingMerge(List<Map<String, String>> data1, List<Map<String, String>> data2) {
        // Creates a set of headers, that includes the keys of both datasets. It uses a hash set, so it prevents duplicates
        Set<String> headers = new HashSet<>(data1.get(0).keySet());
        headers.addAll(data2.get(0).keySet());

        // Performs a initial outer join of the two datasets, and uses this as a base for the algorithm.
        List<Map<String, String>> currentMerge = outerJoin(data1, data2, headers);
         // Initializes the best merge, using the current merge
        List<Map<String, String>> bestMerge = new ArrayList<>(currentMerge);
        // Calculates the score, that will be used as a threshold for improvements. The less data is missing, the better it is.
        double bestMissingDataScore = calculateMissingDataScore(bestMerge, headers);

        // Initializes the initial temperature of the algorithm
        double temperature = INITIAL_TEMPERATURE;

        // Loops for the number of defined iterations.
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            // Copies the current merge (used as a base for the calculations of each iteration)
            List<Map<String, String>> tempMerge = new ArrayList<>(currentMerge);
            // Selects a column to drop, which is done based on the number of missing values.
            String columnToDrop = selectColumnToDrop(tempMerge, headers);

            // if there was a column to drop
            if (columnToDrop != null) {
                 // drop this column in the tempMerge, which is used for the current iteration of the algorithm.
                dropColumn(tempMerge, columnToDrop);
                 // Calculates the score of the missing data in the new state.
                double newMissingDataScore = calculateMissingDataScore(tempMerge, headers);

                // Calculates the delta between the score of the new state and the current best score.
                double deltaScore = newMissingDataScore - bestMissingDataScore;
                 // calculates the acceptance probability, based on the delta, and the temperature.
                double acceptanceProbability = deltaScore > 0 ? Math.exp(-deltaScore / temperature) : 1.0;

                // Checks if the new state will be accepted based on the result of Math.random() and the acceptance probability.
                if (Math.random() < acceptanceProbability) {
                     // If the new state was accepted, assign the result to the current merge.
                    currentMerge = new ArrayList<>(tempMerge);
                    // Also checks if the new state had a better score, so it can become the best state.
                    if (newMissingDataScore < bestMissingDataScore) {
                        bestMerge = new ArrayList<>(currentMerge);
                        bestMissingDataScore = newMissingDataScore;
                    }
                }
            }
            // Decreases the temperature based on the cooling rate.
            temperature *= COOLING_RATE;
        }
         // returns the best found solution.
        return bestMerge;
    }

    // Performs a outer join of the two datasets, based on the "Country" and the "Year" columns.
    public static List<Map<String, String>> outerJoin(List<Map<String, String>> data1, List<Map<String, String>> data2, Set<String> headers) {
        // Creates a list that will contain the joined values from both datasets
        List<Map<String, String>> merged = new ArrayList<>();
        // Creates a map that will work as a secondary index for the second dataset, which is faster.
        Map<String, Map<String, String>> data2Map = new HashMap<>();

        // loops all the rows of the second dataset.
        for (Map<String, String> row : data2) {
            // Creates a key using the Country and Year of each dataset
            String key = row.get("Country") + "-" + row.get("Year");
             // saves each row, indexed by the key generated above.
            data2Map.put(key, row);
        }

         // loops all the rows of the first dataset.
        for (Map<String, String> row : data1) {
             // Creates a key using the Country and Year of each dataset
            String key = row.get("Country") + "-" + row.get("Year");
            // creates a new hashmap to contain the current merge. It’s a copy of the row from the first dataset.
            Map<String, String> mergedRow = new HashMap<>(row);

            // checks if the data2Map contains the specified key, which means that we have to merge the two rows.
            if (data2Map.containsKey(key)) {
                // if the key is there, get the other corresponding row.
                Map<String, String> data2Row = data2Map.get(key);
                // adds all of the values from data2 to this new row, but only if they are not already defined.
                for (String header : headers) {
                    mergedRow.putIfAbsent(header, data2Row.getOrDefault(header, ""));
                }
            } else {
                 // If there is no corresponding key in data2, adds all of the headers with a blank value.
                headers.forEach(header -> mergedRow.putIfAbsent(header, ""));
            }
            // adds the merged row to the final list
            merged.add(mergedRow);
        }

        // Loops through all remaining elements of data2, that were not merged before (used in cases where a certain row only exists in the second dataset).
        for (Map.Entry<String, Map<String, String>> entry : data2Map.entrySet()) {
            // if no row in the merged list has the same key as the current entry
            if (!merged.stream().anyMatch(row -> (row.get("Country") + "-" + row.get("Year")).equals(entry.getKey()))) {
                 // creates a new row copying the values of data2
                Map<String, String> mergedRow = new HashMap<>(entry.getValue());
                 // adds all of the headers to this row with a blank value (similar to the code above)
                headers.forEach(header -> mergedRow.putIfAbsent(header, ""));
                // adds the new row to the list.
                merged.add(mergedRow);
            }
        }
        // returns the outer joined data.
        return merged;
    }

     // Calculates a score based on the number of missing values in the dataset. Lower scores are better.
    public static double calculateMissingDataScore(List<Map<String, String>> data, Set<String> headers) {
        // Initializes the number of missing values as 0
        int missingValues = 0;
        // loops all the rows in the data
        for (Map<String, String> row : data) {
             // loops all the headers to check if the value exist, and if it’s empty.
            for (String header : headers) {
                 // checks if the value exists for the current header, and also if it's an empty string, and increments the missing value variable.
                if (!row.containsKey(header) || row.get(header).isEmpty()) {
                    missingValues++;
                }
            }
        }
        // returns the total number of missing values.
        return missingValues;
    }

    // Given a dataset, and a set of headers, selects the best column to drop. This is based on which has the most missing values.
    public static String selectColumnToDrop(List<Map<String, String>> data, Set<String> headers) {
        // Creates a hashmap to store the number of missing values per column.
        Map<String, Integer> nullCounts = new HashMap<>();
        // Initializes the hashmap, assigning 0 missing values for all of the headers.
        headers.forEach(header -> nullCounts.put(header, 0));

        // Loops all the rows of the dataset.
        for (Map<String, String> row : data) {
             // Loops all headers for the current row.
            for (String header : headers) {
                // checks if the value doesn’t exist or if it’s empty, and increments the counter.
                if (row.get(header) == null || row.get(header).isEmpty()) {
                    nullCounts.put(header, nullCounts.get(header) + 1);
                }
            }
        }

        // Returns the column name that has the most missing values.
        return nullCounts.entrySet().stream()
                 // filters the columns that have more than 0 missing values.
                .filter(entry -> entry.getValue() > 0)
                 // sorts by value, and gets the max.
                .max(Map.Entry.comparingByValue())
                // if it exists, get the key (column name) and returns it.
                .map(Map.Entry::getKey)
                // returns null if no value exists (all values are not missing).
                .orElse(null);
    }

    // Drops a specified column in a dataset.
    public static void dropColumn(List<Map<String, String>> data, String column) {
        // loops all the rows in the dataset
        for (Map<String, String> row : data) {
            // removes the specified column from the row.
            row.remove(column);
        }
    }

    // Writes data to a CSV file, using a specified path.
    public static void writeCsv(List<Map<String, String>> data, String outputPath) throws IOException {
        // If the dataset is empty, it does nothing.
        if (data.isEmpty()) return;
        // Uses the try-with-resources, so the writer and csvPrinter are closed automatically.
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(data.get(0).keySet().toArray(new String[0])))) {
                 // loops through all the rows, and print the values on the csv file.
            for (Map<String, String> row : data) {
                csvPrinter.printRecord(row.values());
            }
        }
    }
}
