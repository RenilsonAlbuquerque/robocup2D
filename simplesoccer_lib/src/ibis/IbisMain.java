package ibis;

import java.net.UnknownHostException;

public class IbisMain {
	
	
	public static void main(String args[]) throws UnknownHostException {
	
		IbisTeam team1 = new IbisTeam("Curin",7,true);
		IbisTeam team2 = new IbisTeam("Paum",7,true);
		team1.launchTeamAndServer();
		team2.launchTeam();
	
	}

}
