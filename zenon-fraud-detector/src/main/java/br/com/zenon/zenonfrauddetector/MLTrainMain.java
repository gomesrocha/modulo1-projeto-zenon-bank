package br.com.zenon.zenonfrauddetector;

public class MLTrainMain {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso:");
            System.out.println("mvn exec:java -Dexec.mainClass=\"br.com.zenon.zenonfrauddetector.MLTrainMain\" -Dexec.args=\"../data/PS_20174392719_1491204439457_log.csv models/fraud.model\"");
            return;
        }

        String csvFile = args[0];
        String modelFile = args[1];

        try {
            FraudModelTrainer trainer = new FraudModelTrainer();
            trainer.trainAndSave(csvFile, 100_000, modelFile);
            System.out.println("Modelo treinado e salvo em: " + modelFile);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}