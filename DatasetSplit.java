import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.Randomize;

import java.io.File;

public class DatasetSplit {

    public static void main(String[] args) {
        try {
            // Load the entire dataset
            DataSource source = new DataSource("/Users/dontronolone/Downloads/Cleaned_GLOBAL_DATAFLOW_1995-2023_no_country.arff");  // Update with your dataset path
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);

            // Randomize the dataset
            Randomize randomize = new Randomize();
            randomize.setInputFormat(data);
            Instances randomizedData = Filter.useFilter(data, randomize);

            // Split the dataset: 80% for training, 20% for testing
            int trainSize = (int) Math.round(randomizedData.numInstances() * 0.8);
            int testSize = randomizedData.numInstances() - trainSize;

            Instances trainData = new Instances(randomizedData, 0, trainSize);
            Instances testData = new Instances(randomizedData, trainSize, testSize);

            // Save the training set to a file
            ArffSaver saverTrain = new ArffSaver();
            saverTrain.setInstances(trainData);
            saverTrain.setFile(new File("/Users/dontronolone/Downloads/trainData.arff"));
            saverTrain.writeBatch();

            // Save the test set to a file
            ArffSaver saverTest = new ArffSaver();
            saverTest.setInstances(testData);
            saverTest.setFile(new File("/Users/dontronolone/Downloads/testData.arff"));
            saverTest.writeBatch();

            System.out.println("Training and test sets saved successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
