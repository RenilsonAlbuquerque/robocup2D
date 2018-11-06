import java.net.UnknownHostException;

public class Main {
public static void main(String[] args) {
		try {
			CommandTeam teamA = new CommandTeam("A");
			CommandTeam teamB = new CommandTeam("A");
			teamA.launchTeamAndServer();
			teamB.launchTeam();
		}catch (UnknownHostException e) {
			System. out .println("Falha ao conectar.");
		}
	}
}