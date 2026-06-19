package com.az7car.watchcat.core.pipeline;

import com.az7car.watchcat.core.config.WatchcatConfig;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class CheckRegistry {

    private final WatchcatConfig config;
    private final CopyOnWriteArrayList<AbstractCheck> allChecks;
    private final CopyOnWriteArrayList<AbstractCheck> combatChecks;
    private final CopyOnWriteArrayList<AbstractCheck> movementChecks;
    private final CopyOnWriteArrayList<AbstractCheck> worldChecks;
    private final CopyOnWriteArrayList<AbstractCheck> modChecks;

    public CheckRegistry(WatchcatConfig config) {
        this.config = config;
        this.allChecks = new CopyOnWriteArrayList<>();
        this.combatChecks = new CopyOnWriteArrayList<>();
        this.movementChecks = new CopyOnWriteArrayList<>();
        this.worldChecks = new CopyOnWriteArrayList<>();
        this.modChecks = new CopyOnWriteArrayList<>();
    }

    public void register(AbstractCheck check) {
        allChecks.add(check);
        switch (check.getCategory()) {
            case "combat" -> combatChecks.add(check);
            case "movement" -> movementChecks.add(check);
            case "world" -> worldChecks.add(check);
            case "mod" -> modChecks.add(check);
        }
    }

    public Collection<AbstractCheck> getCombatChecks() { return combatChecks; }
    public Collection<AbstractCheck> getMovementChecks() { return movementChecks; }
    public Collection<AbstractCheck> getWorldChecks() { return worldChecks; }
    public Collection<AbstractCheck> getModChecks() { return modChecks; }
    public Collection<AbstractCheck> getAllChecks() { return allChecks; }
    public Collection<AbstractCheck> getChecks() { return allChecks; }
}
