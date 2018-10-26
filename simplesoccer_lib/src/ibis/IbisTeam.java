package ibis;



import java.net.UnknownHostException;

import simple_soccer_lib.PlayerCommander;

public class IbisTeam{

	private String hostName;
	private int port;

	private String teamName;
	private int numPlayers;
	private boolean withGoalie;
	
	public IbisTeam(String name, int players, String host, int port, boolean withGoalie) {
		this.hostName = host;
		this.port = port;
		this.teamName = name;
		this.numPlayers = players;
		this.withGoalie = withGoalie;
	}
	
	public IbisTeam(String name, int players, boolean withGoalie) {
		this.hostName = "localhost";
		this.port = 6000;
		this.teamName = name;
		this.numPlayers = players;
		this.withGoalie = withGoalie;
	}

	protected void launchPlayer(int ag, PlayerCommander commander, EnumPlayerPosition position) {
		switch(position.ordinal()) {
		case 0 :
			IbisPlayer player = new Goalkeeper(commander);
			player.start();
			break;
		case 1 | 2:
			IbisPlayer player2 = new CenterBack(commander);
			player2.start();
			break;
		case 3 | 4 | 5:
			IbisPlayer player3 = new MidFielder(commander);
			player3.start();
			break;
		case 6:
			IbisPlayer player4 = new Farward(commander);
			player4.start();
			break;
		}
	}
	
	public final void launchTeam(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				PlayerCommander commander;
				
				System.out.println(" >> Iniciando o time...");
				for (int i = 0; i < IbisTeam.this.numPlayers; i++) {
					try{
						switch(i) {
						case 0:
							commander = new PlayerCommander(teamName, hostName, port, withGoalie);
							launchPlayer(i, commander,EnumPlayerPosition.goalkeeper);
							break;
						case 1 | 2:
							commander = new PlayerCommander(teamName, hostName, port, false);
							launchPlayer(i, commander,EnumPlayerPosition.centerBack);
							break;
						case 3 | 4 | 5:
							commander = new PlayerCommander(teamName, hostName, port, false);
							launchPlayer(i, commander,EnumPlayerPosition.ofensiveMidFielder);
							break;
						case 6:
							commander = new PlayerCommander(teamName, hostName, port, false);
							launchPlayer(i, commander,EnumPlayerPosition.farward);
							break;
						}
						
						
					}catch(UnknownHostException uhe){
						System.err.println("N�o foi poss�vel conectar ao host: "+ IbisTeam.this.hostName);
						uhe.printStackTrace();
					}
					try {
						Thread.sleep(250);
					} catch (Exception e) {}
				}
			}
		}).start();
	}
	
	public final void launchTeamAndServer() throws UnknownHostException {
		launchServer();
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		launchTeam();
	}
	
	public final void launchServer() {
		try {
			System.out.println(" >> Iniciando servidor...");
			
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("cmd /c tools\\startServer.cmd");
			p.waitFor();
//			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
//			String line = "";
//			while ((line = b.readLine()) != null) {
//			  System.out.println(line);
//			  System.out.println(".");
//			}
//			b.close();

        } catch(Exception e) {
        	e.printStackTrace();
        	System.err.println("N�o pode iniciar o servidor!");
        	return;
        }
	}

}
