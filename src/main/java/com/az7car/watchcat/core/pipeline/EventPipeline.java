package com.az7car.watchcat.core.pipeline;

import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import com.az7car.watchcat.punishment.ShadowFlaggingSystem;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventPipeline {

    private final List<EventHandler> handlers;

    public EventPipeline() {
        this.handlers = new CopyOnWriteArrayList<>();
    }

    public void emit(CheckResult result, PlayerData data, AbstractCheck check) {
        if (result == null || result == CheckResult.PASS) return;
        if (result == CheckResult.FLAG || result == CheckResult.FAIL) {
            for (EventHandler handler : handlers) {
                handler.onResult(result, data, check);
            }
        }
    }

    public void subscribe(EventHandler handler) {
        handlers.add(handler);
    }

    public interface EventHandler {
        void onResult(CheckResult result, PlayerData data, AbstractCheck check);
    }
}
