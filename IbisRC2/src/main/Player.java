package main;

import java.awt.Rectangle;
import java.util.ArrayList;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.Vector2D;


public class Player extends Thread{
	private PlayerCommander commander;
	private PlayerPerception selfPerc;
	private FieldPerception fieldPrc;
	private MatchPerception matchPerc;
	private static int LOOP_INTERVAL  = 100;
	
	public Player(PlayerCommander player) {
		commander = player;
	}
	
	@Override
	public void run() {
		long nextInteration = System.currentTimeMillis() + LOOP_INTERVAL;
		this.updatePerceptions();
		switch(selfPerc.getUniformNumber()) {
		case 1:
			acaoGoleiro(nextInteration);
			break;
		case 2:
			acaoZagueiro(nextInteration);
			break;
		case 3:
			acaoZagueiro(nextInteration);
			break;
		case 4:
			acaoMeioCampo(nextInteration);
			break;
		case 5:
			acaoMeioCampo(nextInteration);
			break;
		case 6:
			acaoMeioCampo(nextInteration);
			break;
		case 7:
			acaoMeioAtacante(nextInteration);
			break;
		}
	}
	private void acaoMeioAtacante(long nextInteration) {
		// TODO Auto-generated method stub
		
	}

	private void acaoMeioCampo(long nextInteration) {
		// TODO Auto-generated method stub
		
	}

	private void acaoZagueiro(long nextInteration) {
		// TODO Auto-generated method stub
		
	}

	private void updatePerceptions() {
		PlayerPerception newself = commander.perceiveSelfBlocking();
		FieldPerception newfield = commander.perceiveFieldBlocking();
		MatchPerception newMatch = commander.perceiveMatchBlocking();
		if(newself != null) this.selfPerc = newself;
		if(newfield != null) this.fieldPrc = newfield;
		if(newMatch != null) this.matchPerc = newMatch;
	}
	private void turnToPoint(Vector2D point) {
		commander.doTurnToDirectionBlocking(point.sub(selfPerc.getPosition()));
		
	}
	private void dash(Vector2D point) {
		if(selfPerc.getPosition().distanceTo(point) <=1) return;
		if(!isAlignToPoint(point,15)) turnToPoint(point);
		commander.doDashBlocking(70);
	}
	private void kickToPoint(Vector2D point, double intensity) {
		Vector2D newDirection = point.sub(selfPerc.getPosition());
		double angle = newDirection.angleFrom(selfPerc.getDirection());
		if(angle > 90 || angle < -90) {
			commander.doTurnToDirectionBlocking(newDirection);
			angle = 0;
		}
		commander.doKick(intensity, angle);
	}
	private boolean isAlignedToPoint(Vector2D point, double margin) {
		double angle = point.sub(selfPerc.getPosition()).angleFrom(selfPerc.getDirection());
		return angle < margin && angle > margin*(-1);
	}
	private boolean isAlignToPoint(Vector2D point, double margin) {
		double angle = point .sub(selfPerc.getPosition()).angleFrom(selfPerc.getDirection());
		return angle < margin && angle > margin*(-1);
	}
	private boolean isPointsAreClose(Vector2D reference,Vector2D point, double margin) {
		return reference.distanceTo(point) <= margin;
	}
	private PlayerPerception getClosestPlayerPoint(Vector2D point, EFieldSide side, double margin) {
		ArrayList<PlayerPerception> lp = this.fieldPrc.getTeamPlayers(side);
		PlayerPerception np = null;
		if(lp != null && !lp.isEmpty()) {
			double dist,temp;
			dist = lp.get(0).getPosition().distanceTo(point);
			if(isPointsAreClose(np.getPosition(),point,margin))
				return np;
			for(PlayerPerception p: lp) {
				if(p.getPosition() == null) break;
				if(isPointsAreClose(p.getPosition(),point, margin))
					return p;
				temp = p.getPosition().distanceTo(point);
				if(temp< dist) {
					dist = temp;
					np = p;
				}
			}
		}
		return np;
	}
	
	public void acaoGoleiro(long nextInterator) {
		double xInit = -48, yInit=0, ballX=0, ballY=0;
		EFieldSide side =selfPerc.getSide();
		Vector2D initPos = new Vector2D(xInit*side.value(),yInit*side.value());
		Vector2D ballPos;
		Rectangle area = side == EFieldSide.LEFT? new Rectangle(-52,-20,16,40): new Rectangle(36,-20,16,40);
		while(true) {
			updatePerceptions();
			ballPos = this.fieldPrc.getBall().getPosition();
			switch(matchPerc.getState()) {
				case BEFORE_KICK_OFF:
					commander.doMoveBlocking(xInit, yInit);
					break;
				case PLAY_ON:
					ballX = this.fieldPrc.getBall().getPosition().getX();
					ballY = this.fieldPrc.getBall().getPosition().getY();
					if(this.isPointsAreClose(this.selfPerc.getPosition(), ballPos, 1)) {
						this.kickToPoint(new Vector2D(0,0),100);
					}else if(area.contains(ballX, ballY)) {
						dash(ballPos);
					}else if(!isPointsAreClose(selfPerc.getPosition(), initPos,3)) {
						dash(initPos);
					}else {
						turnToPoint(ballPos);
					}
					break;
					default: break;	
			}
		}
	}



}
