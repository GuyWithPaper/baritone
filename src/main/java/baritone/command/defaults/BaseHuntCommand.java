package baritone.command.defaults;

import baritone.api.IBaritone;
import baritone.api.command.Command;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandException;
import baritone.api.command.helpers.TabCompleteHelper;
import baritone.process.BaseHuntProcess;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class BaseHuntCommand extends Command {
    private static final String[] ACTIONS = {"start", "stop", "clear", "list", "scan", "potential", "setup", "help"};

    public BaseHuntCommand(IBaritone baritone) {
        super(baritone, "basehunt", "hunt", "findbases");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        BaseHuntProcess process = baritone.getPathingBehavior().getBaseHuntProcess();
        
        String action = args.hasAny() ? args.getString().toLowerCase() : "help";
        
        switch (action) {
            case "start":
                process.start();
                logDirect("Base hunting started");
                break;
                
            case "stop":
                process.stop();
                logDirect("Base hunting stopped");
                break;
                
            case "clear":
                process.clearDiscoveredBases();
                logDirect("Cleared discovered bases");
                break;
                
            case "list":
                listDiscoveredBases(process);
                break;
                
            case "scan":
                process.scanForBases();
                logDirect("Scanning for bases...");
                break;
                
            case "potential":
                listPotentialBases(process);
                break;
                
            case "setup":
                setupBaseHunting();
                break;
                
            case "help":
            default:
                showHelp();
                break;
        }
    }

    private void listDiscoveredBases(BaseHuntProcess process) {
        List<BlockPos> bases = process.getDiscoveredBases();
        
        if (bases.isEmpty()) {
            logDirect("No bases discovered yet");
            return;
        }
        
        logDirect("Discovered bases (" + bases.size() + "):");
        for (int i = 0; i < bases.size(); i++) {
            BlockPos pos = bases.get(i);
            Component message = Component.literal(String.format("%d. %d, %d, %d", i + 1, pos.getX(), pos.getY(), pos.getZ()));
            
            Style style = Style.EMPTY
                .withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("#goto %d %d %d", pos.getX(), pos.getY(), pos.getZ())))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to go to this base")));
            
            logDirect(message.withStyle(style));
        }
    }

    private void listPotentialBases(BaseHuntProcess process) {
        List<Map.Entry<BlockPos, Double>> potentialBases = process.getPotentialBases();
        
        if (potentialBases.isEmpty()) {
            logDirect("No potential bases detected");
            return;
        }
        
        logDirect("Potential bases (" + potentialBases.size() + "):");
        for (int i = 0; i < potentialBases.size(); i++) {
            Map.Entry<BlockPos, Double> entry = potentialBases.get(i);
            BlockPos pos = entry.getKey();
            double score = entry.getValue();
            
            Component message = Component.literal(String.format("%d. %d, %d, %d (Score: %.2f)", 
                i + 1, pos.getX(), pos.getY(), pos.getZ(), score));
            
            Style style = Style.EMPTY
                .withColor(ChatFormatting.YELLOW)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("#goto %d %d %d", pos.getX(), pos.getY(), pos.getZ())))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to go to this location")));
            
            logDirect(message.withStyle(style));
        }
    }

    private void setupBaseHunting() {
        baritone.settings().baseHuntingEnabled.value = true;
        baritone.settings().avoidPlayerDetection.value = true;
        baritone.settings().playerDetectionRadius.value = 32;
        baritone.settings().baseDetectionRadius.value = 128;
        baritone.settings().avoidance.value = true;
        baritone.settings().mobAvoidanceCoefficient.value = 2.0;
        baritone.settings().randomPathDeviation.value = 0.1;
        baritone.settings().desktopNotifications.value = true;
        baritone.settings().notificationOnBaseFound.value = true;
        baritone.settings().renderBaseIndicators.value = true;
        
        logDirect("Base hunting settings configured");
    }

    private void showHelp() {
        logDirect("Base Hunting Commands:");
        logDirect("  start - Start base hunting");
        logDirect("  stop - Stop base hunting");
        logDirect("  scan - Manually scan for bases in the area");
        logDirect("  list - Show discovered bases");
        logDirect("  potential - Show potential base locations");
        logDirect("  clear - Clear the list of discovered bases");
        logDirect("  setup - Configure optimal settings for base hunting");
        logDirect("  help - Show this help message");
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (args.hasExactlyOne()) {
            return new TabCompleteHelper()
                .append(ACTIONS)
                .filterPrefix(args.getString())
                .stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Base hunting commands";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
            "The base hunt command provides tools for finding player bases on anarchy servers.",
            "",
            "Usage:",
            "> basehunt - Display help message",
            "> basehunt start - Start hunting for bases",
            "> basehunt stop - Stop base hunting",
            "> basehunt scan - Manually scan for bases in the area",
            "> basehunt list - List discovered bases",
            "> basehunt potential - List potential base locations",
            "> basehunt clear - Clear the list of discovered bases",
            "> basehunt setup - Set up optimal settings for base hunting"
        );
    }
}
