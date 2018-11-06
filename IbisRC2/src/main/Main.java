package main;

import java.net.UnknownHostException;

public class Main {
	
	public static void main(String args[]) throws UnknownHostException  {
		
			Team teama = new Team("A",7);
			Team teamb = new Team("B",7);
			teama.launchTeamAndServer();
			teamb.launchTeam();
			
			
		
		
	}

}
