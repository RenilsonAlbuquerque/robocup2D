package FPF.team.Ibis;

import FPF.team.Ibis.players.*;
import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.PlayerPerception;

public class IbisPlayer extends Thread {
	private int LOOP_INTERVAL = 100; //0.1s
	private PlayerCommander commander;
	private PlayerPerception selfPerc;


	public IbisPlayer(PlayerCommander player) {
		commander = player;
	}
	@Override
	public void run() {
		System. out .println(">> Executando...");
		long nextIteration = System.currentTimeMillis() + LOOP_INTERVAL;
		updatePerceptions();
		switch (selfPerc.getUniformNumber()) {
		case 1:
			new Goolkeeper(commander,nextIteration);
			break ;
		case 2:
			new Back(commander,nextIteration,1); // cima
			break ;
		case 3:
			new Back(commander,nextIteration, -1); // baixo
			break ;
		case 4:
			new DefensiveMidFielder(commander,nextIteration); // centro
			break ;
		case 5:
			new MidFielder(commander,nextIteration,1); // cima
			break ;
		case 6:
			new MidFielder(commander,nextIteration,-1); // baixo
			break ;
		case 7:
			new Farward(commander,nextIteration);
			break ;
		/*case 2:
			new Farward(commander,nextIteration);
			break ;
		case 3:
			new MidFielder(commander,nextIteration,1); // cima
			break ;
		case 4:
			new MidFielder(commander,nextIteration,-1); // baixo
			break ;*/
		default : break ;
		}

	}

	private void updatePerceptions() {
		PlayerPerception newSelf =
				commander.perceiveSelfBlocking();
				commander.perceiveMatchBlocking();
		if (newSelf != null ) this.selfPerc = newSelf;
	
	}

	


	
}