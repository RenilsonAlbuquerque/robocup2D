package FPF.team.Ibis.Main;

import java.net.UnknownHostException;

import FPF.team.Ibis.IbisTeam;


public class MainIbis {

	public static void main(String[] args) throws UnknownHostException {
		IbisTeam team1 = new IbisTeam("Santa");
		IbisTeam team2 = new IbisTeam("Sprot");
		
		team1.launchTeamAndServer();;
		team2.launchTeam();
	}
	
}

