
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.RandomForest;
import java.util.Random;

public class OnlineShoppingBehaviourAnalysis {

    public static void main(String[] args) throws Exception {

        String file = "online_shopping_behaviour.arff";

        DataSource source = new DataSource(file);
        Instances data = source.getDataSet();

        data.setClassIndex(data.numAttributes() - 1);

        System.out.println("ONLINE SHOPPING BEHAVIOUR ANALYSIS");
        System.out.println("Total Records: " + data.numInstances());

        // J48 Decision Tree
        Evaluation e1 = new Evaluation(data);
        e1.crossValidateModel(new J48(), data, 10, new Random(1));

        System.out.println("\nJ48 DECISION TREE");
        System.out.println(e1.toSummaryString());
        System.out.println(e1.toMatrixString());

        // Naive Bayes
        Evaluation e2 = new Evaluation(data);
        e2.crossValidateModel(
            new NaiveBayes(), data, 10, new Random(1)
        );

        System.out.println("\nNAIVE BAYES");
        System.out.println(e2.toSummaryString());
        System.out.println(e2.toMatrixString());

        // Random Forest
        Evaluation e3 = new Evaluation(data);
        e3.crossValidateModel(
            new RandomForest(), data, 10, new Random(1)
        );

        System.out.println("\nRANDOM FOREST");
        System.out.println(e3.toSummaryString());
        System.out.println(e3.toMatrixString());
    }
}
