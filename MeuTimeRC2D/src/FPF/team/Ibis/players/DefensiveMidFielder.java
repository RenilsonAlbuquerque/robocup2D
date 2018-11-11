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


public class DefensiveMidFielder extends Thread {
	private int LOOP_INTERVAL = 100; //0.1s
	private PlayerCommander commander;
	private PlayerPerception selfPerc;
	private FieldPerception fieldPerc;
	private MatchPerception matchPerc;
	private ArrayList<Rectangle> myAreas;

	public DefensiveMidFielder(PlayerCommander player, long nextIteration) {
		commander = player;
		this.action(nextIteration);
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
		this.iHaveTheBall();
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

	

	private void action( long nextIteration) {
		this.updatePerceptions();
		double xInit=-22, yInit=0;
				
		EFieldSide side = selfPerc.getSide();
		Vector2D ballPos;
		Vector2D initPos =	new Vector2D(xInit*side.value(), yInit*side.value());
		Rectangle defenseArea = side == EFieldSide.LEFT ?
				new Rectangle(-36, -16, 26, 34):
					new Rectangle(16, -16, 26, 34);
		Rectangle atackArea = side == EFieldSide.LEFT ?
				new Rectangle(-16, -16, 26, 34):
					new Rectangle(-16, -16, 26, 34);
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
						this.atack(initPos,atackArea);
					}
				}else{
					this.defend(initPos,defenseArea,side);
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
	private void defend(Vector2D originalPosition, Rectangle area,EFieldSide side) {
		if(area.contains(fieldPerc.getBall().getPosition().getX(), fieldPerc.getBall().getPosition().getY())) {
			if(this.iAmTheLastPlayer()) {
				this.dash(fieldPerc.getBall().getPosition());
			}else {
				if(side.value() == 1) {
					this.dash(new Vector2D(-36,fieldPerc.getBall().getPosition().getY()));
				}else
					this.dash(new Vector2D(36,fieldPerc.getBall().getPosition().getY()));
			}
		}else {
			this.dash(originalPosition);
		}
		
	}
	/*
	private void freeFromMark(Vector2D playerOriginalPosition){
		PlayerPerception marker = PlayerUtils.searchNearbyEnemy(this.fieldPerc, this.selfPerc.getSide(), this.selfPerc.getPosition(), 4);
		if(marker.getPosition().distanceTo(this.selfPerc.getPosition()) <= 10){
			this.dash(playerOriginalPosition);
		}
	}
	*/
	private void atack(Vector2D originalPosition,Rectangle atackArea) {
		
			if(this.selfPerc.getSide() == EFieldSide.LEFT) {
				if(this.fieldPerc.getBall().getPosition().getX() > 0) {
					System.out.println("Left DMF atacking");
					this.dash(new Vector2D(10,0));
				}else
					this.dash(originalPosition);
			}else {
				if(this.fieldPerc.getBall().getPosition().getX() < 0) {
					System.out.println("Right DMF atacking");
					this.dash(new Vector2D(-10,0));
				}else
					this.dash(originalPosition);	
			}
	}
	private boolean teamHasBall() {
		for(PlayerPerception p: this.fieldPerc.getTeamPlayers(this.selfPerc.getSide())) {
			if(p.getState().equals(EPlayerState.HAS_BALL))
				return true;
		}
		return false;
	}
	private PlayerPerception playerWithTheBall() {
		PlayerPerception result = null;
		double distance = 1000;
		for(PlayerPerception p: this.fieldPerc.getTeamPlayers(this.selfPerc.getSide())) {
			if(p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) < distance ) {
				distance = p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition());
				result = p;
			}	
		}
		return result;
	}
	private boolean iAmTheLastPlayer() {
		double distance = this.selfPerc.getPosition().distanceTo(this.fieldPerc.getBall().getPosition());
		for(PlayerPerception p: this.fieldPerc.getTeamPlayers((this.selfPerc.getSide() == EFieldSide.LEFT) ? EFieldSide.RIGHT : EFieldSide.RIGHT )) {
			if(p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) < distance ) {
				return false;
			}
		}
		return true;
	}
	private boolean iHaveTheBall() {
		return this.selfPerc.getPosition().distanceTo(fieldPerc.getBall().getPosition()) < 2;
		
	}


}
/*


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
*/