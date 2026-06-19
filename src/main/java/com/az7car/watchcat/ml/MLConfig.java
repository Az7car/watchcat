package com.az7car.watchcat.ml;

import com.az7car.watchcat.core.config.WatchcatConfig;

public class MLConfig {

    private final boolean enabled;
    private final String modelPath;
    private final int inferenceIntervalTicks;
    private final double heuristicWeight;
    private final double mlWeight;

    public MLConfig(WatchcatConfig config) {
        this.enabled = config.isMlEnabled();
        this.modelPath = config.getModelPath();
        this.inferenceIntervalTicks = config.getMLInferenceInterval();
        this.heuristicWeight = config.getHeuristicWeight();
        this.mlWeight = config.getMLWeight();
    }

    public boolean isEnabled() { return enabled; }
    public String getModelPath() { return modelPath; }
    public int getInferenceIntervalTicks() { return inferenceIntervalTicks; }
    public double getHeuristicWeight() { return heuristicWeight; }
    public double getMlWeight() { return mlWeight; }
}
