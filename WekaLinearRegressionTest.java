import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.Evaluation;

public class WekaLinearRegressionTest {

    public static void main(String[] args) {
        try {
            DataSource sourceTrain = new DataSource("/Users/dontronolone/Downloads/trainData.arff");
            Instances trainData = sourceTrain.getDataSet();
            trainData.setClassIndex(trainData.numAttributes() - 1);

            DataSource sourceTest = new DataSource("/Users/dontronolone/Downloads/testData.arff");
            Instances testData = sourceTest.getDataSet();
            testData.setClassIndex(testData.numAttributes() - 1);

            LinearRegression linear = new LinearRegression();
            linear.setRidge(1.0E-8);
            linear.buildClassifier(trainData);

            Evaluation eval = new Evaluation(trainData);
            eval.evaluateModel(linear, testData);

            System.out.println("=== Test Set Evaluation ===");
            System.out.println("Correlation Coefficient: " + eval.correlationCoefficient());
            System.out.println("Mean Absolute Error (MAE): " + eval.meanAbsoluteError());
            System.out.println("Root Mean Squared Error (RMSE): " + eval.rootMeanSquaredError());
            System.out.println("Relative Absolute Error: " + eval.relativeAbsoluteError() + " %");
            System.out.println("Root Relative Squared Error: " + eval.rootRelativeSquaredError() + " %");

            System.out.println("\n=== Predictions on Test Set ===");
            for (int i = 0; i < testData.numInstances(); i++) {
                double actual = testData.instance(i).classValue();
                double predicted = linear.classifyInstance(testData.instance(i));
                System.out.println("Instance " + (i + 1) + ": Actual = " + actual + ", Predicted = " + predicted);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
