import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PearsonCorrelationMatrixCalculator {

    public static void main(String[] args) {
        String filePath = "/Users/dontronolone/Downloads/Cleaned_GLOBAL_DATAFLOW_1995-2023_new.csv";
        String outputFilePath = "/Users/dontronolone/Downloads/correlation_matrix.csv";

        String[] labels = {
                "Under-five mortality rate","Under-five deaths",
                "BMI-for-age <-3 SD","BMI-for-age >+3 SD","Height-for-age <-3 SD (Severe Stunting)","Height-for-age >+3 SD",
                "Weight-for-age (>+3 SD)","Weight-for-height <-3 SD (severe wasting)"
        };

        try {
            double[][] data = loadData(filePath, labels);


            if (data.length == 0 || data[0].length != labels.length) {
                System.err.println("Data shape is incorrect. Ensure each row has the same number of columns as labels.");
                return;
            }

            PearsonsCorrelation correlation = new PearsonsCorrelation();
            double[][] correlationMatrix = correlation.computeCorrelationMatrix(data).getData();

            writeMatrixToCSV(correlationMatrix, labels, outputFilePath);
            System.out.println("Correlation matrix saved to: " + outputFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double[][] loadData(String filePath, String[] labels) throws IOException {
        List<double[]> dataList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line = reader.readLine();
        int rowCount = 0;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(",");
            List<Double> numericValues = new ArrayList<>();

            for (int i = 2; i < tokens.length; i++) {
                try {
                    numericValues.add(Double.parseDouble(tokens[i]));
                } catch (NumberFormatException e) {
                    System.err.println("Non-numeric value encountered in row " + rowCount + ": " + tokens[i]);
                }
            }

            // Ensure the row has the expected number of columns (same as labels.length)
            if (numericValues.size() == labels.length) {
                dataList.add(numericValues.stream().mapToDouble(Double::doubleValue).toArray());
            } else {
                System.err.println("Row " + rowCount + " has incorrect number of columns. Expected " + labels.length);
            }

            rowCount++;
        }
        reader.close();

        // Convert List to 2D array
        double[][] data = new double[dataList.size()][];
        for (int i = 0; i < dataList.size(); i++) {
            data[i] = dataList.get(i);
        }

        return data;
    }


    public static void writeMatrixToCSV(double[][] matrix, String[] labels, String outputFilePath) throws IOException {
        FileWriter csvWriter = new FileWriter(outputFilePath);

        // Write header row
        csvWriter.append(" ,");
        for (String label : labels) {
            csvWriter.append(label).append(",");
        }
        csvWriter.append("\n");

        // Write matrix data with row labels
        for (int i = 0; i < matrix.length; i++) {
            csvWriter.append(labels[i]).append(","); // Row label
            for (int j = 0; j < matrix[i].length; j++) {
                csvWriter.append(String.format("%.4f", matrix[i][j])).append(",");
            }
            csvWriter.append("\n");
        }

        csvWriter.flush();
        csvWriter.close();
    }
}
