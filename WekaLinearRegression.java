import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.Evaluation;

import java.util.Random;

public class WekaLinearRegression {

    public static void main(String[] args) {
        try {
            // Load the dataset using the WEKA DataSource
            DataSource source = new DataSource("/Users/dontronolone/Downloads/Cleaned_GLOBAL_DATAFLOW_1995-2023_no_country.arff");  // Update with the path to your dataset
            // Gets the data from the source
            Instances data = source.getDataSet();

             // Sets the class index (target variable), in this case, is Under-five_mortality_rate.
            int classIndex = data.attribute("Under-five_mortality_rate").index();
            data.setClassIndex(classIndex);

            // Creates a linear regression model
            LinearRegression linear = new LinearRegression();
            // Sets a small value for Ridge regularization
            linear.setRidge(1.0E-8);
             // Creates a new Evaluation object, to evaluate the results of the model
            Evaluation eval = new Evaluation(data);
            // Performs a 10 fold cross validation on the dataset, using a new Random object with seed=1.
            eval.crossValidateModel(linear, data, 10, new Random(1));

             // Prints the summary of the cross validation, with all of the metrics
            System.out.println("=== Summary ===");
            System.out.println(eval.toSummaryString());
              // Prints the Correlation Coefficient of the model.
            System.out.println("=== Correlation Coefficient ===");
            System.out.println("Correlation Coefficient: " + eval.correlationCoefficient());
            // Prints the Mean Absolute Error (MAE) of the model.
            System.out.println("Mean Absolute Error (MAE): " + eval.meanAbsoluteError());
             // Prints the Root Mean Squared Error (RMSE) of the model.
            System.out.println("Root Mean Squared Error (RMSE): " + eval.rootMeanSquaredError());

            // Build a linear regression classifier using the loaded data.
            linear.buildClassifier(data);
             // Prints the regression equation of the model.
            System.out.println("=== Linear Regression Model ===");
            System.out.println(linear);

        } catch (Exception e) {
            // If there is an error, the stack trace is printed
            e.printStackTrace();
        }
    }
}
