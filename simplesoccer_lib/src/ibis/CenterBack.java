package ibis;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.utils.EPlayerState;

public class CenterBack extends IbisPlayer{

	public CenterBack(PlayerCommander player) {
		super(player);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void run() {
		System.out.println(">> 1. Waiting initial perceptions...");
		selfPerc  = commander.perceiveSelfBlocking();
		fieldPerc = commander.perceiveFieldBlocking();
		
		System.out.println(">> 2. Moving to initial position...");
		commander.doMoveBlocking(-25.0d, 0.0d);
		
		selfPerc  = commander.perceiveSelfBlocking();
		fieldPerc = commander.perceiveFieldBlocking();
		
		System.out.println(">> 3. Now starting...");
		while (commander.isActive()) {
			
			if (teamHasBall()) {
				if(this.selfPerc.getState().compareTo(EPlayerState.HAS_BALL) == 0) {
					int shirt = isSomeoneFree();
					if(shirt != -1){
						shortPass(shirt,2.0);
					}else{
						//avança ao meio de campo esperando alguém ficar livre
					}
				}
				else {
					//se posiciona para receber a bola
				}
			} else {
				if(ballInDefenseField()) {
					if(isAlignedToBall()) {
						runToBall();
					}
					else {
						turnToBall();
					}
				}
			}

			updatePerceptions(); //non-blocking
		}
		
		System.out.println(">> 4. Terminated!");
	}
	
	

}
