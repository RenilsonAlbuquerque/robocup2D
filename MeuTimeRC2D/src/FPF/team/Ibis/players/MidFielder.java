package FPF.team.Ibis.players;


import java.awt.Rectangle;
import java.util.ArrayList;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.EPlayerState;
import simple_soccer_lib.utils.Vector2D;
import utils.PlayerUtils;


public class MidFielder extends Thread {
	private int LOOP_INTERVAL = 100; //0.1s
	private PlayerCommander commander;
	private PlayerPerception selfPerc;
	private FieldPerception fieldPerc;
	private MatchPerception matchPerc;
	private ArrayList<Rectangle> myAreas;

	public MidFielder(PlayerCommander player, long nextIteration,int pos) {
		commander = player;
		this.action(nextIteration, pos);
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
	}

	private void turnToPoint(Vector2D point){
		Vector2D newDirection = point.sub(selfPerc.getPosition());
		commander.doTurnToDirectionBlocking(newDirection);
	}

	private void dash(Vector2D point){
		if (selfPerc.getPosition().distanceTo(point) <= 1) return ;
		if (!isAlignToPoint(point, 15)) turnToPoint(point);
		commander.doDashBlocking(70);
	}

	private void kickToPoint(Vector2D point, double intensity){
		Vector2D newDirection = point.sub(selfPerc.getPosition());
		double angle = newDirection.angleFrom(selfPerc.getDirection());
		if (angle > 90 || angle < -90){
			commander.doTurnToDirectionBlocking(newDirection);
			angle = 0;
		}
		commander.doKickBlocking(intensity, angle);
	}

	private boolean isAlignToPoint(Vector2D point, double margin){
		double angle = point.sub(selfPerc.getPosition()).angleFrom(selfPerc.getDirection());
		return angle < margin && angle > margin*(-1);
	}

	private boolean isPointsAreClose(Vector2D reference,
			Vector2D point, double margin){
		return reference.distanceTo(point) <= margin;
	}

	private PlayerPerception getClosestPlayerPoint(Vector2D point, EFieldSide side, double margin){
		ArrayList<PlayerPerception> lp = fieldPerc.getTeamPlayers(side);
		PlayerPerception np = null ;
		if (lp != null && !lp.isEmpty()){
			double dist,temp;
			dist = lp.get(0).getPosition().distanceTo(point);
			np = lp.get(0);
			if (isPointsAreClose(np.getPosition(), point, margin))
				return np;
			for (PlayerPerception p : lp) {
				if (p.getPosition() == null )
					break ;
				if (isPointsAreClose(p.getPosition(), point, margin))
					return p;
				temp = p.getPosition().distanceTo(point);
				if (temp < dist){
					dist = temp;
					np = p;
				}
			}
		}
		return np;
	}

	private void action( long nextIteration, int pos) {
		this.updatePerceptions();
		double xInit=-22, yInit=0;
		if(pos!=0) {
			yInit=20*pos;
			xInit=-17;
		}			
		EFieldSide side = selfPerc.getSide();
		Vector2D initPos =	new Vector2D(xInit*side.value(), yInit*side.value());
		Vector2D ballPos, vTemp;
		PlayerPerception pTemp;
		while ( true ) {
			updatePerceptions();
			ballPos = fieldPerc.getBall().getPosition();
			switch (matchPerc.getState()) {
			case BEFORE_KICK_OFF :
				commander.doMoveBlocking(xInit, yInit);
				break ;
			case PLAY_ON :
				if(this.teamHasBall()){ // meu time tem a bola?
					if(this.selfPerc.getState() == EPlayerState.HAS_BALL){ //eu estou com a bola?
						//toca pra alguém desmarcado
						kickToPoint(PlayerUtils.getClosestTeammatePoint(this.fieldPerc,this.selfPerc.getPosition(), this.selfPerc.getSide(), 5).getPosition(), 30);
					}else{
						//this.freeFromMark(initPos);
						this.atack(initPos);
					}
				}else{
					this.defend(initPos);
				}
				break ;
			case GOAL_KICK_RIGHT :
				commander.doMoveBlocking(xInit, yInit);
				break ;
			case GOAL_KICK_LEFT :
				commander.doMoveBlocking(xInit, yInit);
				break ;
				/* Todos os estados da partida */
			default :
				break ;
			}
		}
	}
	private void defend(Vector2D originalPosition) {
		if(this.selfPerc.getPosition().distanceTo(originalPosition) > 20) {
			this.dash(originalPosition);
		}else {
			//Estou no raio de ação da jogada?
			if(PlayerUtils.isPointsAreClose(selfPerc.getPosition(),this.fieldPerc.getBall().getPosition(), 30)) {
				//Sou o marcador mais próximo?
				if(iAmTheLastPlayer()) {
					//tenta roubar a bola
					this.dash(this.fieldPerc.getBall().getPosition());
				}else{
					//faz a cobertura
					this.dash(originalPosition);
				}
			}else{ //procuro alguém próximo pra marcar
				Vector2D closestPlayer = PlayerUtils.searchNearbyEnemy(fieldPerc, this.selfPerc.getSide(), this.selfPerc.getPosition(), 3).getPosition();
				if(this.selfPerc.getPosition().distanceTo(closestPlayer) <= 2)
					this.dash(PlayerUtils. searchNearbyEnemy(fieldPerc, this.selfPerc.getSide(), this.selfPerc.getPosition(), 5).getPosition());
				else
					this.dash(originalPosition);
			}
		}
		
	}
	private boolean iAmTheLastPlayer() {
		double distance = this.selfPerc.getPosition().distanceTo(this.fieldPerc.getBall().getPosition());
		for(PlayerPerception p: this.fieldPerc.getTeamPlayers(this.selfPerc.getSide())) {
			if(p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) < distance ) {
				return false;
			}
			
		}
		return true;
	}
	private void freeFromMark(Vector2D playerOriginalPosition){
		PlayerPerception marker = PlayerUtils.searchNearbyEnemy(this.fieldPerc, this.selfPerc.getSide(), this.selfPerc.getPosition(), 4);
		if(marker.getPosition().distanceTo(this.selfPerc.getPosition()) <= 10){
			this.dash(playerOriginalPosition);
		}
	}
	private void atack(Vector2D originalPosition) {
		
			if(this.selfPerc.getSide() == EFieldSide.LEFT) {
				if(this.fieldPerc.getBall().getPosition().getX() > 0 ) {	
					System.out.println(this.selfPerc.getUniformNumber() + " Atacking");
					this.dash(new Vector2D(originalPosition.getX() + 60, originalPosition.getY()));
				}else {
					this.dash(originalPosition);
				}
			}else {
				if(this.fieldPerc.getBall().getPosition().getX() < 0 ) {
					System.out.println(this.selfPerc.getUniformNumber() + " Atacking");
					this.dash(new Vector2D(originalPosition.getX() - 30, originalPosition.getY()));
				}
				else {
					this.dash(originalPosition);
				}
				
			}
	}
	private boolean teamHasBall() {
		for(PlayerPerception p: this.fieldPerc.getTeamPlayers(this.selfPerc.getSide())) {
			if(p.getState().equals(EPlayerState.HAS_BALL))
				return true;
		}
		return false;
	}


}
