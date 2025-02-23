import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PearsonCorrelationMatrixCalculator {

    public static void main(String[] args) {
        // Define the path to the input CSV file
        String filePath = "/Users/dontronolone/Downloads/Cleaned_GLOBAL_DATAFLOW_1995-2023_new.csv";
        // Define the path to the output CSV file for the matrix
        String outputFilePath = "/Users/dontronolone/Downloads/correlation_matrix.csv";

        // Define the labels to be used for the correlation matrix.
        String[] labels = {
                "Under-five mortality rate","Under-five deaths",
                "BMI-for-age <-3 SD","BMI-for-age >+3 SD","Height-for-age <-3 SD (Severe Stunting)","Height-for-age >+3 SD",
                "Weight-for-age (>+3 SD)","Weight-for-height <-3 SD (severe wasting)"
        };

        try {
            // Load the data from the CSV, according to the provided labels.
            double[][] data = loadData(filePath, labels);

            // Checks for possible errors when loading data (if there's no data or if the data shape is incorrect)
            if (data.length == 0 || data[0].length != labels.length) {
                System.err.println("Data shape is incorrect. Ensure each row has the same number of columns as labels.");
                return;
            }

            // Creates a new Pearson Correlation object from Apache Commons Library
            PearsonsCorrelation correlation = new PearsonsCorrelation();
            // Calculates the correlation matrix using all the loaded data and gets the matrix as a 2d array.
            double[][] correlationMatrix = correlation.computeCorrelationMatrix(data).getData();

            // Writes the correlation matrix to the CSV file.
            writeMatrixToCSV(correlationMatrix, labels, outputFilePath);
            // Prints a confirmation message on the console.
            System.out.println("Correlation matrix saved to: " + outputFilePath);

        } catch (IOException e) {
            // If there is an error, the stack trace is printed.
            e.printStackTrace();
        }
    }

    // Load data from a CSV file, filtering the data based on the provided labels.
    public static double[][] loadData(String filePath, String[] labels) throws IOException {
        // Create a new list that will hold each row of data as an array of doubles
        List<double[]> dataList = new ArrayList<>();
        // Creates a buffered reader to read the csv files line by line.
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        // reads the first line (headers) and discards it.
        String line = reader.readLine();
        int rowCount = 0;
        // loops through all of the lines of the file.
        while ((line = reader.readLine()) != null) {
            // splits the line using the comma separator
            String[] tokens = line.split(",");
            // creates a list of doubles to store the double values from each line
            List<Double> numericValues = new ArrayList<>();

            // loops through all of the elements of each line (starting at index 2, since the first two are not numbers).
            for (int i = 2; i < tokens.length; i++) {
                try {
                    // tries to parse the string to a double, and if possible add it to the list
                    numericValues.add(Double.parseDouble(tokens[i]));
                } catch (NumberFormatException e) {
                    // if it's not a number, it throws an error on the console, showing which line has a non numeric value.
                    System.err.println("Non-numeric value encountered in row " + rowCount + ": " + tokens[i]);
                }
            }

            // Ensure the row has the expected number of columns (same as labels.length)
            if (numericValues.size() == labels.length) {
               // If it has the same number of labels, converts the list of doubles to an array of doubles, and saves it to the data list.
                dataList.add(numericValues.stream().mapToDouble(Double::doubleValue).toArray());
            } else {
                 // if the list doesn’t have the correct number of columns, print the error message with the row count.
                System.err.println("Row " + rowCount + " has incorrect number of columns. Expected " + labels.length);
            }

            rowCount++;
        }
        // Closes the reader.
        reader.close();

        // Convert List to 2D array
        // Creates a matrix (2d array of doubles) to store all of the collected values.
        double[][] data = new double[dataList.size()][];
        // loops all the elements of the list and saves them in the new 2d array.
        for (int i = 0; i < dataList.size(); i++) {
            data[i] = dataList.get(i);
        }
        // returns the 2d array
        return data;
    }

    // write the correlation matrix to a csv file, using a given path, and a set of labels for the columns and the rows.
    public static void writeMatrixToCSV(double[][] matrix, String[] labels, String outputFilePath) throws IOException {
        // Creates a new filewriter based on the output path
        FileWriter csvWriter = new FileWriter(outputFilePath);

        // Write header row, using the defined labels.
        csvWriter.append(" ,");
         // Adds each label to the first line of the file.
        for (String label : labels) {
            csvWriter.append(label).append(",");
        }
        // Adds a line break after finishing the header.
        csvWriter.append("\n");

        // Write matrix data with row labels, looping for each row in the matrix.
        for (int i = 0; i < matrix.length; i++) {
            // Adds the label to the beginning of each row
            csvWriter.append(labels[i]).append(","); // Row label
            // loops through all the elements in the row, formatting them and adding them separated by commas.
            for (int j = 0; j < matrix[i].length; j++) {
                csvWriter.append(String.format("%.4f", matrix[i][j])).append(",");
            }
             // adds a line break at the end of each row.
            csvWriter.append("\n");
        }

        // Flush the writer
        csvWriter.flush();
         // Closes the writer.
        csvWriter.close();
    }
}
