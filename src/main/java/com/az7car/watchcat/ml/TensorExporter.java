package com.az7car.watchcat.ml;

import com.az7car.watchcat.detection.base.PlayerData;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;

public class TensorExporter {

    private final Path exportPath;
    private final boolean enabled;

    public TensorExporter(Path dataFolder, boolean enabled) {
        this.exportPath = dataFolder.resolve("training_data.csv");
        this.enabled = enabled;
        if (enabled) {
            try {
                if (!Files.exists(exportPath)) {
                    Files.writeString(exportPath, createHeader(), StandardOpenOption.CREATE);
                }
            } catch (IOException e) {
                // silent
            }
        }
    }

    public void export(PlayerData data, float anomalyScore, boolean flagged) {
        if (!enabled) return;

        float[] features = data.getFeatureVector().compute(data);
        StringBuilder line = new StringBuilder();
        line.append(data.getUuid()).append(",");
        line.append(data.getPlayerName()).append(",");
        line.append(flagged ? 1 : 0).append(",");
        line.append(anomalyScore).append(",");
        for (int i = 0; i < features.length; i++) {
            line.append(features[i]);
            if (i < features.length - 1) line.append(",");
        }
        line.append("\n");

        try {
            Files.writeString(exportPath, line.toString(),
                StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            // silent
        }
    }

    private String createHeader() {
        StringBuilder sb = new StringBuilder("uuid,player_name,flagged,anomaly_score,");
        for (int i = 0; i < FeatureVector.getFeatureCount(); i++) {
            sb.append("f").append(i);
            if (i < FeatureVector.getFeatureCount() - 1) sb.append(",");
        }
        sb.append("\n");
        return sb.toString();
    }
}
