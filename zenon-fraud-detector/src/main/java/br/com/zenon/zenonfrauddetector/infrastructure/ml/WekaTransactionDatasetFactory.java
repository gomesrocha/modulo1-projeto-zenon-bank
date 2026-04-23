package br.com.zenon.zenonfrauddetector.infrastructure.ml;

import br.com.zenon.zenonfrauddetector.domain.model.Transaction;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public class WekaTransactionDatasetFactory {

    public Instances fromTransactions(List<Transaction> transactions) {
        ArrayList<Attribute> attributes = buildAttributes();

        Instances dataset = new Instances("transactions_fraud", attributes, transactions.size());
        dataset.setClassIndex(dataset.numAttributes() - 1);

        for (Transaction transaction : transactions) {
            dataset.add(toTrainingInstance(transaction, dataset));
        }

        return dataset;
    }

    public Instance toPredictionInstance(Transaction transaction, Instances header) {
        DenseInstance instance = new DenseInstance(header.numAttributes());
        instance.setDataset(header);

        instance.setValue(0, transaction.step());
        instance.setValue(1, transaction.type().name());
        instance.setValue(2, transaction.amount().doubleValue());

        instance.setValue(3, transaction.origin().oldBalance().doubleValue());
        instance.setValue(4, transaction.origin().newBalance().doubleValue());

        instance.setValue(5, transaction.recipient().oldBalance().doubleValue());
        instance.setValue(6, transaction.recipient().newBalance().doubleValue());

        instance.setValue(7, transaction.isFlaggedFraud() ? "true" : "false");

        instance.setMissing(header.classIndex());
        return instance;
    }

    private DenseInstance toTrainingInstance(Transaction transaction, Instances dataset) {
        DenseInstance instance = new DenseInstance(dataset.numAttributes());
        instance.setDataset(dataset);

        instance.setValue(0, transaction.step());
        instance.setValue(1, transaction.type().name());
        instance.setValue(2, transaction.amount().doubleValue());

        instance.setValue(3, transaction.origin().oldBalance().doubleValue());
        instance.setValue(4, transaction.origin().newBalance().doubleValue());

        instance.setValue(5, transaction.recipient().oldBalance().doubleValue());
        instance.setValue(6, transaction.recipient().newBalance().doubleValue());

        instance.setValue(7, transaction.isFlaggedFraud() ? "true" : "false");
        instance.setValue(8, transaction.isFraud() ? "true" : "false");

        return instance;
    }

    private ArrayList<Attribute> buildAttributes() {
        ArrayList<Attribute> attributes = new ArrayList<>();

        attributes.add(new Attribute("step"));

        ArrayList<String> typeValues = new ArrayList<>(List.of(
                "CASH_IN", "CASH_OUT", "DEBIT", "PAYMENT", "TRANSFER"
        ));
        attributes.add(new Attribute("type", typeValues));

        attributes.add(new Attribute("amount"));
        attributes.add(new Attribute("origin_old_balance"));
        attributes.add(new Attribute("origin_new_balance"));
        attributes.add(new Attribute("recipient_old_balance"));
        attributes.add(new Attribute("recipient_new_balance"));

        ArrayList<String> booleanValues = new ArrayList<>(List.of("false", "true"));
        attributes.add(new Attribute("is_flagged_fraud", booleanValues));
        attributes.add(new Attribute("is_fraud", booleanValues));

        return attributes;
    }
}