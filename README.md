# Watchcat

Server-side anti-cheat for Paper 1.21.11. By @Az7car.

Website: https://amethystcore.com/watchcat
Discord: https://orvexsmp.net/discord

## Features

### Combat Detection (12 checks)
- Killaura - GCD analysis, cinematic smoothing (R-squared), constellation tracking
- AimAssist - vertical lock deviation, pitch smoothing curves
- Reach - 3D ray-AABB intersection, distance validation
- Hitbox - expanded bounding box detection via ray intersection
- AutoClicker - CPS limits, interval standard deviation
- Triggerbot - click timing coefficient of variation
- Velocity/AntiKnockback - vertical velocity ratio vs expected knockback
- Criticals - air tick count verification on attack
- BowAimbot - yaw/pitch lock during bow charging
- AutoShield - perfect block timing patterns
- AutoTotem - inventory swap interval analysis

### Movement Detection (19 checks)
- Speed - 3D momentum + friction per tick
- Fly - gravitational constant validation, vertical velocity profiling
- NoFall - fall distance vs ground-state mismatch
- Step - Y delta vs step height limit
- Jesus - water/lava surface movement speed
- Spider - wall climb acceleration
- Glide - descent rate vs air ticks
- HighJump - initial jump velocity
- BunnyHop - air-to-ground speed ratio
- FastLadder - ladder climbing speed
- Phase - solid block penetration
- Blink - position delta vs time gap
- LongJump - horizontal velocity cap
- ElytraFly - elytra speed limit
- BoatFly - vehicle vertical movement
- NoWeb - speed reduction in cobwebs
- Strafe - air acceleration ratio
- SafeWalk - edge detection without sneaking
- EntitySpeed - ridden entity speed caps

### World Detection (13 checks)
- Scaffold - eye-vector vs block-face angle
- FastPlace - click interval variance
- InventoryMove - movement packets with GUI open
- FastBreak - block break timing
- Nuker - concurrent break rate
- AirPlace - block placement without adjacent face
- Tower - vertical placement + movement sync
- ChestStealer - inventory transaction rate
- BuildReach - block placement distance
- NoSwing - interaction without animation
- AutoFarm - crop harvest timing patterns
- AutoFish - fishing rod reuse intervals
- AutoMine - instant block target lock

### Mod & Injector Detection (6 checks)
- BrandDetector - minecraft:brand analysis, blocked client list
- PayloadProber - crafted payload injection, response analysis
- TickTimerAnalyzer - packet timing statistical analysis
- BadPackets - NaN/Infinity validation, self-attack detection
- Freecam - air movement without vertical change
- AntiAFK - periodic rotation without movement

### Machine Learning Brain
- 16-dimension feature vector extraction (rotation deltas, GCD, click variance, acceleration, packet rate)
- ONNX Runtime inference (in-JVM, no external dependencies)
- Isolation Forest anomaly detection blending with heuristic scores
- CSV training data export for offline model training

### Punishment System
- Shadow flagging with configurable confidence thresholds
- Randomized ban wave execution (30min-6hr jitter)
- Severity-based duration tiers: 30 days to permanent
- Offense multiplier: 1st=1x, 2nd=2x, 3rd=4x, 4th+=permanent
- IP bans alongside UUID bans
- Unique 7-character alphanumeric appeal code per player
- Non-deterministic scheduling to blind cheat developers

### Architecture
- Direct Netty pipeline injection (no ProtocolLib dependency)
- Fully asynchronous check execution (off-main-thread)
- 100% reflection-based, no Mojang-mapped dependencies
- Maven build, Java 21, Paper 1.21.11

## Building

```
mvn clean package
```

Output JAR in `target/watchcat-1.0.0.jar`.

## Configuration

Edit `config.yml` in the plugin data folder. Each check has:
- `enabled` - toggle
- `severity` - critical/high/medium/low (affects ban duration)
- `weight` - contribution to confidence score
- Check-specific thresholds

## Ban Wave System

When a player's confidence score reaches the confirm threshold (default 0.95), they are added to the pending ban queue. Bans execute in randomized waves with jittered timing and severity-scaled duration. Each player receives a unique appeal code.

## Dependencies

- Paper 1.21.11 (required)
- ONNX Runtime 1.26.0 (bundled via Maven shade plugin)
- Java 21 (required)

## License

All rights reserved. Not for redistribution without permission.
