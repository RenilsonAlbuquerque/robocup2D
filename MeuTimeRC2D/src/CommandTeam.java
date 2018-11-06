import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.AbstractTeam;

public class CommandTeam extends AbstractTeam {
	public CommandTeam(String suffix) {
		super("CommandTeam" + suffix, 4, false);
	}
	@Override
	protected void launchPlayer( int ag, PlayerCommander comm) {
		System. out .println("Player lançado!");
		CommandPlayer p = new CommandPlayer(comm);
		p.start();
	}
}