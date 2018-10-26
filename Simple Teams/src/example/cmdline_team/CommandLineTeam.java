package example.cmdline_team;

import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;

public class CommandLineTeam extends AbstractTeam {

	public CommandLineTeam() {
		super("COMM", 1, false);
	}

	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {
		System.out.println("Player lan�ado");
		CommandLinePlayer p = new CommandLinePlayer(commander);
		p.start();
	}

}
