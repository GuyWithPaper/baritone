
package baritone.behavior;

import baritone.api.IBaritone;
import baritone.api.behavior.IPathingBehavior;
import baritone.api.pathing.goals.Goal;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.process.*;
import baritone.utils.PathingCommandContext;

import java.util.*;
import java.util.stream.Collectors;

public final class PathingBehavior implements IPathingBehavior {
    private final IBaritone baritone;
    
    private final CustomGoalProcess customGoalProcess;
    private final FollowProcess followProcess;
    private final MineProcess mineProcess;
    private final FollowEntityProcess followEntityProcess;
    private final GetToBlockProcess getToBlockProcess;
    private final BuiltInMineProcess builtInMineProcess;
    private final AvoidanceProcess avoidanceProcess; 
    private final ExploreProcess exploreProcess;
    private final FarmProcess farmProcess;
    private final BuilderProcess builderProcess;
    private final ElytraProcess elytraProcess;
    private final BaseHuntProcess baseHuntProcess;
    
    private final List<IBaritoneProcess> processes;
    
    private IBaritoneProcess inProgress;
    private PathingCommand inProgressCommand;
    
    public PathingBehavior(IBaritone baritone) {
        this.baritone = baritone;
        
        customGoalProcess = new CustomGoalProcess(baritone);
        followProcess = new FollowProcess(baritone);
        mineProcess = new MineProcess(baritone);
        followEntityProcess = new FollowEntityProcess(baritone);
        getToBlockProcess = new GetToBlockProcess(baritone);
        builtInMineProcess = new BuiltInMineProcess(baritone);
        avoidanceProcess = new AvoidanceProcess(baritone);
        exploreProcess = new ExploreProcess(baritone);
        farmProcess = new FarmProcess(baritone);
        builderProcess = new BuilderProcess(baritone);
        elytraProcess = new ElytraProcess(baritone);
        baseHuntProcess = new BaseHuntProcess(baritone);
        
        processes = Arrays.asList(
            avoidanceProcess,
            baseHuntProcess,
            getToBlockProcess,
            customGoalProcess,
            farmProcess,
            builderProcess,
            exploreProcess,
            mineProcess,
            builtInMineProcess,
            followProcess,
            followEntityProcess,
            elytraProcess
        );
    }
    
    @Override
    public void onTick(boolean isSafeToCancel) {
        IBaritoneProcess toCancel = inProgress;
        inProgress = null;
        inProgressCommand = null;
        
        baritone.getPathingControlManager().mostRecentInControl().forEach(process -> {
            if (process != null && process.isActive() && !processes.contains(process)) {
                processes.add(process);
            }
        });
        
        PathingCommand command = onTick(new PathingCommandContext(), isSafeToCancel);
        command = repack(command);
        
        if (!isSafeToCancel && activeProcesses().contains(toCancel) && toCancel.isActive() && command.commandType != PathingCommandType.CANCEL_AND_SET_GOAL) {
            inProgress = toCancel;
            inProgressCommand = command;
            return;
        }
        
        switch (command.commandType) {
            case REQUEST_PAUSE:
                baritone.getCustomGoalProcess().getInControlFor(1);
                return;
            case CANCEL_AND_SET_GOAL:
                Goal goal = command.goal;
                baritone.getCustomGoalProcess().setGoalAndPath(goal);
                return;
            case FORCE_REVALIDATE_GOAL_AND_PATH:
            case REVALIDATE_GOAL_AND_PATH:
                Goal goal1 = command.goal;
                baritone.getCustomGoalProcess().setGoalAndPath(goal1);
                return;
            case SET_GOAL_AND_PATH:
                baritone.getCustomGoalProcess().setGoalAndPath(command.goal);
                return;
            case PATH_TO_GOAL:
                baritone.getCustomGoalProcess().setGoal(command.goal);
                baritone.getCustomGoalProcess().path();
                return;
            default:
                throw new IllegalStateException("Unexpected value: " + command.commandType);
        }
    }
    
    private PathingCommand repack(PathingCommand command) {
        if (command == null) {
            return new PathingCommand(null, PathingCommandType.CANCEL_AND_SET_GOAL);
        }
        
        return command;
    }
    
    private PathingCommand onTick(PathingCommandContext context, boolean safeToCancel) {
        List<IBaritoneProcess> processes = activeProcesses();
        
        if (processes.isEmpty()) {
            return new PathingCommand(null, PathingCommandType.CANCEL_AND_SET_GOAL);
        }
        
        IBaritoneProcess process = processes.get(0);
        PathingCommand command = process.onProcess(context);
        inProgress = process;
        inProgressCommand = command;
        return command;
    }
    
    public List<IBaritoneProcess> activeProcesses() {
        return processes.stream()
            .filter(IBaritoneProcess::isActive)
            .sorted(Comparator.comparingDouble(IBaritoneProcess::priority).reversed())
            .collect(Collectors.toList());
    }
    
    public IBaritoneProcess getInProgress() {
        return inProgress;
    }
    
    @Override
    public CustomGoalProcess getCustomGoalProcess() {
        return customGoalProcess;
    }
    
    @Override
    public FollowProcess getFollowProcess() {
        return followProcess;
    }
    
    @Override
    public MineProcess getMineProcess() {
        return mineProcess;
    }
    
    @Override
    public GetToBlockProcess getGetToBlockProcess() {
        return getToBlockProcess;
    }
    
    @Override
    public FarmProcess getFarmProcess() {
        return farmProcess;
    }
    
    @Override
    public ExploreProcess getExploreProcess() {
        return exploreProcess;
    }
    
    @Override
    public BuiltInMineProcess getBuiltInMineProcess() {
        return builtInMineProcess;
    }
    
    @Override
    public FollowEntityProcess getFollowEntityProcess() {
        return followEntityProcess;
    }
    
    @Override
    public BuilderProcess getBuilderProcess() {
        return builderProcess;
    }
    
    public BaseHuntProcess getBaseHuntProcess() {
        return baseHuntProcess;
    }
}
