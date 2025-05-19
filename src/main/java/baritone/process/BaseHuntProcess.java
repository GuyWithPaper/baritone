package baritone.process;

import baritone.Baritone;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.Settings;
import baritone.api.utils.BetterBlockPos;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.pathing.goals.GoalComposite;
import baritone.api.cache.ICachedWorld;
import baritone.api.cache.ICachedChunk;
import baritone.api.cache.ICachedRegion;
import baritone.api.event.events.ChatEvent;
import baritone.api.event.events.RenderEvent;
import baritone.api.event.events.TickEvent;
import baritone.api.event.events.BlockChangeEvent;
import baritone.api.event.listener.AbstractGameEventListener;
import baritone.api.IBaritone;
import baritone.utils.BlockStateInterface;
import baritone.utils.PathingCommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.concurrent.TimeUnit;

public final class BaseHuntProcess implements IBaritoneProcess, AbstractGameEventListener {

    private final IBaritone baritone;
    private final Minecraft mc = Minecraft.getInstance();
    private final Map<BlockPos, Double> suspiciousLocations = new ConcurrentHashMap<>();
    private final Set<BlockPos> discoveredBases = new HashSet<>();
    private final Map<UUID, Vec3> trackedPlayers = new ConcurrentHashMap<>();
    
    private Goal currentGoal;
    private BlockPos currentTarget;
    private long lastScanTime;
    private long lastBaseFoundTime;
    private boolean isActive;
    private int scanRadius;
    private int scanDelay;
    private int ticksSinceLastPlayerCheck;
    private boolean stealthModeActive;
    private int stealthModeWaitTicks;
    private Random random = new Random();
    private Path basesFilePath;
    
