package com.az7car.watchcat.core.di;

import com.az7car.watchcat.WatchcatPlugin;
import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.netty.PacketInjector;
import com.az7car.watchcat.core.netty.PacketProcessor;
import com.az7car.watchcat.core.pipeline.CheckRegistry;
import com.az7car.watchcat.detection.base.PlayerData;
import com.az7car.watchcat.detection.combat.*;
import com.az7car.watchcat.detection.mod.*;
import com.az7car.watchcat.detection.movement.*;
import com.az7car.watchcat.detection.world.*;
import com.az7car.watchcat.ml.AnomalyDetector;
import com.az7car.watchcat.ml.ONNXInference;
import com.az7car.watchcat.punishment.AppealCodeManager;
import com.az7car.watchcat.punishment.BanWaveExecutor;
import com.az7car.watchcat.punishment.ChatPunishmentManager;
import com.az7car.watchcat.punishment.ShadowFlaggingSystem;
import com.az7car.watchcat.util.TPSMonitor;

public class WatchcatInjector {

    public WatchcatConfig createConfig() {
        return new WatchcatConfig(WatchcatPlugin.getInstance());
    }

    public PacketProcessor createPacketProcessor(WatchcatConfig config) {
        return new PacketProcessor(config);
    }

    public PacketInjector createPacketInjector(WatchcatPlugin plugin, WatchcatConfig config, CheckRegistry registry) {
        PacketProcessor processor = createPacketProcessor(config);
        return new PacketInjector(plugin, config, processor, registry);
    }

    public CheckRegistry createCheckRegistry(WatchcatConfig config) {
        return new CheckRegistry(config);
    }

    public TPSMonitor createTPSMonitor() {
        return new TPSMonitor();
    }

    public ONNXInference createONNXInference(WatchcatConfig config) {
        return new ONNXInference(config);
    }

    public AnomalyDetector createAnomalyDetector(ONNXInference inference) {
        return new AnomalyDetector(inference);
    }

    public AppealCodeManager createAppealCodeManager(WatchcatConfig config, WatchcatPlugin plugin) {
        return new AppealCodeManager(config, plugin);
    }

    public ShadowFlaggingSystem createFlaggingSystem() {
        return new ShadowFlaggingSystem();
    }

    public BanWaveExecutor createBanWaveExecutor(WatchcatConfig config, WatchcatPlugin plugin) {
        return new BanWaveExecutor(config, plugin);
    }

    public ChatPunishmentManager createChatPunishmentManager(WatchcatConfig config, WatchcatPlugin plugin) {
        return new ChatPunishmentManager(config, plugin);
    }

    public AntiBotCheck createAntiBotCheck(WatchcatConfig config) {
        return new AntiBotCheck(config);
    }

    public RaidCheck createRaidCheck(WatchcatConfig config) {
        return new RaidCheck(config);
    }

