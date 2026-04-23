package br.com.zenon.zenonfrauddetector;

import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.util.List;

public class FraudModelTrainer {

    private final TransactionIngestor ingestor;
    private final WekaTransactionDatasetFactory datasetFactory;

    public FraudModelTrainer() {
        this.ingestor = new TransactionIngestor();
        this.datasetFactory = new WekaTransactionDatasetFactory();
    }

    public void trainAndSave(String csvFile, int limit, String modelFile) throws Exception {
        List<Transaction> transactions = ingestor.ingest(csvFile, limit);
        Instances dataset = datasetFactory.fromTransactions(transactions);

        RandomForest model = new RandomForest();
        model.setNumIterations(100);
        model.setSeed(42);

        model.buildClassifier(dataset);

        Instances header = new Instances(dataset, 0);
        SerializationHelper.writeAll(modelFile, new Object[]{model, header});
    }
}