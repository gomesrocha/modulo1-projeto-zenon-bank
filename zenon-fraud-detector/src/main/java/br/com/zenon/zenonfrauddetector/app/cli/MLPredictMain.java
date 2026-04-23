package br.com.zenon.zenonfrauddetector.app.cli;

import br.com.zenon.zenonfrauddetector.app.service.FraudModelService;
import br.com.zenon.zenonfrauddetector.domain.model.CustomerBalance;
import br.com.zenon.zenonfrauddetector.domain.model.FraudPrediction;
import br.com.zenon.zenonfrauddetector.domain.model.Transaction;
import br.com.zenon.zenonfrauddetector.domain.model.TransactionType;

import java.math.BigDecimal;

public class MLPredictMain {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso:");
            System.out.println("mvn exec:java -Dexec.mainClass=\"br.com.zenon.zenonfrauddetector.app.cli.MLPredictMain\" -Dexec.args=\"models/fraud.model\"");
            return;
        }

        String modelFile = args[0];

        try {
            FraudModelService service = new FraudModelService(modelFile);

            Transaction transaction = new Transaction(
                    1,
                    TransactionType.TRANSFER,
                    new BigDecimal("181.00"),
                    new CustomerBalance(
                            "C1305486145",
                            new BigDecimal("181.00"),
                            new BigDecimal("0.00")
                    ),
                    new CustomerBalance(
                            "C553264065",
                            new BigDecimal("0.00"),
                            new BigDecimal("0.00")
                    ),
                    false,
                    false
            );

            FraudPrediction prediction = service.predict(transaction);

            System.out.println("Fraude prevista: " + prediction.fraud());
            System.out.println("Probabilidade de fraude: " + prediction.fraudProbability());

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}