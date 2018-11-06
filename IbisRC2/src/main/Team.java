package main;

import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;

public class Team extends AbstractTeam {

	public Team(String name,int qtdPlayers) {
		super(name,qtdPlayers,true);
	}

	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {
		// TODO Auto-generated method stub
		Player player = new Player(commander);
		player.start();
		
	}

}