    public BaseHuntProcess(IBaritone baritone) {
        this.baritone = baritone;
        this.isActive = false;
        this.lastScanTime = 0;
        this.lastBaseFoundTime = 0;
        this.scanRadius = 128;
        this.scanDelay = 300;
        this.ticksSinceLastPlayerCheck = 0;
        this.stealthModeActive = false;
        this.stealthModeWaitTicks = 0;
        
        try {
            String gameDir = Minecraft.getInstance().gameDirectory.getAbsolutePath();
            this.basesFilePath = Paths.get(gameDir, "baritone", "discovered_bases.txt");
            Files.createDirectories(this.basesFilePath.getParent());
            loadDiscoveredBases();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean isActive() {
        return isActive;
    }
    
    @Override
    public void onTick(TickEvent event) {
        if (!isActive || event.getType() != TickEvent.Type.IN) {
            return;
        }
        
        ticksSinceLastPlayerCheck++;
        
        if (baritone.getPlayerContext().player() == null) {
            return;
        }
        
        Settings settings = baritone.settings();
        
        if (settings.avoidPlayerDetection.value && ticksSinceLastPlayerCheck >= 5) {
            updateTrackedPlayers();
            ticksSinceLastPlayerCheck = 0;
        }
        
        if (stealthModeActive) {
            stealthModeWaitTicks--;
            if (stealthModeWaitTicks <= 0) {
                stealthModeActive = false;
            }
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentGoal == null && currentTime - lastScanTime > TimeUnit.SECONDS.toMillis(settings.baseScanCooldownSeconds.value)) {
            scanForBases();
            lastScanTime = currentTime;
        }
        
        if (currentTarget != null && baritone.getPlayerContext().playerFeet().distanceTo(currentTarget) < 5) {
            logBaseFound(currentTarget);
            
            if (!discoveredBases.contains(currentTarget)) {
                discoveredBases.add(currentTarget);
                saveDiscoveredBases();
                
                if (settings.notificationOnBaseFound.value) {
                    sendNotification("Base Found", "Located a player base at " + formatCoords(currentTarget));
                }
                
                // Reset current target and goal
                currentTarget = null;
                currentGoal = null;
                lastBaseFoundTime = currentTime;
            }
        }
    }
    
    @Override
    public void onRender(RenderEvent event) {
        if (!isActive || !baritone.settings().renderBaseIndicators.value) {
            return;
        }
        
        for (Map.Entry<BlockPos, Double> entry : suspiciousLocations.entrySet()) {
            if (entry.getValue() >= baritone.settings().baseIndicatorThreshold.value) {
                renderBaseIndicator(event, entry.getKey(), entry.getValue());
            }
        }
    }
    
    private void renderBaseIndicator(RenderEvent event, BlockPos pos, double score) {
        float alpha = Math.min(1.0f, (float)(score / 10.0));
        event.getGraphics().drawBox(pos, baritone.settings().colorBaseIndicator.value, alpha, true);
    }
    
    @Override
    public void onBlockChange(BlockChangeEvent event) {
        if (!isActive) {
            return;
        }
        
        if (isBaseIndicator(event.getNewState().getBlock())) {
            evaluateLocation(event.getPos());
        }
    }
    
    @Override
    public PathingCommand onProcess(PathingCommandContext ctx) {
        if (!isActive) {
            return new PathingCommand(null, PathingCommandType.CANCEL_AND_SET_GOAL);
        }
        
        if (stealthModeActive) {
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
        }
        
        if (isPlayerNearby()) {
            stealthModeActive = true;
            stealthModeWaitTicks = baritone.settings().stealthModeWaitTicks.value;
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
        }
        
        if (currentGoal == null) {
            BlockPos target = findNextTarget();
            if (target != null) {
                currentTarget = target;
                int approachDistance = baritone.settings().prioritizeCoveredApproach.value ? 16 : 3;
                currentGoal = new GoalNear(target, approachDistance);
                baritone.getGameEventHandler().onPlayerSprintState(baritone.settings().allowSprint.value && !stealthModeActive);
            } else {
                return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
            }
        }
        
        return new PathingCommand(currentGoal, PathingCommandType.REVALIDATE_GOAL_AND_PATH);
    }
    
    @Override
    public boolean isTemporary() {
        return true;
    }
    
    @Override
    public void onLostControl() {
        currentGoal = null;
        isActive = false;
        stealthModeActive = false;
    }
    
    @Override
    public double priority() {
        return 52.0;
    }
    
    @Override
    public String displayName0() {
        return "Base Hunting";
    }
    
    private BlockPos findNextTarget() {
        if (suspiciousLocations.isEmpty()) {
            return null;
        }
        
        BetterBlockPos playerPos = baritone.getPlayerContext().playerFeet();
        int maxBases = baritone.settings().maxReportedBases.value;
        
        return suspiciousLocations.entrySet().stream()
            .filter(entry -> entry.getValue() >= baritone.settings().baseIndicatorThreshold.value)
            .filter(entry -> !discoveredBases.contains(entry.getKey()))
            .sorted((a, b) -> {
                double aScore = a.getValue() / Math.sqrt(playerPos.distanceSq(a.getKey()));
                double bScore = b.getValue() / Math.sqrt(playerPos.distanceSq(b.getKey()));
                return Double.compare(bScore, aScore);
            })
            .limit(maxBases)
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }
    
    private boolean isPlayerNearby() {
        if (!baritone.settings().avoidPlayerDetection.value) {
            return false;
        }
        
        ClientPlayerEntity player = baritone.getPlayerContext().player();
        int detectionRadius = baritone.settings().playerDetectionRadius.value;
        
        for (Vec3 otherPlayerPos : trackedPlayers.values()) {
            double distance = player.position().distanceTo(otherPlayerPos);
            if (distance < detectionRadius) {
                return true;
            }
        }
        
        return false;
    }
    
    private void updateTrackedPlayers() {
        trackedPlayers.clear();
        
        if (mc.level == null) return;
        
        for (Player otherPlayer : mc.level.players()) {
            if (otherPlayer != mc.player) {
                trackedPlayers.put(otherPlayer.getUUID(), otherPlayer.position());
            }
        }
    }
    
    public void scanForBases() {
        suspiciousLocations.clear();
        BlockStateInterface bsi = new BlockStateInterface(baritone.getPlayerContext());
        
        ClientPlayerEntity player = baritone.getPlayerContext().player();
        if (player == null || mc.level == null) return;
        
        int radius = baritone.settings().baseDetectionRadius.value;
        BlockPos playerPos = player.blockPosition();
        
        int chunkX = playerPos.getX() >> 4;
        int chunkZ = playerPos.getZ() >> 4;
        int chunkRadius = (radius >> 4) + 1;
        
        for (int x = chunkX - chunkRadius; x <= chunkX + chunkRadius; x++) {
            for (int z = chunkZ - chunkRadius; z <= chunkZ + chunkRadius; z++) {
                LevelChunk chunk = mc.level.getChunk(x, z);
                scanChunk(chunk);
            }
        }
        
        if (baritone.settings().optimizeForDistantBases.value) {
            scanCachedChunks();
        }
    }
    
    private void scanChunk(LevelChunk chunk) {
        if (chunk == null) return;
        
        int chunkX = chunk.getPos().x << 4;
        int chunkZ = chunk.getPos().z << 4;
        
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = mc.level.getMinBuildHeight(); y < mc.level.getMaxBuildHeight(); y++) {
                    BlockPos pos = new BlockPos(chunkX + x, y, chunkZ + z);
                    BlockState state = chunk.getBlockState(pos);
                    
                    if (isBaseIndicator(state.getBlock())) {
                        evaluateLocation(pos);
                    }
                }
            }
        }
    }
    
    private void scanCachedChunks() {
        ICachedWorld cachedWorld = baritone.getWorldProvider().getCurrentWorld();
        if (cachedWorld == null) return;
        
        ClientPlayerEntity player = baritone.getPlayerContext().player();
        if (player == null) return;
        
        BetterBlockPos playerPos = BetterBlockPos.from(player);
        int chunkX = playerPos.getX() >> 4;
        int chunkZ = playerPos.getZ() >> 4;
        int chunkRadius = (baritone.settings().baseDetectionRadius.value >> 4) + 1;
        
        for (int x = chunkX - chunkRadius; x <= chunkX + chunkRadius; x++) {
            for (int z = chunkZ - chunkRadius; z <= chunkZ + chunkRadius; z++) {
                ICachedChunk chunk = cachedWorld.getCachedChunk(x, z);
                if (chunk != null) {
                    scanCachedChunk(chunk, x, z);
                }
            }
        }
    }
    
    private void scanCachedChunk(ICachedChunk chunk, int chunkX, int chunkZ) {
        if (chunk == null) return;
        
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = mc.level.getMinBuildHeight(); y < mc.level.getMaxBuildHeight(); y++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (isBaseIndicator(block)) {
                        BlockPos pos = new BlockPos(baseX + x, y, baseZ + z);
                        evaluateLocation(pos);
                    }
                }
            }
        }
    }
    
    private void evaluateLocation(BlockPos pos) {
        if (discoveredBases.contains(pos)) {
            return;
        }
        
        BetterBlockPos playerPos = baritone.getPlayerContext().playerFeet();
        if (playerPos.distanceSq(pos) > Math.pow(baritone.settings().baseDetectionRadius.value, 2)) {
            return;
        }
        
        double score = evaluateBaseIndicator(pos);
        if (score > 0) {
            suspiciousLocations.put(pos, score);
        }
    }
    
    private double evaluateBaseIndicator(BlockPos pos) {
        double score = 0;
        Settings settings = baritone.settings();
        BlockStateInterface bsi = new BlockStateInterface(baritone.getPlayerContext());
        
        // Check the block itself
        Block block = bsi.getBlock(pos).getBlock();
        if (isBaseIndicator(block)) {
            score += settings.baseIndicatorWeight.value;
        }
        
        // Check surrounding blocks
        int radius = 5;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    
                    BlockPos checkPos = pos.offset(x, y, z);
                    Block checkBlock = bsi.getBlock(checkPos).getBlock();
                    
                    if (isBaseIndicator(checkBlock)) {
                        double distance = Math.sqrt(x*x + y*y + z*z);
                        score += settings.baseIndicatorWeight.value / Math.max(1, distance);
                    } else if (isInfrastructureBlock(checkBlock)) {
                        double distance = Math.sqrt(x*x + y*y + z*z);
                        score += 0.5 / Math.max(1, distance);
                    }
                }
            }
        }
        
        if (settings.ignoreFakeBaseIndicators.value && score < 1.5) {
            return 0;
        }
        
        return score;
    }
    
    private boolean isBaseIndicator(Block block) {
        return baritone.settings().baseIndicatorBlocks.value.contains(block);
    }
    
    private boolean isInfrastructureBlock(Block block) {
        return baritone.settings().infrastructureBlocks.value.contains(block);
    }
    
    private void logBaseFound(BlockPos pos) {
        String coords = formatCoords(pos);
        baritone.logDirect("Base found at " + coords);
    }
    
    private String formatCoords(BlockPos pos) {
        return String.format("X: %d, Y: %d, Z: %d", pos.getX(), pos.getY(), pos.getZ());
    }
    
    private void sendNotification(String title, String message) {
        if (baritone.settings().desktopNotifications.value) {
            baritone.settings().notifier.value.accept(title + " - " + message, true);
        }
        
        Component titleComponent = Component.literal(title).withStyle(ChatFormatting.GREEN);
        Component messageComponent = Component.literal(message);
        
        if (baritone.settings().logAsToast.value) {
            baritone.settings().toaster.value.accept(titleComponent, messageComponent);
        }
    }
    
    private void loadDiscoveredBases() {
        if (!baritone.settings().saveDiscoveredBases.value) {
            return;
        }
        
        try {
            if (Files.exists(basesFilePath)) {
                List<String> lines = Files.readAllLines(basesFilePath);
                discoveredBases.clear();
                
                for (String line : lines) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        try {
                            int x = Integer.parseInt(parts[0]);
                            int y = Integer.parseInt(parts[1]);
                            int z = Integer.parseInt(parts[2]);
                            discoveredBases.add(new BlockPos(x, y, z));
                        } catch (NumberFormatException e) {
                            // Skip invalid entries
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void saveDiscoveredBases() {
        if (!baritone.settings().saveDiscoveredBases.value) {
            return;
        }
        
        try {
            List<String> lines = discoveredBases.stream()
                .map(pos -> pos.getX() + "," + pos.getY() + "," + pos.getZ())
                .collect(Collectors.toList());
            
            Files.write(basesFilePath, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void start() {
        if (!isActive) {
            isActive = true;
            lastScanTime = 0;
            currentGoal = null;
            currentTarget = null;
            baritone.logDirect("Base hunting started");
        }
    }
    
    public void stop() {
        if (isActive) {
            isActive = false;
            baritone.logDirect("Base hunting stopped");
        }
    }
    
    public void clearDiscoveredBases() {
        discoveredBases.clear();
        saveDiscoveredBases();
        baritone.logDirect("Cleared discovered bases");
    }
    
    public List<BlockPos> getDiscoveredBases() {
        return new ArrayList<>(discoveredBases);
    }
    
    public List<Map.Entry<BlockPos, Double>> getPotentialBases() {
        return suspiciousLocations.entrySet().stream()
            .filter(entry -> entry.getValue() >= baritone.settings().baseIndicatorThreshold.value)
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(baritone.settings().maxReportedBases.value)
            .collect(Collectors.toList());
    }
}
