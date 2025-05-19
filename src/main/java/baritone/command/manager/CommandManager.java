package baritone.command.defaults;

import baritone.api.IBaritone;
import baritone.api.command.ICommand;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class DefaultCommands {

    private DefaultCommands() {}

    public static Stream<ICommand> createAll(IBaritone baritone) {
        return Stream.of(
                new HelpCommand(baritone),
                new SetCommand(baritone),
                new CommandAlias(baritone, Arrays.asList("m", "mine"), "mine"),
                new MineCommand(baritone),
                new ClickCommand(baritone),
                new GotoCommand(baritone),
                new PathCommand(baritone),
                new GoalCommand(baritone),
                new BuildCommand(baritone),
                new SelCommand(baritone),
                new RepackCommand(baritone),
                new SchematicaCommand(baritone),
                new ComeCommand(baritone),
                new AxisCommand(baritone),
                new FarmCommand(baritone),
                new FollowCommand(baritone),
                new CommandAlias(baritone, Arrays.asList("b"), "build"),
                new ExploreCommand(baritone),
                new InvertCommand(baritone),
                new VersionCommand(baritone),
                new TunnelCommand(baritone),
                new RenderCommand(baritone),
                new TimeoutCommand(baritone),
                new BlacklistCommand(baritone),
                new SaveCommand(baritone),
                new ProcCommand(baritone),
                new ReloadAllCommand(baritone),
                new CourseCommand(baritone),
                new CancelCommand(baritone),
                new ForceInternalMiningCommand(baritone),
                new ETACommand(baritone),
                new ThrowCommand(baritone),
                new RotateCommand(baritone),
                new FetchCommand(baritone),
                new FindCommand(baritone),
                new SurfaceCommand(baritone),
                new ExecutionControlCommands.PauseCommand(baritone),
                new ExecutionControlCommands.ResumeCommand(baritone),
                new ExecutionControlCommands.PauseResumeCommand(baritone),
                new ElytraCommand(baritone),
                new BaseHuntCommand(baritone)
        );
    }
}
