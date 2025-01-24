import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.Evaluation;

import java.util.Random;

public class WekaLinearRegression {

    public static void main(String[] args) {
        try {

            DataSource source = new DataSource("/Users/dontronolone/Downloads/Cleaned_GLOBAL_DATAFLOW_1995-2023_no_country.arff");  // Update with the path to your dataset
            Instances data = source.getDataSet();

            int classIndex = data.attribute("Under-five_mortality_rate").index();
            data.setClassIndex(classIndex);

            LinearRegression linear = new LinearRegression();
            linear.setRidge(1.0E-8);
            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(linear, data, 10, new Random(1));

            System.out.println("=== Summary ===");
            System.out.println(eval.toSummaryString());
            System.out.println("=== Correlation Coefficient ===");
            System.out.println("Correlation Coefficient: " + eval.correlationCoefficient());
            System.out.println("Mean Absolute Error (MAE): " + eval.meanAbsoluteError());
            System.out.println("Root Mean Squared Error (RMSE): " + eval.rootMeanSquaredError());

            linear.buildClassifier(data);
            System.out.println("=== Linear Regression Model ===");
            System.out.println(linear);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}