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
        // Define the path to the input CSV file
        String csvFilePath = "/Users/dontronolone/Downloads/Cleaned_GLOBAL_DATAFLOW_1995-2023_new.csv"; // Update this path
        // Define the path to the output ARFF file
        String arffFilePath = "/Users/dontronolone/Downloads/Cleaned_GLOBAL_DATAFLOW_1995-2023_new.arff"; // Specify output path

        try {
            // Load data from the CSV file into a List of String arrays.
            List<String[]> data = loadData(csvFilePath);
            // Write the data in ARFF format into the specified file.
            writeARFF(data, arffFilePath);
            // Prints a message to the console to confirm the process was completed.
            System.out.println("ARFF file saved to: " + arffFilePath);
        } catch (IOException e) {
            // If there's an exception during the process (like file not found, or invalid file format), print the stack trace.
            e.printStackTrace();
        }
    }

    // Reads a csv file from a specified path, and returns a list of arrays, where each array is a line from the file
    public static List<String[]> loadData(String csvFilePath) throws IOException {
        // Creates a list to store the data read from the csv
        List<String[]> dataList = new ArrayList<>();
        // Creates a buffered reader to read the file line by line
        BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));

        String line;
        // While there are still lines in the file
        while ((line = reader.readLine()) != null) {
            // Splits the line by a comma and saves the resulting array
            String[] tokens = line.split(",");
            // Adds this new array (representing a line) to the list of lines.
            dataList.add(tokens);
        }
        // Closes the buffered reader.
        reader.close();
        // Returns the list with all of the data.
        return dataList;
    }

    // Receives a list of String arrays, and saves them in an ARFF file, according to the format used by WEKA
    public static void writeARFF(List<String[]> data, String arffFilePath) throws IOException {
        // Create a writer for an ARFF file in a specific location
        FileWriter arffWriter = new FileWriter(arffFilePath);

        // Write ARFF header, which defines the name of the relation
        arffWriter.write("@relation cleaned_global_dataflow\n\n");

        // Define the attributes, by fetching the headers from the first line of the file
        String[] headers = data.get(0);

        // Collect all unique country names for the nominal attribute. This uses a hashset, so it guarantees uniqueness
        Set<String> countryNames = new HashSet<>();
        // loops all of the rows (except for the first, which is the header).
        for (int i = 1; i < data.size(); i++) {
            // gets the name of the country (first element of the array)
            String country = data.get(i)[0];
            // if it's not an empty string, add it to the hash set.
            if (!country.isEmpty()) {
                countryNames.add(country);
            }
        }

        // Define "Country Name" as nominal with single quotes around each country name and no trailing comma
        arffWriter.write("@attribute Country_Name {");
        // adds all of the countries found to the attributes, separated by commas. Each country is between single quotes.
        arffWriter.write(countryNames.stream().map(name -> "'" + name + "'").collect(Collectors.joining(",")));
        arffWriter.write("}\n");

        // Define other attributes as numeric by looping the other headers and generating the @attribute section for each.
        for (int i = 1; i < headers.length; i++) {
            arffWriter.write("@attribute " + headers[i].replace(" ", "_") + " numeric\n");
        }
        // writes a line break and the declaration of @data, which means that after this line the actual data is coming.
        arffWriter.write("\n@data\n");

        // Write data rows
        // loops all of the data, skipping the first line (which is the header)
        for (int i = 1; i < data.size(); i++) {
            // gets the current line.
            String[] row = data.get(i);
            // Writes the Country Name, or ? if it's empty. The ? character is a placeholder for missing data, and it's the standard format for ARFF files.
            arffWriter.write(row[0].isEmpty() ? "?" : "'" + row[0] + "'");
            // Loops all other values on the same line, and saves them after a comma, also using ? if there's missing data.
            for (int j = 1; j < row.length; j++) {
                arffWriter.write(",");
                arffWriter.write(row[j].isEmpty() ? "?" : row[j]); // Use "?" for missing values
            }
            // adds a line break at the end of each row.
            arffWriter.write("\n");
        }

        // Closes the filewriter.
        arffWriter.close();
    }
}
