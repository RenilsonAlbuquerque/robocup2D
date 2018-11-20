package FPF.team.Ibis;

import FPF.team.Ibis.players.*;
import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EPlayerState;
import utils.PlayerUtils;


public class IbisPlayer extends Thread {
	private int LOOP_INTERVAL = 100; //0.1s
	private PlayerCommander commander;
	private PlayerPerception selfPerc;
	private FieldPerception fieldPerc;
	private MatchPerception matchPerc;

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
		FieldPerception newField =
				commander.perceiveFieldBlocking();
		MatchPerception newMatch =
				commander.perceiveMatchBlocking();
		if (newSelf != null ) this .selfPerc = newSelf;
		if (newField != null ) this .fieldPerc = newField;
		if (newMatch != null ) this .matchPerc = newMatch;
		this.updateState();
	}

	


	
	/*------------*/
	private boolean updateState() {
		if(PlayerUtils.isPointsAreClose(this.selfPerc.getPosition(),this.fieldPerc.getBall().getPosition(),2)
				) {
			this.selfPerc.setState(EPlayerState.HAS_BALL);
			return true;
		}
		if(!PlayerUtils.getClosestTeammatePoint(this.fieldPerc,this.selfPerc.getPosition(), this.selfPerc.getSide(), 3).getSide().equals(this.selfPerc.getSide()) ) {
			this.selfPerc.setState(EPlayerState.CATCH);
			return true;
		}
		return true;
		
	}
}