    public void registerChecks(CheckRegistry registry, WatchcatConfig config,
                               AnomalyDetector anomalyDetector, ShadowFlaggingSystem flaggingSystem,
                               ChatPunishmentManager chatPunishmentManager) {
        registry.register(new KillauraCheck(config));
        registry.register(new AimAssistCheck(config));
        registry.register(new ReachCheck(config));
        registry.register(new HitboxCheck(config));
        registry.register(new AutoClickerCheck(config));
        registry.register(new TriggerbotCheck(config));
        registry.register(new VelocityCheck(config));
        registry.register(new CriticalsCheck(config));
        registry.register(new BowAimbotCheck(config));
        registry.register(new AntiKnockbackCheck(config));
        registry.register(new AutoShieldCheck(config));
        registry.register(new AutoTotemCheck(config));
        registry.register(new AutoArmorCheck(config));
        registry.register(new FastBowCheck(config));
        registry.register(new MultiAuraCheck(config));
        registry.register(new ShieldBreakerCheck(config));
        registry.register(new MaceSwitchCheck(config));

        registry.register(new SpeedCheck(config));
        registry.register(new FlyCheck(config));
        registry.register(new NoFallCheck(config));
        registry.register(new StepCheck(config));
        registry.register(new JesusCheck(config));
        registry.register(new SpiderCheck(config));
        registry.register(new GlideCheck(config));
        registry.register(new HighJumpCheck(config));
        registry.register(new BunnyHopCheck(config));
        registry.register(new FastLadderCheck(config));
        registry.register(new PhaseCheck(config));
        registry.register(new BlinkCheck(config));
        registry.register(new LongJumpCheck(config));
        registry.register(new ElytraFlyCheck(config));
        registry.register(new BoatFlyCheck(config));
        registry.register(new NoWebCheck(config));
        registry.register(new StrafeCheck(config));
        registry.register(new SafeWalkCheck(config));
        registry.register(new EntitySpeedCheck(config));
        registry.register(new DerpCheck(config));
        registry.register(new AntiVoidCheck(config));

        registry.register(new ScaffoldCheck(config));
        registry.register(new FastPlaceCheck(config));
        registry.register(new InventoryMoveCheck(config));
        registry.register(new FastBreakCheck(config));
        registry.register(new NukerCheck(config));
        registry.register(new AirPlaceCheck(config));
        registry.register(new TowerCheck(config));
        registry.register(new ChestStealerCheck(config));
        registry.register(new BuildReachCheck(config));
        registry.register(new NoSwingCheck(config));
        registry.register(new InventoryActionsCheck(config));
        registry.register(new AutoToolCheck(config));
        registry.register(new AutoFarmCheck(config));
        registry.register(new AutoFishCheck(config));
        registry.register(new AutoMineCheck(config));

        registry.register(new BrandDetector(config));
        registry.register(new PayloadProber(config));
        registry.register(new TickTimerAnalyzer(config));
        registry.register(new BadPacketsCheck(config));
        registry.register(new FreecamCheck(config));
        registry.register(new AntiAFKCheck(config));
        registry.register(new TimerCheck(config));
        registry.register(new MorePacketsCheck(config));
        registry.register(new InvalidInteractCheck(config));
        registry.register(new DisablerCheck(config));
        registry.register(new SkinBlinkerCheck(config));
        registry.register(new SpinbotCheck(config));
        registry.register(new HeadRollCheck(config));
        registry.register(new NoRotateSetCheck(config));
        registry.register(new FlightCheck(config));
        registry.register(new AntiSwearCheck(config, chatPunishmentManager));
        registry.register(new AntiFloodCheck(config, chatPunishmentManager));
        registry.register(new AntiAdCheck(config, chatPunishmentManager));

        registry.register(new AimbotCheck(config));
        registry.register(new GroundSpoofCheck(config));
        registry.register(new XRayCheck(config));
        registry.register(new NoSlowCheck(config));
        registry.register(new FastEatCheck(config));
        registry.register(new KeepSprintCheck(config));
        registry.register(new FastHealCheck(config));

        registry.register(new AimLockCheck(config));
        registry.register(new RegenCheck(config));
        registry.register(new SuperKnockbackCheck(config));
        registry.register(new WTapCheck(config));
        registry.register(new BacktrackCheck(config));

        registry.register(new MotionCheck(config));
        registry.register(new GravityCheck(config));
        registry.register(new AirJumpCheck(config));
        registry.register(new FastFallCheck(config));
        registry.register(new CollisionCheck(config));
        registry.register(new PredictionCheck(config));

        registry.register(new BlockInteractionCheck(config));
        registry.register(new FastConsumeCheck(config));
        registry.register(new GhostBlockCheck(config));
        registry.register(new RotationPlaceCheck(config));

        registry.register(new BaritoneCheck(config));
        registry.register(new AutoCrystalCheck(config));
        registry.register(new AntiAntiXrayCheck(config));
        registry.register(new OreBotCheck(config));

        registry.register(new PitchLimitCheck(config));
        registry.register(new NoMissCheck(config));
        registry.register(new InvisibleAimCheck(config));
        registry.register(new ReachMultiCheck(config));
        registry.register(new CPSLimitCheck(config));
        registry.register(new DelayCheck(config));

        registry.register(new TimerBalanceCheck(config));
        registry.register(new TimerAccelCheck(config));
        registry.register(new SwimCheck(config));
        registry.register(new AscensionCheck(config));
        registry.register(new JumpCheck(config));
        registry.register(new VehicleSpeedCheck(config));

        registry.register(new InstantMineCheck(config));
        registry.register(new MultiTaskCheck(config));
        registry.register(new DirectionCheck(config));
        registry.register(new PacketOrderCheck(config));
        registry.register(new RotationBreakCheck(config));

        registry.register(new NoRenderCheck(config));
        registry.register(new TrajectoryCheck(config));
        registry.register(new ClickTPCheck(config));
        registry.register(new AntiInvisCheck(config));
        registry.register(new ProxyCheck(config));

        registry.register(new AutoBlockCheck(config));
        registry.register(new ComboCheck(config));
        registry.register(new DoubleHitCheck(config));
        registry.register(new PerfectBlockCheck(config));
        registry.register(new ShieldBlockCheck(config));

        registry.register(new IceSpeedCheck(config));
        registry.register(new SlimeBlockCheck(config));
        registry.register(new WaterSpeedCheck(config));
        registry.register(new LavaSpeedCheck(config));
        registry.register(new WallClimbCheck(config));

        registry.register(new BreakPatternCheck(config));
        registry.register(new ChestAuraCheck(config));

        registry.register(new AntiAntiCheatCheck(config));
        registry.register(new ScreenshareCheck(config));
        registry.register(new PluginDetector(config));

        registry.register(new AutoWeaponCheck(config));
        registry.register(new AutoPotCheck(config));
        registry.register(new AutoPearlCheck(config));
        registry.register(new ReachOverrideCheck(config));
        registry.register(new HitBoxOverrideCheck(config));

        registry.register(new ClipCheck(config));
        registry.register(new YPortCheck(config));
        registry.register(new SpeedLimitCheck(config));
        registry.register(new AntiLevitationCheck(config));
        registry.register(new DolphinJumpCheck(config));

        registry.register(new AutoRespawnCheck(config));
        registry.register(new FastDoorCheck(config));
        registry.register(new SurroundCheck(config));
        registry.register(new PistonPushCheck(config));

        registry.register(new ClientSpoofCheck(config));
        registry.register(new PacketManiCheck(config));
        registry.register(new ForceOPCheck(config));
        registry.register(new NoPitchLimitCheck(config));
        registry.register(new AntiFireworkCheck(config));

        registry.register(new NoHurtCamCheck(config));
        registry.register(new AntiWeaknessCheck(config));
        registry.register(new AntiHungerCheck(config));
        registry.register(new NoFireCheck(config));
        registry.register(new NoPushCheck(config));

        registry.register(new SneakCheck(config));
        registry.register(new PingSpoofCheck(config));
        registry.register(new PacketDelayCheck(config));
        registry.register(new NoDragCheck(config));
        registry.register(new NoJumpDelayCheck(config));

        registry.register(new AutoSignCheck(config));
        registry.register(new AutoDoorCheck(config));
        registry.register(new AutoBrewCheck(config));
        registry.register(new AutoEnchantCheck(config));
        registry.register(new AutoAnvilCheck(config));

        registry.register(new ResourceSpoofCheck(config));
        registry.register(new NameSpoofCheck(config));
        registry.register(new NoAttackCooldownCheck(config));
        registry.register(new AntiCactusCheck(config));
        registry.register(new AntiBerryCheck(config));
        registry.register(new NoPortalOverlayCheck(config));
        registry.register(new NoPumpkinCheck(config));

        registry.register(new SmoothAimCheck(config));
        registry.register(new RageAuraCheck(config));
        registry.register(new ClickPatternCheck(config));
        registry.register(new TickShiftCheck(config));
        registry.register(new AutoSoupCheck(config));

        registry.register(new SpeedFieldCheck(config));
        registry.register(new EnderPearlCheck(config));

        registry.register(new BoatGlitchCheck(config));
        registry.register(new ContainerSortCheck(config));
        registry.register(new AutoSmithingCheck(config));

        registry.register(new NoFireOverlayCheck(config));
        registry.register(new AntiFogCheck(config));
        registry.register(new NoScoreboardCheck(config));
        registry.register(new AntiPotionCheck(config));
        registry.register(new CrashPayloadCheck(config));
        registry.register(new ESPCheck(config));
        registry.register(new TracersCheck(config));

        registry.register(new AutoLeaveCheck(config));
        registry.register(new BookExploitCheck(config));
        registry.register(new NoWeatherCheck(config));
        registry.register(new AntiWDLCheck(config));
        registry.register(new SoundPosCheck(config));
        registry.register(new PickRangeCheck(config));
        registry.register(new ExpPickupCheck(config));
        registry.register(new PacketSpamCheck(config));
        registry.register(new AntiResourcePackCheck(config));
        registry.register(new NoPotionLabelCheck(config));
        registry.register(new AutoWalkCheck(config));
    }
}
