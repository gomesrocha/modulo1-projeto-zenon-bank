package br.com.zenon.zenonfrauddetector.domain.model;

public record FraudPrediction(
        boolean fraud,
        double fraudProbability
) {
}