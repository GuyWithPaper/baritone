package baritone.api;

import baritone.api.utils.Helper;
import baritone.api.utils.NotificationHelper;
import baritone.api.utils.SettingsUtil;
import baritone.api.utils.TypeUtils;
import baritone.api.utils.gui.BaritoneToast;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class Settings {
    private static final Logger LOGGER = LoggerFactory.getLogger("Baritone");

    public final Setting<Boolean> allowBreak = new Setting<>(true);
    public final Setting<List<Block>> allowBreakAnyway = new Setting<>(new ArrayList<>());
    public final Setting<Boolean> allowSprint = new Setting<>(true);
    public final Setting<Boolean> allowPlace = new Setting<>(true);
    public final Setting<Boolean> allowPlaceInFluidsSource = new Setting<>(true);
    public final Setting<Boolean> allowPlaceInFluidsFlow = new Setting<>(true);
    public final Setting<Boolean> allowInventory = new Setting<>(false);
    public final Setting<Integer> ticksBetweenInventoryMoves = new Setting<>(1);
    public final Setting<Boolean> inventoryMoveOnlyIfStationary = new Setting<>(false);
    public final Setting<Boolean> assumeExternalAutoTool = new Setting<>(false);
    public final Setting<Boolean> autoTool = new Setting<>(true);
    public final Setting<Double> blockPlacementPenalty = new Setting<>(20D);
    public final Setting<Double> blockBreakAdditionalPenalty = new Setting<>(2D);
    public final Setting<Double> jumpPenalty = new Setting<>(2D);
    public final Setting<Double> walkOnWaterOnePenalty = new Setting<>(3D);
    public final Setting<Boolean> strictLiquidCheck = new Setting<>(false);
    public final Setting<Boolean> allowWaterBucketFall = new Setting<>(true);
    public final Setting<Boolean> assumeWalkOnWater = new Setting<>(false);
    public final Setting<Boolean> assumeWalkOnLava = new Setting<>(false);
    public final Setting<Boolean> assumeStep = new Setting<>(false);
    public final Setting<Boolean> assumeSafeWalk = new Setting<>(false);
    public final Setting<Boolean> allowJumpAtBuildLimit = new Setting<>(false);
    
    @Deprecated
    @JavaOnly
    public final Setting<Boolean> allowJumpAt256 = new Setting<>(false);
    
    public final Setting<Boolean> allowParkourAscend = new Setting<>(true);
    public final Setting<Boolean> allowDiagonalDescend = new Setting<>(false);
    public final Setting<Boolean> allowDiagonalAscend = new Setting<>(false);
    public final Setting<Boolean> allowDownward = new Setting<>(true);
    
    public final Setting<List<Item>> acceptableThrowawayItems = new Setting<>(new ArrayList<>(Arrays.asList(
            Blocks.DIRT.asItem(),
            Blocks.COBBLESTONE.asItem(),
            Blocks.NETHERRACK.asItem(),
            Blocks.STONE.asItem()
    )));
    
    public final Setting<List<Block>> blocksToAvoid = new Setting<>(new ArrayList<>());
    public final Setting<List<Block>> blocksToDisallowBreaking = new Setting<>(new ArrayList<>());
    
    public final Setting<List<Block>> blocksToAvoidBreaking = new Setting<>(new ArrayList<>(Arrays.asList(
            Blocks.CRAFTING_TABLE,
            Blocks.FURNACE,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST
    )));
    
    public final Setting<Double> avoidBreakingMultiplier = new Setting<>(.1);
    public final Setting<List<Block>> buildIgnoreBlocks = new Setting<>(new ArrayList<>());
    public final Setting<List<Block>> buildSkipBlocks = new Setting<>(new ArrayList<>());
    public final Setting<Map<Block, List<Block>>> buildValidSubstitutes = new Setting<>(new HashMap<>());
    public final Setting<Map<Block, List<Block>>> buildSubstitutes = new Setting<>(new HashMap<>());
    public final Setting<List<Block>> okIfAir = new Setting<>(new ArrayList<>());
    public final Setting<Boolean> buildIgnoreExisting = new Setting<>(false);
    public final Setting<Boolean> buildIgnoreDirection = new Setting<>(false);
    public final Setting<List<String>> buildIgnoreProperties = new Setting<>(new ArrayList<>());
    public final Setting<Boolean> avoidUpdatingFallingBlocks = new Setting<>(true);
    public final Setting<Boolean> allowVines = new Setting<>(false);
    public final Setting<Boolean> allowWalkOnBottomSlab = new Setting<>(true);
    public final Setting<Boolean> allowParkour = new Setting<>(false);
    public final Setting<Boolean> allowParkourPlace = new Setting<>(false);
    public final Setting<Boolean> considerPotionEffects = new Setting<>(true);
    public final Setting<Boolean> sprintAscends = new Setting<>(true);
    public final Setting<Boolean> overshootTraverse = new Setting<>(true);
    public final Setting<Boolean> pauseMiningForFallingBlocks = new Setting<>(true);
    public final Setting<Integer> rightClickSpeed = new Setting<>(4);
    public final Setting<Double> randomLooking113 = new Setting<>(2d);
    public final Setting<Float> blockReachDistance = new Setting<>(4.5f);
    public final Setting<Integer> blockBreakSpeed = new Setting<>(6);
    public final Setting<Double> randomLooking = new Setting<>(0.01d);
    public final Setting<Double> costHeuristic = new Setting<>(3.563);
    public final Setting<Integer> pathingMaxChunkBorderFetch = new Setting<>(50);
    public final Setting<Double> backtrackCostFavoringCoefficient = new Setting<>(0.5);
    public final Setting<Boolean> avoidance = new Setting<>(false);
    public final Setting<Double> mobSpawnerAvoidanceCoefficient = new Setting<>(2.0);
    public final Setting<Integer> mobSpawnerAvoidanceRadius = new Setting<>(16);
    public final Setting<Double> mobAvoidanceCoefficient = new Setting<>(1.5);
    public final Setting<Integer> mobAvoidanceRadius = new Setting<>(8);
    public final Setting<Boolean> rightClickContainerOnArrival = new Setting<>(true);
    public final Setting<Boolean> enterPortal = new Setting<>(true);
    public final Setting<Boolean> minimumImprovementRepropagation = new Setting<>(true);
    public final Setting<Boolean> cutoffAtLoadBoundary = new Setting<>(false);
    public final Setting<Double> maxCostIncrease = new Setting<>(10D);
    public final Setting<Integer> costVerificationLookahead = new Setting<>(5);
    public final Setting<Double> pathCutoffFactor = new Setting<>(0.9);
    public final Setting<Integer> pathCutoffMinimumLength = new Setting<>(30);
    public final Setting<Integer> planningTickLookahead = new Setting<>(150);
    public final Setting<Integer> pathingMapDefaultSize = new Setting<>(1024);
    public final Setting<Float> pathingMapLoadFactor = new Setting<>(0.75f);
    public final Setting<Integer> maxFallHeightNoWater = new Setting<>(3);
    public final Setting<Integer> maxFallHeightBucket = new Setting<>(20);
    public final Setting<Boolean> allowOvershootDiagonalDescend = new Setting<>(true);
    public final Setting<Boolean> simplifyUnloadedYCoord = new Setting<>(true);
    public final Setting<Boolean> repackOnAnyBlockChange = new Setting<>(true);
    public final Setting<Integer> movementTimeoutTicks = new Setting<>(100);
    public final Setting<Long> primaryTimeoutMS = new Setting<>(500L);
    public final Setting<Long> failureTimeoutMS = new Setting<>(2000L);
    public final Setting<Long> planAheadPrimaryTimeoutMS = new Setting<>(4000L);
    public final Setting<Long> planAheadFailureTimeoutMS = new Setting<>(5000L);
    public final Setting<Boolean> slowPath = new Setting<>(false);
    public final Setting<Long> slowPathTimeDelayMS = new Setting<>(100L);
    public final Setting<Long> slowPathTimeoutMS = new Setting<>(40000L);
    public final Setting<Boolean> doBedWaypoints = new Setting<>(true);
    public final Setting<Boolean> doDeathWaypoints = new Setting<>(true);
    public final Setting<Boolean> chunkCaching = new Setting<>(true);
    public final Setting<Boolean> pruneRegionsFromRAM = new Setting<>(true);
    public final Setting<Integer> chunkPackerQueueMaxSize = new Setting<>(2000);
    public final Setting<Boolean> backfill = new Setting<>(false);
    public final Setting<Boolean> logAsToast = new Setting<>(false);
    public final Setting<Long> toastTimer = new Setting<>(5000L);
    public final Setting<Boolean> chatDebug = new Setting<>(false);
    public final Setting<Boolean> chatControl = new Setting<>(true);
    public final Setting<Boolean> chatControlAnyway = new Setting<>(false);
    public final Setting<Boolean> renderPath = new Setting<>(true);
    public final Setting<Boolean> renderPathAsLine = new Setting<>(false);
    public final Setting<Boolean> renderGoal = new Setting<>(true);
    public final Setting<Boolean> renderGoalAnimated = new Setting<>(true);
    public final Setting<Boolean> renderSelectionBoxes = new Setting<>(true);
    public final Setting<Boolean> renderGoalIgnoreDepth = new Setting<>(true);
    public final Setting<Boolean> renderGoalXZBeacon = new Setting<>(false);
    public final Setting<Boolean> renderSelectionBoxesIgnoreDepth = new Setting<>(true);
    public final Setting<Boolean> renderPathIgnoreDepth = new Setting<>(true);
    public final Setting<Float> pathRenderLineWidthPixels = new Setting<>(5F);
    public final Setting<Float> goalRenderLineWidthPixels = new Setting<>(3F);
    public final Setting<Boolean> fadePath = new Setting<>(false);
    public final Setting<Boolean> freeLook = new Setting<>(true);
    public final Setting<Boolean> blockFreeLook = new Setting<>(false);
    public final Setting<Boolean> elytraFreeLook = new Setting<>(true);
    public final Setting<Boolean> smoothLook = new Setting<>(false);
    public final Setting<Boolean> elytraSmoothLook = new Setting<>(false);
    public final Setting<Integer> smoothLookTicks = new Setting<>(5);
    public final Setting<Boolean> remainWithExistingLookDirection = new Setting<>(true);
    public final Setting<Boolean> antiCheatCompatibility = new Setting<>(true);
    public final Setting<Boolean> pathThroughCachedOnly = new Setting<>(false);
    public final Setting<Boolean> sprintInWater = new Setting<>(true);
    public final Setting<Boolean> blacklistClosestOnFailure = new Setting<>(true);
    public final Setting<Boolean> renderCachedChunks = new Setting<>(false);
    public final Setting<Float> cachedChunksOpacity = new Setting<>(0.5f);
    public final Setting<Boolean> prefixControl = new Setting<>(true);
    public final Setting<String> prefix = new Setting<>("#");
    public final Setting<Boolean> shortBaritonePrefix = new Setting<>(false);
    public final Setting<Boolean> useMessageTag = new Setting<>(false);
    public final Setting<Boolean> echoCommands = new Setting<>(true);
    public final Setting<Boolean> censorCoordinates = new Setting<>(false);
    public final Setting<Boolean> censorRanCommands = new Setting<>(false);
    public final Setting<Boolean> itemSaver = new Setting<>(false);
    public final Setting<Integer> itemSaverThreshold = new Setting<>(10);
    public final Setting<Boolean> preferSilkTouch = new Setting<>(false);
    public final Setting<Boolean> walkWhileBreaking = new Setting<>(true);
    public final Setting<Boolean> splicePath = new Setting<>(true);
    public final Setting<Integer> maxPathHistoryLength = new Setting<>(300);
    public final Setting<Integer> pathHistoryCutoffAmount = new Setting<>(50);
    public final Setting<Integer> mineGoalUpdateInterval = new Setting<>(5);
    public final Setting<Integer> maxCachedWorldScanCount = new Setting<>(10);
    public final Setting<Integer> mineMaxOreLocationsCount = new Setting<>(64);
    public final Setting<Integer> minYLevelWhileMining = new Setting<>(0);
    public final Setting<Integer> maxYLevelWhileMining = new Setting<>(2031);
    public final Setting<Boolean> allowOnlyExposedOres = new Setting<>(false);
    public final Setting<Integer> allowOnlyExposedOresDistance = new Setting<>(1);
    public final Setting<Boolean> exploreForBlocks = new Setting<>(true);
    public final Setting<Integer> worldExploringChunkOffset = new Setting<>(0);
    public final Setting<Integer> exploreChunkSetMinimumSize = new Setting<>(10);
    public final Setting<Integer> exploreMaintainY = new Setting<>(64);
    public final Setting<Boolean> replantCrops = new Setting<>(true);
    public final Setting<Boolean> replantNetherWart = new Setting<>(false);
    public final Setting<Integer> farmMaxScanSize = new Setting<>(256);
    public final Setting<Boolean> extendCacheOnThreshold = new Setting<>(false);
    public final Setting<Boolean> buildInLayers = new Setting<>(false);
    public final Setting<Boolean> layerOrder = new Setting<>(false);
    public final Setting<Integer> layerHeight = new Setting<>(1);
    public final Setting<Integer> startAtLayer = new Setting<>(0);
    public final Setting<Boolean> skipFailedLayers = new Setting<>(false);
    public final Setting<Boolean> buildOnlySelection = new Setting<>(false);
    public final Setting<Vec3i> buildRepeat = new Setting<>(new Vec3i(0, 0, 0));
    public final Setting<Integer> buildRepeatCount = new Setting<>(-1);
    public final Setting<Boolean> buildRepeatSneaky = new Setting<>(true);
    public final Setting<Boolean> breakFromAbove = new Setting<>(false);
    public final Setting<Boolean> goalBreakFromAbove = new Setting<>(false);
    public final Setting<Boolean> mapArtMode = new Setting<>(false);
    public final Setting<Boolean> okIfWater = new Setting<>(false);
    public final Setting<Integer> incorrectSize = new Setting<>(100);
    public final Setting<Double> breakCorrectBlockPenaltyMultiplier = new Setting<>(10d);
    public final Setting<Double> placeIncorrectBlockPenaltyMultiplier = new Setting<>(2d);
    public final Setting<Boolean> schematicOrientationX = new Setting<>(false);
    public final Setting<Boolean> schematicOrientationY = new Setting<>(false);
    public final Setting<Boolean> schematicOrientationZ = new Setting<>(false);
    public final Setting<Rotation> buildSchematicRotation = new Setting<>(Rotation.NONE);
    public final Setting<Mirror> buildSchematicMirror = new Setting<>(Mirror.NONE);
    public final Setting<String> schematicFallbackExtension = new Setting<>("schematic");
    public final Setting<Integer> builderTickScanRadius = new Setting<>(5);
    public final Setting<Boolean> mineScanDroppedItems = new Setting<>(true);
    public final Setting<Long> mineDropLoiterDurationMSThanksLouca = new Setting<>(250L);
    public final Setting<Boolean> distanceTrim = new Setting<>(true);
    public final Setting<Boolean> cancelOnGoalInvalidation = new Setting<>(true);
    public final Setting<Integer> axisHeight = new Setting<>(120);
    public final Setting<Boolean> disconnectOnArrival = new Setting<>(false);
    public final Setting<Boolean> legitMine = new Setting<>(false);
    public final Setting<Integer> legitMineYLevel = new Setting<>(-59);
    public final Setting<Boolean> legitMineIncludeDiagonals = new Setting<>(false);
    public final Setting<Boolean> forceInternalMining = new Setting<>(true);
    public final Setting<Boolean> internalMiningAirException = new Setting<>(true);
    public final Setting<Double> followOffsetDistance = new Setting<>(0D);
    public final Setting<Float> followOffsetDirection = new Setting<>(0F);
    public final Setting<Integer> followRadius = new Setting<>(3);
    public final Setting<Integer> followTargetMaxDistance = new Setting<>(0);
    public final Setting<Boolean> disableCompletionCheck = new Setting<>(false);
    public final Setting<Long> cachedChunksExpirySeconds = new Setting<>(-1L);
    
    // Base hunting settings - Added for anarchy servers
    public final Setting<Boolean> baseHuntingEnabled = new Setting<>(false);
    public final Setting<Integer> baseDetectionRadius = new Setting<>(128);
    public final Setting<Boolean> avoidPlayerDetection = new Setting<>(true);
    public final Setting<Integer> playerDetectionRadius = new Setting<>(32);
    public final Setting<Double> playerAvoidanceCoefficient = new Setting<>(3.0);
    public final Setting<List<Block>> baseIndicatorBlocks = new Setting<>(new ArrayList<>(Arrays.asList(
            Blocks.CHEST, 
            Blocks.TRAPPED_CHEST,
            Blocks.ENDER_CHEST,
            Blocks.FURNACE,
            Blocks.BLAST_FURNACE, 
            Blocks.SMOKER,
            Blocks.BREWING_STAND,
            Blocks.ENCHANTING_TABLE,
            Blocks.ANVIL,
            Blocks.BEACON,
            Blocks.RESPAWN_ANCHOR,
            Blocks.LODESTONE
    )));
    public final Setting<Double> baseIndicatorWeight = new Setting<>(2.0);
    public final Setting<List<Block>> infrastructureBlocks = new Setting<>(new ArrayList<>(Arrays.asList(
            Blocks.RAIL,
            Blocks.POWERED_RAIL,
            Blocks.DETECTOR_RAIL,
            Blocks.ACTIVATOR_RAIL,
            Blocks.HOPPER,
            Blocks.DROPPER,
            Blocks.DISPENSER,
            Blocks.STONE_BRICKS,
            Blocks.NETHER_BRICKS
    )));
    public final Setting<Integer> baseIndicatorThreshold = new Setting<>(3);
    public final Setting<Boolean> ignoreFakeBaseIndicators = new Setting<>(true);
    public final Setting<Boolean> stealthMode = new Setting<>(false);
    public final Setting<Integer> stealthModeWaitTicks = new Setting<>(60);
    public final Setting<Integer> baseScanCooldownSeconds = new Setting<>(300);
    public final Setting<Boolean> optimizeForDistantBases = new Setting<>(true);
    public final Setting<Double> randomPathDeviation = new Setting<>(0.0);
    public final Setting<Boolean> prioritizeCoveredApproach = new Setting<>(false);
    public final Setting<Integer> maxReportedBases = new Setting<>(50);
    public final Setting<Boolean> saveDiscoveredBases = new Setting<>(true);
    public final Setting<Boolean> followHighwaysTowardsBase = new Setting<>(true);
    public final Setting<Double> highwayPreferenceMultiplier = new Setting<>(0.7);
    
    @JavaOnly
    public final Setting<Consumer<Component>> logger = new Setting<>((msg) -> {
        try {
            final GuiMessageTag tag = useMessageTag.value ? Helper.MESSAGE_TAG : null;
            Minecraft.getInstance().gui.getChat().addMessage(msg, null, tag);
        } catch (Throwable t) {
            LOGGER.warn("Failed to log message to chat: " + msg.getString(), t);
        }
    });
    
    @JavaOnly
    public final Setting<BiConsumer<String, Boolean>> notifier = new Setting<>(NotificationHelper::notify);
    
    @JavaOnly
    public final Setting<BiConsumer<Component, Component>> toaster = new Setting<>(BaritoneToast::addOrUpdate);
    
    public final Setting<Boolean> verboseCommandExceptions = new Setting<>(false);
    public final Setting<Double> yLevelBoxSize = new Setting<>(15D);
    public final Setting<Color> colorCurrentPath = new Setting<>(Color.RED);
    public final Setting<Color> colorNextPath = new Setting<>(Color.MAGENTA);
    public final Setting<Color> colorBlocksToBreak = new Setting<>(Color.RED);
    public final Setting<Color> colorBlocksToPlace = new Setting<>(Color.GREEN);
    public final Setting<Color> colorBlocksToWalkInto = new Setting<>(Color.MAGENTA);
    public final Setting<Color> colorBestPathSoFar = new Setting<>(Color.BLUE);
    public final Setting<Color> colorMostRecentConsidered = new Setting<>(Color.CYAN);
    public final Setting<Color> colorGoalBox = new Setting<>(Color.GREEN);
    public final Setting<Color> colorInvertedGoalBox = new Setting<>(Color.RED);
    public final Setting<Color> colorSelection = new Setting<>(Color.CYAN);
    public final Setting<Color> colorSelectionPos1 = new Setting<>(Color.BLACK);
    public final Setting<Color> colorSelectionPos2 = new Setting<>(Color.ORANGE);
    public final Setting<Color> colorBaseIndicator = new Setting<>(new Color(255, 0, 128));
    public final Setting<Float> selectionOpacity = new Setting<>(.5f);
    public final Setting<Float> selectionLineWidth = new Setting<>(2F);
    public final Setting<Boolean> renderSelection = new Setting<>(true);
    public final Setting<Boolean> renderSelectionIgnoreDepth = new Setting<>(true);
    public final Setting<Boolean> renderSelectionCorners = new Setting<>(true);
    public final Setting<Boolean> renderBaseIndicators = new Setting<>(true);
    public final Setting<Boolean> useSwordToMine = new Setting<>(true);
    public final Setting<Boolean> desktopNotifications = new Setting<>(false);
    public final Setting<Boolean> notificationOnPathComplete = new Setting<>(true);
    public final Setting<Boolean> notificationOnFarmFail = new Setting<>(true);
    public final Setting<Boolean> notificationOnBuildFinished = new Setting<>(true);
    public final Setting<Boolean> notificationOnExploreFinished = new Setting<>(true);
    public final Setting<Boolean> notificationOnMineFail = new Setting<>(true);
    public final Setting<Boolean> notificationOnBaseFound = new Setting<>(true);
    public final Setting<Integer> elytraSimulationTicks = new Setting<>(20);
    public final Setting<Integer> elytraPitchRange = new Setting<>(25);
    public final Setting<Double> elytraFireworkSpeed = new Setting<>(1.2);
    public final Setting<Integer> elytraFireworkSetbackUseDelay = new Setting<>(15);
    public final Setting<Double> elytraMinimumAvoidance = new Setting<>(0.2);
    public final Setting<Boolean> elytraConserveFireworks = new Setting<>(false);
    public final Setting<Boolean> elytraRenderRaytraces = new Setting<>(false);
    public final Setting<Boolean> elytraRenderHitboxRaytraces = new Setting<>(false);
    public final Setting<Boolean> elytraRenderSimulation = new Setting<>(true);
    public final Setting<Boolean> elytraAutoJump = new Setting<>(false);
    public final Setting<Long> elytraNetherSeed = new Setting<>(146008555100680L);
    public final Setting<Boolean> elytraPredictTerrain = new Setting<>(false);
    public final Setting<Boolean> elytraAutoSwap = new Setting<>(true);
    public final Setting<Integer> elytraMinimumDurability = new Setting<>(5);
    public final Setting<Integer> elytraMinFireworksBeforeLanding = new Setting<>(5);
    public final Setting<Boolean> elytraAllowEmergencyLand = new Setting<>(true);
    public final Setting<Long> elytraTimeBetweenCacheCullSecs = new Setting<>(TimeUnit.MINUTES.toSeconds(3));
    public final Setting<Integer> elytraCacheCullDistance = new Setting<>(5000);
    public final Setting<Boolean> elytraAllowLandOnNetherFortress = new Setting<>(false);
    public final Setting<Boolean> elytraTermsAccepted = new Setting<>(false);
    public final Setting<Boolean> elytraChatSpam = new Setting<>(false);
    
    public final Map<String, Setting<?>> byLowerName;
    public final List<Setting<?>> allSettings;
    public final Map<Setting<?>, Type> settingTypes;

    public final class Setting<T> {
        public T value;
        public final T defaultValue;
        private String name;
        private boolean javaOnly;

        @SuppressWarnings("unchecked")
        private Setting(T value) {
            if (value == null) {
                throw new IllegalArgumentException("Cannot determine value type class from null");
            }
            this.value = value;
            this.defaultValue = value;
            this.javaOnly = false;
        }

        @Deprecated
        public final T get() {
            return value;
        }

        public final String getName() {
            return name;
        }

        public Class<T> getValueClass() {
            return (Class<T>) TypeUtils.resolveBaseClass(getType());
        }

        @Override
        public String toString() {
            return SettingsUtil.settingToString(this);
        }

        public void reset() {
            value = defaultValue;
        }

        public final Type getType() {
            return settingTypes.get(this);
        }

        public boolean isJavaOnly() {
            return javaOnly;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    private @interface JavaOnly {}

    Settings() {
        Field[] temp = getClass().getFields();

        Map<String, Setting<?>> tmpByName = new HashMap<>();
        List<Setting<?>> tmpAll = new ArrayList<>();
        Map<Setting<?>, Type> tmpSettingTypes = new HashMap<>();

        try {
            for (Field field : temp) {
                if (field.getType().equals(Setting.class)) {
                    Setting<?> setting = (Setting<?>) field.get(this);
                    String name = field.getName();
                    setting.name = name;
                    setting.javaOnly = field.isAnnotationPresent(JavaOnly.class);
                    name = name.toLowerCase();
                    if (tmpByName.containsKey(name)) {
                        throw new IllegalStateException("Duplicate setting name");
                    }
                    tmpByName.put(name, setting);
                    tmpAll.add(setting);
                    tmpSettingTypes.put(setting, ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        byLowerName = Collections.unmodifiableMap(tmpByName);
        allSettings = Collections.unmodifiableList(tmpAll);
        settingTypes = Collections.unmodifiableMap(tmpSettingTypes);
    }

    @SuppressWarnings("unchecked")
    public <T> List<Setting<T>> getAllValuesByType(Class<T> cla$$) {
        List<Setting<T>> result = new ArrayList<>();
        for (Setting<?> setting : allSettings) {
            if (setting.getValueClass().equals(cla$$)) {
                result.add((Setting<T>) setting);
            }
        }
        return result;
    }
}
