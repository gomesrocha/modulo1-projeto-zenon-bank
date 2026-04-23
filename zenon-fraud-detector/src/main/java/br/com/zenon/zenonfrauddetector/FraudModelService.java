package br.com.zenon.zenonfrauddetector;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class FraudModelService {

    private final Classifier model;
    private final Instances header;
    private final WekaTransactionDatasetFactory datasetFactory;

    public FraudModelService(String modelFile) throws Exception {
        Object[] objects = SerializationHelper.readAll(modelFile);
        this.model = (Classifier) objects[0];
        this.header = (Instances) objects[1];
        this.datasetFactory = new WekaTransactionDatasetFactory();
    }

    public FraudPrediction predict(Transaction transaction) throws Exception {
        Instance instance = datasetFactory.toPredictionInstance(transaction, header);

        double predictedIndex = model.classifyInstance(instance);
        String predictedLabel = header.classAttribute().value((int) predictedIndex);

        double[] distribution = model.distributionForInstance(instance);
        int fraudIndex = header.classAttribute().indexOfValue("true");
        double fraudProbability = distribution[fraudIndex];

        return new FraudPrediction("true".equals(predictedLabel), fraudProbability);
    }
}