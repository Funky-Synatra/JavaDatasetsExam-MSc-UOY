import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CSVToARFFConverter {

    public static void main(String[] args) {
        String csvFilePath = "/Users/dontronolone/Downloads/Cleaned_GLOBAL_DATAFLOW_1995-2023_new.csv"; // Update this path
        String arffFilePath = "/Users/dontronolone/Downloads/Cleaned_GLOBAL_DATAFLOW_1995-2023_new.arff"; // Specify output path

        try {
            List<String[]> data = loadData(csvFilePath);
            writeARFF(data, arffFilePath);
            System.out.println("ARFF file saved to: " + arffFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String[]> loadData(String csvFilePath) throws IOException {
        List<String[]> dataList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));

        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(",");
            dataList.add(tokens);
        }
        reader.close();
        return dataList;
    }

    public static void writeARFF(List<String[]> data, String arffFilePath) throws IOException {
        FileWriter arffWriter = new FileWriter(arffFilePath);

        // Write ARFF header
        arffWriter.write("@relation cleaned_global_dataflow\n\n");

        // Define attributes
        String[] headers = data.get(0);

        // Collect all unique country names for the nominal attribute
        Set<String> countryNames = new HashSet<>();
        for (int i = 1; i < data.size(); i++) {
            String country = data.get(i)[0];
            if (!country.isEmpty()) {
                countryNames.add(country);
            }
        }

        // Define "Country Name" as nominal with single quotes around each country name and no trailing comma
        arffWriter.write("@attribute Country_Name {");
        arffWriter.write(countryNames.stream().map(name -> "'" + name + "'").collect(Collectors.joining(",")));
        arffWriter.write("}\n");

        // Define other attributes as numeric
        for (int i = 1; i < headers.length; i++) {
            arffWriter.write("@attribute " + headers[i].replace(" ", "_") + " numeric\n");
        }
        arffWriter.write("\n@data\n");

        // Write data rows
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            arffWriter.write(row[0].isEmpty() ? "?" : "'" + row[0] + "'"); // Write Country Name or "?" for missing
            for (int j = 1; j < row.length; j++) {
                arffWriter.write(",");
                arffWriter.write(row[j].isEmpty() ? "?" : row[j]); // Use "?" for missing values
            }
            arffWriter.write("\n");
        }

        arffWriter.close();
    }
}
