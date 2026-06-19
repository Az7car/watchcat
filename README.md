# Watchcat

Server-side anti-cheat for Paper 1.21.11. By @Az7car.

Website: https://watchcat.gg
Discord: https://discord.gg/2hPbabJ9jP

## Features

### Combat Detection (53 checks)
- Killaura, AimAssist, Reach, Hitbox, AutoClicker, Triggerbot
- Velocity, AntiKnockback, Criticals, BowAimbot, AutoShield
- AutoTotem, AutoArmor, FastBow, MultiAura, ShieldBreaker
- MaceSwitch, NoSlow, FastEat, FastHeal, KeepSprint
- AimLock, Regen, SuperKnockback, WTap, Backtrack
- PitchLimit, NoMiss, InvisibleAim, ReachMulti, CPSLimit
- Delay, AutoBlock, Combo, DoubleHit, PerfectBlock
- ShieldBlock, AutoWeapon, AutoPot, AutoPearl
- ReachOverride, HitBoxOverride, NoHurtCam, AntiWeakness
- AntiHunger, NoFire, NoPush, SmoothAim, RageAura
- ClickPattern, TickShift, AutoSoup, AutoLeave

### Movement Detection (44 checks)
- Speed, Fly, NoFall, Step, Jesus, Spider, Glide, HighJump
- BunnyHop, FastLadder, Phase, Blink, LongJump, ElytraFly
- BoatFly, NoWeb, Strafe, SafeWalk, EntitySpeed, Derp
- AntiVoid, Motion, Gravity, AirJump, FastFall, Collision
- Prediction, TimerBalance, TimerAccel, Swim, Ascension
- JumpCheck, VehicleSpeed, IceSpeed, SlimeBlock, WaterSpeed
- LavaSpeed, WallClimb, Clip, YPort, SpeedLimit
- AntiLevitation, DolphinJump, Sneak, PingSpoof, PacketDelay
- NoDrag, NoJumpDelay, SpeedField, EnderPearl, AutoWalk

### World Detection (34 checks)
- Scaffold, FastPlace, InventoryMove, FastBreak, Nuker
- AirPlace, Tower, ChestStealer, BuildReach, NoSwing
- AutoFarm, AutoFish, AutoMine, InventoryActions, AutoTool
- BlockInteraction, FastConsume, GhostBlock, RotationPlace
- InstantMine, MultiTask, Direction, PacketOrder, RotationBreak
- BreakPattern, ChestAura, AutoRespawn, FastDoor, Surround
- PistonPush, AutoSign, AutoDoor, AutoBrew, AutoEnchant
- AutoAnvil, BoatGlitch, ContainerSort, AutoSmithing
- PickRange, ExpPickup

### Mod & Injector Detection (87 checks)
- BrandDetector, PayloadProber, TickTimerAnalyzer, BadPackets
- Freecam, AntiAFK, Timer, MorePackets, InvalidInteract
- Disabler, SkinBlinker, SpinBot, HeadRoll, NoRotateSet
- Flight, AntiSwear, AntiFlood, AntiAd, Aimbot, GroundSpoof
- XRay, Baritone, AutoCrystal, AntiAntiXRay, OreBot
- NoRender, Trajectory, ClickTP, AntiInvis, Proxy
- AntiAntiCheat, ScreenShare, PluginDetector, ClientSpoof
- PacketMani, ForceOP, NoPitchLimit, AntiFirework
- ResourceSpoof, NameSpoof, NoAttackCooldown, AntiCactus
- AntiBerry, NoPortalOverlay, NoPumpkin, NoFireOverlay
- AntiFog, NoScoreboard, AntiPotion, CrashPayload, ESP
- Tracers, NoFov, NoBob, AntiBlind, TimeSpoof, AntiShield
- BookExploit, NoWeather, AntiWDL, SoundPos, PacketSpam
- AntiResourcePack, NoPotionLabel, AntiBot, Raid

### Machine Learning Brain
- 32-dimension feature vector (rotation deltas, GCD, click variance, acceleration, packet rate, velocity std/mean/trend, pitch entropy, convergence)
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
- 218 checks total | 265+ Java files

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

## Commands

- `/watchcat alerts` or `/watchdog alerts` - toggle alert messages
- `/watchcat stats` or `/watchdog stats` - check performance statistics  
- `/watchcat profile` or `/watchdog profile` - detailed check profiling
- `/watchcat checks [category]` or `/watchdog checks [category]` - list checks with status
- `/watchcat player <name>` or `/watchdog player <name>` - per-player check state
- `/watchcat reason <player> <reason>` or `/watchdog reason <player> <reason>` - ban a player with reason
- `/watchcat reload` or `/watchdog reload` - reload configuration
- `/watchcat whitelist <check|all> <player>` or `/watchdog whitelist` - exemption management
- `/watchcat report <check> <player>` or `/watchdog report` - report a false positive
- `/watchcat banwave` or `/watchdog banwave` - trigger manual ban wave

## Dependencies

- Paper 1.21.11 (required)
- ONNX Runtime 1.26.0 (bundled via Maven shade plugin)
- Java 21 (required)

## License

All rights reserved. Not for redistribution without permission.
