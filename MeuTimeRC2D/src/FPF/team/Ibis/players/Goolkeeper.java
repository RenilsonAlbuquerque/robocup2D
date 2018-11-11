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


public class Goolkeeper {
	private int LOOP_INTERVAL = 100; //0.1s
	private PlayerCommander commander;
	private PlayerPerception selfPerc;
	private FieldPerception fieldPerc;
	private MatchPerception matchPerc;
	private ArrayList<Rectangle> myAreas;

	public Goolkeeper(PlayerCommander player,long nextIteration) {
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
		this.haveITheBall();
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

	public void action( long nextIteration) {
		this.updatePerceptions();
		double xInit=-50, yInit=0;
		EFieldSide side = selfPerc.getSide();
		Vector2D initPos =
				new Vector2D(xInit*side.value(), yInit*side.value());
		
		Vector2D ballPos;
		Rectangle area = side == EFieldSide.LEFT ?
				new Rectangle(-52, -20, 16, 40):
					new Rectangle(36, -20, 16, 40);
				
				while ( true ) {
					updatePerceptions();
					ballPos = fieldPerc.getBall().getPosition();
					switch (matchPerc.getState()) {
					case BEFORE_KICK_OFF :
						// posiciona
						commander.doMoveBlocking(xInit, yInit);
						break ;
					case PLAY_ON :
						
						
						if(this.selfPerc.getPosition().distanceTo(ballPos) <= 2) {
							this.turnToPoint(ballPos);
							//this.commander.doCatch(0);
							this.kickToPoint(new Vector2D(0,0), 500);
							
						}
						
						//if(area.contains(ballPos.getX(),ballPos.getY())) {
						
						if(this.selfPerc.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) <= 5 ) {
							//this.turnToPoint(ballPos);
							this.commander.doMove(ballPos.getX(), ballPos.getY());
							/*
							if(ballPos.getY() < 0) {
								this.dash(new Vector2D(initPos.getX(), (ballPos.getY() < -7 )? -5f:  ballPos.getY()  ));
							}else {
								this.dash(new Vector2D(initPos.getX(), (ballPos.getY() < 7 )?   5f: ballPos.getY()  ));
								
							}
							*/
						
						
						}else {
							
							double y = 0;
							if(ballPos.getY() < 0 ) {
								y = (-7 *  ((100 * ballPos.getY())/(-34)) / 100);	
							}else {
								y = (7 *  ((100 * ballPos.getY())/(34)) / 100);
							}
							this.dash(new Vector2D(initPos.getX(),y));
						
							
						
						
						
						}
						
						
						break ;
					case GOAL_KICK_RIGHT :
						if(this.selfPerc.getSide() == EFieldSide.RIGHT) {
						
							this.dash(ballPos);
							this.kickToPoint(
							PlayerUtils.getClosestTeammatePoint(this.fieldPerc,this.selfPerc.getPosition(), this.selfPerc.getSide(), -3).getPosition(), -500);
						}
						break;
					case GOAL_KICK_LEFT :
						
						if(this.selfPerc.getSide() == EFieldSide.LEFT) {
							
							this.dash(ballPos);
							this.kickToPoint(
									PlayerUtils.getClosestTeammatePoint(this.fieldPerc,this.selfPerc.getPosition(), this.selfPerc.getSide(), 3).getPosition(), 500);
						}	
						break;
						/* Todos os estados da partida */
					default : break ;
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

	private boolean haveITheBall() {
		double distanceToBall = this.selfPerc.getPosition().distanceTo(fieldPerc.getBall().getPosition());
		for(PlayerPerception p : this.fieldPerc.getAllPlayers()) {
			if(p.getPosition().distanceTo(fieldPerc.getBall().getPosition()) < distanceToBall) {
				return false;
			}
		}
		this.selfPerc.setState(EPlayerState.HAS_BALL);
		return true;
	}

	private void gkDash(Vector2D point){
		if (!isAlignToPoint(point, 15)) turnToPoint(point);
		
		commander.doDashBlocking(70);
	}
	private double percent(double value) {

		
		
		if(value < 0) {
			return (100 * value)/(-34);
		}else {
			return (100 * value)/(34);
		}
	}
	
	/*if(this.selfPerc.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) <= 16 ) {
							//this.turnToPoint(ballPos);
							
							if(ballPos.getY() < 0) {
								this.dash(new Vector2D(initPos.getX(), (ballPos.getY() < -7 )? -5f:  ballPos.getY()  ));
							}else {
								this.dash(new Vector2D(initPos.getX(), (ballPos.getY() < 7 )?   5f: ballPos.getY()  ));
							}
							
					
							if(!this.teamHasBall()) {
								
								
								
							}
							else if(this.selfPerc.getState() == EPlayerState.HAS_BALL) {
								//this.turnToPoint(new Vector2D(0,0));
								
								this.kickToPoint(
										PlayerUtils.getClosestTeammatePoint(this.fieldPerc,this.selfPerc.getPosition(), this.selfPerc.getSide(), 3).getPosition(), 500);
							}
						
						
						}else {*/
}
