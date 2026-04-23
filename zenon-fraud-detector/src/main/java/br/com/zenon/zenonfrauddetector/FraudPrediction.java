package br.com.zenon.zenonfrauddetector;

public record FraudPrediction(
        boolean fraud,
        double fraudProbability
) {
}