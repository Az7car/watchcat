package com.az7car.watchcat.ml;

import com.az7car.watchcat.core.config.WatchcatConfig;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class ONNXInference {

    private final boolean enabled;
    private final MLConfig config;
    private boolean modelLoaded;
    private float[][] trees;
    private float threshold;

    public ONNXInference(WatchcatConfig config) {
        this.config = new MLConfig(config);
        this.enabled = config.isMlEnabled();
        this.modelLoaded = false;
        loadModel();
    }

    private void loadModel() {
        if (!enabled) return;
        modelLoaded = true;
    }

    public float infer(float[] features) {
        if (!enabled || !modelLoaded) return 0.0f;
        return anomalyScore(features);
    }

    private float anomalyScore(float[] features) {
        double score = 0;
        if (trees != null) {
            for (float[] tree : trees) {
                score += traverseTree(features, tree);
            }
            score /= trees.length;
        }
        return (float) MathUtils.clamp(score, 0.0, 1.0);
    }

    private double traverseTree(float[] features, float[] tree) {
        int nodeIndex = 0;
        while (nodeIndex < tree.length) {
            int featureIndex = (int) tree[nodeIndex];
            if (featureIndex < 0) break;
            double splitValue = tree[nodeIndex + 1];
            int leftChild = (int) tree[nodeIndex + 2];
            int rightChild = (int) tree[nodeIndex + 3];

            if (featureIndex < features.length) {
                if (features[featureIndex] < splitValue) {
                    if (leftChild < 0) return tree[nodeIndex + 4];
                    nodeIndex = leftChild;
                } else {
                    if (rightChild < 0) return tree[nodeIndex + 4];
                    nodeIndex = rightChild;
                }
            } else {
                break;
            }
        }
        return 0.5;
    }

    public void loadIsolationForest(float[][] treeData, float anomalyThreshold) {
        this.trees = treeData;
        this.threshold = anomalyThreshold;
        this.modelLoaded = true;
    }

    public boolean isModelLoaded() { return modelLoaded; }
    public double getThreshold() { return threshold; }

    public void close() {}

    private static class MathUtils {
        static double clamp(double v, double min, double max) {
            return Math.max(min, Math.min(max, v));
        }
    }
}
