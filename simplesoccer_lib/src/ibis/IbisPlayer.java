package ibis;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EPlayerState;
import simple_soccer_lib.utils.Vector2D;

public class IbisPlayer extends Thread {

	protected PlayerCommander commander;

	protected PlayerPerception selfPerc;
	protected FieldPerception fieldPerc;

	public IbisPlayer(PlayerCommander player) {
		commander = player;
	}

	private boolean closeToBall() {
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		Vector2D myPos = selfPerc.getPosition();

		return ballPos.distanceTo(myPos) < 2.0;
	}

	protected boolean isAlignedToBall() {
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		Vector2D myPos = selfPerc.getPosition();

		if (ballPos == null || myPos == null) {
			return false;
		}

		double angle = selfPerc.getDirection().angleFrom(ballPos.sub(myPos));
		// System.out.println("Vetores: " + ballPos.sub(myPos) + " " +
		// selfPerc.getDirection());
		// System.out.println(" => Angulo agente-bola: " + angle);

		return angle < 15.0d && angle > -15.0d;
	}

	double angleToBall() {
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		Vector2D myPos = selfPerc.getPosition();

		return selfPerc.getDirection().angleFrom(ballPos.sub(myPos));
	}

	protected void updatePerceptions() {
		PlayerPerception newSelf = commander.perceiveSelf();
		FieldPerception newField = commander.perceiveField();

		if (newSelf != null) {
			this.selfPerc = newSelf;
		}
		if (newField != null) {
			this.fieldPerc = newField;
		}
	}

	protected void turnToBall() {
		System.out.println("TURN");
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		Vector2D myPos = selfPerc.getPosition();
		System.out.println(" => Angulo agente-bola: " + angleToBall() + " (desalinhado)");
		System.out.println(" => Posicoes: ball = " + ballPos + ", player = " + myPos);

		Vector2D newDirection = ballPos.sub(myPos);
		System.out.println(" => Nova direcao: " + newDirection);

		commander.doTurnToPoint(ballPos);
		// DirectionBlocking(newDirection);
	}
	

	protected void runToBall() {
		System.out.println("RUN");
		commander.doDashBlocking(100.0d);
	}

	protected boolean teamHasBall() {
		for (PlayerPerception player : fieldPerc.getAllPlayers()) {
			if (player.getTeam().equals(commander.getTeamName())
					&& player.getState().compareTo(EPlayerState.HAS_BALL) == 0) {
				return true;
			}
		}
		return false;
	}

	protected boolean ballInDefenseField() {
		if(fieldPerc.getBall().getPosition().getX() < 50)
			return true;
		else
			return false;
	}
	/*
	protected boolean amIFree() {
		
	}
	*/
	

}
