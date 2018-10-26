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
					
				}
				else {
					
				}
				/*
				if (closeToBall()) {
					commander.doKick(50.0d, 0.0d);
				} else {
					runToBall();
				}
				*/
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
