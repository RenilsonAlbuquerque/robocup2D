package team.legacy.Tracker;

import java.io.IOException;
import java.net.InetAddress;

public class MainTracker {

	public static void main(String[] args) {
		int numPlayers = 7; 
		
		try {			
			launchServer();
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			for (int i = 0; i < numPlayers; i++) {
				Tracker player = new Tracker(InetAddress.getByName("localhost"), 6000, "Tracker");
				player.run();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void launchServer() {
		try {
			System.out.println(" >> Iniciando servidor...");
			
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("cmd /c tools\\startServer.cmd");
			p.waitFor();

        } catch(Exception e) {
        	e.printStackTrace();
        	System.err.println("N�o pode iniciar o servidor!");
        	return;
        }
	}

}
