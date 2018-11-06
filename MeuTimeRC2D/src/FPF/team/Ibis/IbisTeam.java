package FPF.team.Ibis;

import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;


public class IbisTeam extends AbstractTeam {

	public IbisTeam(String suffix) {
		super("Time_" + suffix, 7, false);
	}

	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {
		IbisPlayer pl = new IbisPlayer(commander);
		pl.start();
	}

}
