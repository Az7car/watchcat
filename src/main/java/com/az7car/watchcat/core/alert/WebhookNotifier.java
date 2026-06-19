package com.az7car.watchcat.core.alert;

import java.awt.Color;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WebhookNotifier {

    private String webhookUrl;

    public WebhookNotifier(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void sendBanAlert(String playerName, String checkName, String severity, String duration, String appealCode) {
        if (webhookUrl == null || webhookUrl.isEmpty()) return;
        try {
            String json = String.format(
                "{\"embeds\":[{\"title\":\"Watchcat Ban\",\"color\":16711680,\"fields\":[" +
                "{\"name\":\"Player\",\"value\":\"%s\",\"inline\":true}," +
                "{\"name\":\"Check\",\"value\":\"%s\",\"inline\":true}," +
                "{\"name\":\"Severity\",\"value\":\"%s\",\"inline\":true}," +
                "{\"name\":\"Duration\",\"value\":\"%s\",\"inline\":true}," +
                "{\"name\":\"Appeal\",\"value\":\"[orvexsmp.net/discord](https://orvexsmp.net/discord) (`%s`)\",\"inline\":false}" +
                "],\"footer\":{\"text\":\"Watchcat by @Az7car\"},\"timestamp\":\"%s\"}]}",
                escapeJson(playerName), escapeJson(checkName), escapeJson(severity),
                escapeJson(duration), escapeJson(appealCode),
                java.time.Instant.now().toString()
            );
            send(json);
        } catch (Exception e) {}
    }

    public void sendAlert(String playerName, String checkName, String details, double confidence) {
        if (webhookUrl == null || webhookUrl.isEmpty()) return;
        try {
            String json = String.format(
                "{\"embeds\":[{\"title\":\"Watchcat Alert\",\"color\":16776960,\"fields\":[" +
                "{\"name\":\"Player\",\"value\":\"%s\",\"inline\":true}," +
                "{\"name\":\"Check\",\"value\":\"%s\",\"inline\":true}," +
                "{\"name\":\"Details\",\"value\":\"%s\",\"inline\":false}," +
                "{\"name\":\"Confidence\",\"value\":\"%.1f%%\",\"inline\":true}" +
                "],\"footer\":{\"text\":\"Watchcat by @Az7car\"},\"timestamp\":\"%s\"}]}",
                escapeJson(playerName), escapeJson(checkName), escapeJson(details),
                confidence * 100, java.time.Instant.now().toString()
            );
            send(json);
        } catch (Exception e) {}
    }

    private void send(String json) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(webhookUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) {}
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
