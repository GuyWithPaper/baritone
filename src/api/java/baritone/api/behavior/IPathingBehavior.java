package baritone.api.behavior;

import baritone.api.pathing.goals.Goal;
import baritone.api.process.*;
import baritone.process.BaseHuntProcess;

public interface IPathingBehavior {
    boolean cancelEverything();
    boolean isPathing();
    PathExecutor getCurrent();
    PathExecutor getNext();
    CalculationContext getCalculationContext();
    IPathingControlManager getPathingControlManager();
    ICustomGoalProcess getCustomGoalProcess();
    FollowProcess getFollowProcess();
    MineProcess getMineProcess();
    GetToBlockProcess getGetToBlockProcess();
    FarmProcess getFarmProcess();
    BuilderProcess getBuilderProcess();
    ExploreProcess getExploreProcess();
    FollowEntityProcess getFollowEntityProcess();
    BuiltInMineProcess getBuiltInMineProcess();
    BaseHuntProcess getBaseHuntProcess();
    Goal getGoal();
    PathExecutor.PathingStatus getPathingStatus();
    boolean isActive();
}
