package com.az7car.watchcat.ml;

import com.az7car.watchcat.detection.base.PlayerData;

public class AnomalyDetector {

    private final ONNXInference inference;
    private final double mlWeight;
    private final double heuristicWeight;

    public AnomalyDetector(ONNXInference inference) {
        this.inference = inference;
        this.mlWeight = 0.5;
        this.heuristicWeight = 0.5;
    }

    public float getAnomalyScore(PlayerData data) {
        float[] features = data.getFeatureVector().compute(data);
        float mlScore = inference.infer(features);
        data.setMlAnomalyScore(mlScore);
        return mlScore;
    }

    public float computeBlendedScore(PlayerData data, float heuristicScore) {
        float mlScore = getAnomalyScore(data);
        return (float) (heuristicScore * heuristicWeight + mlScore * mlWeight);
    }
}
