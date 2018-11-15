package FPF.team.Ibis.players;


import java.awt.Rectangle;
import java.util.ArrayList;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.Vector2D;
import utils.PlayerUtils;


public class MidFielder extends Thread {
	private PlayerCommander commander;
	private PlayerPerception selfPerc;
	private FieldPerception fieldPerc;
	private MatchPerception matchPerc;
	private Rectangle defenseArea;
	private Rectangle atackArea;
	private Vector2D goalPos;
	private EFieldSide side;
	private Vector2D initPos;
	private double xInit, yInit;
	private int pos;
	
	
	public MidFielder(PlayerCommander player, long nextIteration,int pos) {
		commander = player;
		this.updatePerceptions();
		side = selfPerc.getSide();
		this.xInit=-20;
		this.yInit=20*pos;
		this.goalPos = new Vector2D(50*side.value(), 0);
		this.initPos =	new Vector2D(xInit*side.value(), yInit*side.value());
		this.pos = pos;
		
		if(pos  == 1) {
			this.defenseArea = this.side == EFieldSide.LEFT ?
					new Rectangle(-27, 0, 27, 34):
						new Rectangle(0, -34, 27, 34);
			this.atackArea = this.side == EFieldSide.LEFT ?
					new Rectangle(0, 0, 53, 34):
						new Rectangle(-53, -34, 53, 34);
		}else {
			this.defenseArea = this.side == EFieldSide.LEFT ?
					new Rectangle(-27, -34, 27, 34):
					new Rectangle(0, 0, 27, 34);
			this.atackArea = this.side == EFieldSide.LEFT ?
					new Rectangle(0, -34, 53, 34):
						new Rectangle(-53, 0, 53, 34);
		}
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
		Vector2D ballPos;
		

		while ( true ) {
			this.updatePerceptions();
			ballPos = fieldPerc.getBall().getPosition();
			switch (matchPerc.getState()) {
			case BEFORE_KICK_OFF :
				commander.doMoveBlocking(this.xInit, this.yInit);
				break ;
			case PLAY_ON :
				//this.defend(initPos);
				if(this.teamHasBall()){ // meu time tem a bola?
					this.atack(this.initPos);
				}else{
					this.defend(initPos);
				}
				break ;
			case GOAL_KICK_RIGHT :
				dash(initPos);
				break ;
			case GOAL_KICK_LEFT :
				dash(initPos);
				break ;
			case AFTER_GOAL_RIGHT :
				this.dash(initPos);
				break ;
			case AFTER_GOAL_LEFT :
				dash(initPos);
				
			case KICK_IN_LEFT :
				if(selfPerc.getSide().equals(EFieldSide.LEFT)) {
					if(pos == -1 && fieldPerc.getBall().getPosition().getX() > -36 && fieldPerc.getBall().getPosition().getY() <= -1){
						if(ballPos.distanceTo(selfPerc.getPosition())<=1) {
							kickToPoint(fieldPerc.getTeamPlayer(side, 4).getPosition() ,250);
						}else{
							//System.out.println("Aqui");
							//commander.doMoveBlocking(fieldPerc.getBall().getPosition().getX(),fieldPerc.getBall().getPosition().getY());
							this.dash(fieldPerc.getBall().getPosition());
						}			
						
					}else if(pos != -1 && fieldPerc.getBall().getPosition().getX() > -36 && fieldPerc.getBall().getPosition().getY() >= 1){
						if(ballPos.distanceTo(selfPerc.getPosition())<=1) {
							kickToPoint(fieldPerc.getTeamPlayer(side, 4).getPosition() ,250);
						}else {
							//System.out.println("Aqui");
							//commander.doMoveBlocking(fieldPerc.getBall().getPosition().getX(),fieldPerc.getBall().getPosition().getY());
							this.dash(fieldPerc.getBall().getPosition());
						}
						
					}else {
						this.dash(this.initPos);
					}
				}else {
					this.dash(this.initPos);
				}
				break ;
			case KICK_IN_RIGHT :
				if(selfPerc.getSide().equals(EFieldSide.RIGHT)) {
					if(pos != -1 && fieldPerc.getBall().getPosition().getX() < 36 && fieldPerc.getBall().getPosition().getY() <= -1){
						if(ballPos.distanceTo(selfPerc.getPosition())<=1) {
							kickToPoint(fieldPerc.getTeamPlayer(side, 4).getPosition(),250);
						}else {
							commander.doMoveBlocking(fieldPerc.getBall().getPosition().getX(),fieldPerc.getBall().getPosition().getY());
							dash(fieldPerc.getBall().getPosition());
						}			
						
					}else if(pos == -1 && fieldPerc.getBall().getPosition().getX() < 36 && fieldPerc.getBall().getPosition().getY() >= 1){
						if(ballPos.distanceTo(selfPerc.getPosition())<=1) {
							kickToPoint(fieldPerc.getTeamPlayer(side, 4).getPosition(),250);
						}else {
							commander.doMoveBlocking(fieldPerc.getBall().getPosition().getX(),fieldPerc.getBall().getPosition().getY());
							dash(fieldPerc.getBall().getPosition());
						}						
					}else {
						this.dash(this.initPos);
					}
				}else {
					this.dash(initPos);
				}
				/* Todos os estados da partida */
			default :
				break ;
			}
		}
	}
	private void defend(Vector2D originalPosition) {
		if(this.defenseArea.contains(this.selfPerc.getPosition().getX(),this.selfPerc.getPosition().getY())) {
			if(this.iAmTheLastPlayer()) {
				if(this.fieldPerc.getBall().getPosition().distanceTo(selfPerc.getPosition())<=1) {
					//System.out.println("Chuta para longe "+selfPerc.getUniformNumber());
					kickToPoint(goalPos,150);
				}else {
					this.dash(this.fieldPerc.getBall().getPosition());
				}
			}
			else
				this.dash(originalPosition);
		}else {
			this.dash(originalPosition);
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
	
	private void atack(Vector2D originalPosition) {
		if (PlayerUtils.isPointsAreClose(selfPerc.getPosition(),
				this.fieldPerc.getBall().getPosition(), 1)){
			if (PlayerUtils.isPointsAreClose(this.fieldPerc.getBall().getPosition(), goalPos, 30)){
				// chuta para o gol
				kickToPoint(goalPos, 100);
			} else {
				// conduz para o gol
				kickToPoint(goalPos, 20);
			}
		}else  {
			if(this.atackArea.contains(this.selfPerc.getPosition().getX(),this.selfPerc.getPosition().getY())
					&& this.iAmTheLastPlayer()) {
				this.dash(this.fieldPerc.getBall().getPosition());
			}
			else {
				this.dash(new Vector2D(this.atackArea.getCenterX(),this.atackArea.getCenterY()));
			}
			
		}
	}
	private boolean teamHasBall() {
		double smallerDistanceMyTeam = 2000;
		double smallerDistanceOtherTeam = 2000;
		if(this.selfPerc.getSide().equals(EFieldSide.LEFT)) {			
			for(PlayerPerception p: this.fieldPerc.getTeamPlayers(EFieldSide.LEFT)) {
				if(p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) < smallerDistanceMyTeam)
					smallerDistanceMyTeam = p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition());
			}
			for(PlayerPerception p: this.fieldPerc.getTeamPlayers(EFieldSide.RIGHT)) {
				if(p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) < smallerDistanceMyTeam)
					smallerDistanceOtherTeam = p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition());
				//System.out.println(smallerDistanceOutherTeam-smallerDistanceMyTeam);
			}return smallerDistanceMyTeam <= smallerDistanceOtherTeam || smallerDistanceOtherTeam-smallerDistanceMyTeam >= 10;
		
		}else{
			for(PlayerPerception p: this.fieldPerc.getTeamPlayers(EFieldSide.LEFT)) {
				if(p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) < smallerDistanceMyTeam)
					smallerDistanceOtherTeam = p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition());
			}
			for(PlayerPerception p: this.fieldPerc.getTeamPlayers(EFieldSide.RIGHT)) {
				if(p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) < smallerDistanceMyTeam)
					smallerDistanceMyTeam = p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition());
				//System.out.println(smallerDistanceOutherTeam-smallerDistanceMyTeam);
			}return smallerDistanceMyTeam <= smallerDistanceOtherTeam || smallerDistanceOtherTeam-smallerDistanceMyTeam >= 10;
		}
	}

}
