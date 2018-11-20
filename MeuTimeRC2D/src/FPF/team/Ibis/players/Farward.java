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


public class Farward extends Thread {
	private PlayerCommander commander;
	private PlayerPerception selfPerc;
	private FieldPerception fieldPerc;
	private MatchPerception matchPerc;
	private double xInit=-9;
	private double yInit=0;
	private EFieldSide side;
	private Vector2D initPos;
	private Vector2D goalPos;
	private Vector2D ballPos;
	private Rectangle areaDef;
	private Rectangle areaAtack;
	private Vector2D escanPos;

	public Farward(PlayerCommander player,long nextIteration) {
		commander = player;
		this.updatePerceptions();
		this.xInit=-9;
		this.yInit=0;
		this.side = selfPerc.getSide();
		this.initPos = new Vector2D(xInit*side.value(), yInit);
		this.goalPos = new Vector2D(50*side.value(), 0);
		//this.areaDef = new Rectangle(0, -16, 16, 32);
		this.areaDef = this.side == EFieldSide.LEFT ?
				new Rectangle(-5, -16, 21, 32):
					new Rectangle(-16, -16, 21, 32);
				
		this.areaAtack = this.side == EFieldSide.LEFT ?
				new Rectangle(0, -16, 53, 16):
					new Rectangle(-53, -16, 53, 16);
		this.escanPos = new Vector2D(35*this.side.value(), 0*this.side.value());
		this.action(nextIteration);
		
	}
	private void action( long nextIteration) {
		this.updatePerceptions();
		while ( true ) {
			updatePerceptions();
			ballPos = fieldPerc.getBall().getPosition();
			switch (matchPerc.getState()) {
			case PLAY_ON :
				if(this.selfPerc.getPosition().distanceTo(ballPos) <=6) {
					this.atack(initPos);
				}
				else if(areaAtack.contains(ballPos.getX(), ballPos.getY())) {
					if(this.selfPerc.getPosition().getX() <= (31 * this.side.value()) && ballPos.getX() <= (31 * this.side.value())) {
						this.dash(ballPos);
					}
					else if(this.selfPerc.getPosition().distanceTo(ballPos) <= 16)  {
						this.dash(ballPos);
					}
					else { 
						this.dash(new Vector2D(15*side.value(),0));
					}
				}
				else if(this.areaDef.contains(ballPos.getX(), ballPos.getY())) {
					if(!this.teamHasBall() || this.selfPerc.getPosition().distanceTo(ballPos) <=9) {
						this.dash(ballPos);
					}
				}
				else {
					if(teamHasBall()) {
						this.dash(new Vector2D(15 *side.value(), 0));
					}else
						this.dash(new Vector2D(0,0));
				}
			break;
			case KICK_OFF_LEFT :
				if(selfPerc.getSide().equals(EFieldSide.LEFT)) {
					if(ballPos.distanceTo(selfPerc.getPosition())<=1){
						if (!(this.isAlignToPoint(fieldPerc.getTeamPlayer(side, 6).getPosition(), 1))) {
							this.turnToPoint(fieldPerc.getTeamPlayer(side, 6).getPosition());
						}else {
							kickToPoint(fieldPerc.getTeamPlayer(side, 6).getPosition() ,75);
						}
					}
					dash(this.ballPos);
				}		
				//commander.doMoveBlocking(xInit, yInit);
				break ;
			case KICK_OFF_RIGHT :
				if(selfPerc.getSide().equals(EFieldSide.RIGHT)) {
					if(ballPos.distanceTo(selfPerc.getPosition())<=1){
						if (!(this.isAlignToPoint(fieldPerc.getTeamPlayer(side, 6).getPosition(), 1))) {
							this.turnToPoint(fieldPerc.getTeamPlayer(side, 6).getPosition());
						}else {
							kickToPoint(fieldPerc.getTeamPlayer(side, 6).getPosition() ,75);
						}
					}
					dash(this.ballPos);
				}
				break ;
			case BEFORE_KICK_OFF :
				commander.doMoveBlocking(xInit, yInit);
				if(selfPerc.getSide().equals(EFieldSide.RIGHT)) {
					if(ballPos.distanceTo(selfPerc.getPosition())<=1){
						if (!(this.isAlignToPoint(fieldPerc.getTeamPlayer(side, 6).getPosition(), 1))) {
							this.turnToPoint(fieldPerc.getTeamPlayer(side, 6).getPosition());
						}else {
							kickToPoint(fieldPerc.getTeamPlayer(side, 6).getPosition() ,80);
						}
					}
					dash(this.ballPos);
				}
				break ;
			case KICK_IN_RIGHT :
				if(this.side.value() == 1) {
					this.dash(new Vector2D(this.areaAtack.getCenterX(),this.areaAtack.getCenterY()));
				}
				break;
			case KICK_IN_LEFT :	
				if(this.side.value() == -1) {
					this.dash(new Vector2D(this.areaAtack.getCenterX(),this.areaAtack.getCenterY()));
				}
				break;
			case GOAL_KICK_RIGHT :
				if(this.side.value() == 1) {
					this.dash(new Vector2D(this.areaAtack.getCenterX(),this.areaAtack.getCenterY()));
				}else
					this.dash(initPos);
				break;
			case GOAL_KICK_LEFT :
				if(this.side.value() == 1) {
					this.dash(new Vector2D(this.areaAtack.getCenterX(),this.areaAtack.getCenterY()));
				}else
					this.dash(initPos);
			
				break;
			case CORNER_KICK_LEFT :
				if(selfPerc.getSide().equals(EFieldSide.LEFT))
					dash(escanPos);
				break ;
			case CORNER_KICK_RIGHT :
				if(selfPerc.getSide().equals(EFieldSide.RIGHT))
					dash(escanPos);
				break ;
			case FREE_KICK_LEFT :
				if(selfPerc.getSide().equals(EFieldSide.LEFT)) {
					if(this.souOMaisPerto()) {
						if(selfPerc.getPosition().distanceTo(ballPos)<=1) {
							this.kickToPoint(goalPos, 500);
						}else {
							dash(ballPos);
						}
						
					}
				}
					
				break ;
			case FREE_KICK_RIGHT :
				if(selfPerc.getSide().equals(EFieldSide.RIGHT)) {
					if(this.souOMaisPerto()) {
						if(selfPerc.getPosition().distanceTo(ballPos)<=1) {
							this.kickToPoint(goalPos, 500);
						}else {
							dash(ballPos);
						}
						
					}
				}
				break ;
			default :
				break ;
			}
			
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
	}

	private void turnToPoint(Vector2D point){
		Vector2D newDirection = point.sub(selfPerc.getPosition());
		commander.doTurnToDirectionBlocking(newDirection);
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

	private void defend(Vector2D originalPosition) {
		if(this.areaDef.contains(this.selfPerc.getPosition().getX(),this.selfPerc.getPosition().getY())) {
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
	private void atack(Vector2D originalPosition) {
		if (PlayerUtils.isPointsAreClose(selfPerc.getPosition(),
				this.fieldPerc.getBall().getPosition(), 1)){
			if (PlayerUtils.isPointsAreClose(this.fieldPerc.getBall().getPosition(), goalPos, 28)){
				// chuta para o gol
				kickToPoint(goalPos, 100);
			} else {
				// conduz para o gol
				kickToPoint(goalPos, 20);
			}
		}else  {
			if(this.areaAtack.contains(this.selfPerc.getPosition().getX(),this.selfPerc.getPosition().getY())
					&& this.iAmTheLastPlayer()) {
				this.dash(this.fieldPerc.getBall().getPosition());
			}
			else {
				this.dash(new Vector2D(this.areaAtack.getCenterX(),this.areaAtack.getCenterY()));
			}
			
		}
	}
	private void dash(Vector2D point){
		if (selfPerc.getPosition().distanceTo(point) <= 1) return ;
		if (!isAlignToPoint(point, 10)) turnToPoint(point);
		if(selfPerc.getPosition().distanceTo(point) <= 1.5) {
			commander.doDashBlocking(55);
		}else if(selfPerc.getPosition().distanceTo(point) < 2) {
			commander.doDashBlocking(65);			
		}else if(selfPerc.getPosition().distanceTo(point) < 3) {
			commander.doDashBlocking(75);			
		}else if(selfPerc.getPosition().distanceTo(point) < 4){
			commander.doDashBlocking(85);					
		}else if(selfPerc.getPosition().distanceTo(point) < 5){
			commander.doDashBlocking(95);					
		}else{
			commander.doDashBlocking(100);
		}
		
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

	/*private boolean isPointsAreClose(Vector2D reference,
			Vector2D point, double margin){
		return reference.distanceTo(point) <= margin;
	}*/

	/*private PlayerPerception getClosestPlayerPoint(Vector2D point, EFieldSide side, double margin){
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
	}*/


	
	private boolean teamHasBall() {
		double smallerDistanceMyTeam = 2000;
		double smallerDistanceOutherTeam = 2000;
		if(this.selfPerc.getSide().equals(EFieldSide.LEFT)) {			
			for(PlayerPerception p: this.fieldPerc.getTeamPlayers(EFieldSide.LEFT)) {
				if(p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) < smallerDistanceMyTeam)
					smallerDistanceMyTeam = p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition());
			}
			for(PlayerPerception p: this.fieldPerc.getTeamPlayers(EFieldSide.RIGHT)) {
				if(p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) < smallerDistanceMyTeam)
					smallerDistanceOutherTeam = p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition());
				//System.out.println(smallerDistanceOutherTeam-smallerDistanceMyTeam);
			}return smallerDistanceMyTeam <= smallerDistanceOutherTeam || smallerDistanceOutherTeam-smallerDistanceMyTeam >= 10;
		
		}else{
			for(PlayerPerception p: this.fieldPerc.getTeamPlayers(EFieldSide.LEFT)) {
				if(p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) < smallerDistanceMyTeam)
					smallerDistanceOutherTeam = p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition());
			}
			for(PlayerPerception p: this.fieldPerc.getTeamPlayers(EFieldSide.RIGHT)) {
				if(p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) < smallerDistanceMyTeam)
					smallerDistanceMyTeam = p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition());
				//System.out.println(smallerDistanceOutherTeam-smallerDistanceMyTeam);
			}return smallerDistanceMyTeam <= smallerDistanceOutherTeam || smallerDistanceOutherTeam-smallerDistanceMyTeam >= 10;
		}
	}
	
	private boolean souOMaisPerto() {
		return selfPerc.getPosition().distanceTo(ballPos) < fieldPerc.getTeamPlayer(side, 5).getPosition().distanceTo(ballPos) && selfPerc.getPosition().distanceTo(ballPos) < fieldPerc.getTeamPlayer(side, 6).getPosition().distanceTo(ballPos);
	}



}
