package example.ballfollower_team;

import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;


public class BallFollowerTeam extends AbstractTeam {

	public BallFollowerTeam(String suffix) {
		super("BallFollower" + suffix, 2, false);
	}

	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {
		BallFollowerPlayer pl = new BallFollowerPlayer(commander);
		pl.start();
	}

}
