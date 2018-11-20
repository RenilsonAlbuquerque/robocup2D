package FPF.team.Ibis.players;


import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.Vector2D;
import utils.PlayerUtils;


public class Goolkeeper {

	private PlayerCommander commander;
	private PlayerPerception selfPerc;
	private FieldPerception fieldPerc;
	private MatchPerception matchPerc;
	private Vector2D initPos;
	private EFieldSide side;
	private double xInit,yInit;
	private Vector2D centro;
	
	public Goolkeeper(PlayerCommander player,long nextIteration) {
		commander = player;
		this.updatePerceptions();
		xInit=-51;
		yInit=0;
		side = selfPerc.getSide();
		initPos = new Vector2D(xInit*side.value(), yInit*side.value());
		this.centro = new Vector2D(0,0);
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
	}

	private void turnToPoint(Vector2D point){
		Vector2D newDirection = point.sub(selfPerc.getPosition());
		commander.doTurnToDirectionBlocking(newDirection);
	}

	private void dash(Vector2D point){
		if (selfPerc.getPosition().distanceTo(point) <= 1) return ;
		if (!isAlignToPoint(point, 10)) turnToPoint(point);
		if(selfPerc.getPosition().distanceTo(point) <= 1.5) {
			commander.doDashBlocking(75);
		}else if(selfPerc.getPosition().distanceTo(point) < 2) {
			commander.doDashBlocking(80);			
		}else if(selfPerc.getPosition().distanceTo(point) < 3) {
			commander.doDashBlocking(85);			
		}else if(selfPerc.getPosition().distanceTo(point) < 4){
			commander.doDashBlocking(90);					
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

	

	public void action( long nextIteration) {
		this.updatePerceptions();
	
		Vector2D currentBallPos = null;
		Vector2D previousBallPos = null;
		Vector2D previousUnchangedPosition = this.fieldPerc.getBall().getPosition();
		/*
		Rectangle area = side == EFieldSide.LEFT ?
				new Rectangle(-52, -20, 16, 40):
					new Rectangle(36, -20, 16, 40);
		*/		
				while ( true ) {
					updatePerceptions();
					previousBallPos = previousUnchangedPosition;
					currentBallPos = this.fieldPerc.getBall().getPosition();
					switch (matchPerc.getState()) {
					case BEFORE_KICK_OFF :
						// posiciona
						commander.doMoveBlocking(xInit, yInit);
						break ;
					case PLAY_ON :
						
						
						if(this.selfPerc.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) <= 28) {
							if(this.selfPerc.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) <= 1.1) {
								//this.turnToPoint(this.fieldPerc.getBall().getPosition());
								//this.commander.doCatchBlocking(0);
								//this.turnToPoint(this.centro);
								//commander.doKickBlocking(500, 0);
								//System.out.println("goleiro olha"+selfPerc.getDirection());
								this.kickToPoint(this.centro, 500);
							}else if(currentBallPos.distanceTo(previousBallPos) >= 2.5) {
								previousUnchangedPosition = new Vector2D(currentBallPos.getX(),currentBallPos.getY());
								currentBallPos.setX((51 - Math.abs(currentBallPos.getX()))  * this.selfPerc.getSide().value() * (-1) );
								previousBallPos.setX((51 - Math.abs(previousBallPos.getX()))  * this.selfPerc.getSide().value() * (-1));
								
								//System.out.println(" new Previous: (" +  previousBallPos.getX() + ","+ previousBallPos.getY() + ") "+
									//	" Current: (" +  currentBallPos.getX() + ","+ currentBallPos.getY() + ")");
								
								double x1,x2,y1,y2;
								x1 = currentBallPos.getX();
								y1 = currentBallPos.getY();

								x2 = previousBallPos.getX();
								y2 = previousBallPos.getY();
								
								double y = ((x2 * y1) - (y2 * x1))/ (x2 - x1);
							
								if(y>-7 && y<7)
									this.dash(new Vector2D(initPos.getX(),y));
								
												
							}
							//System.out.println(" Previous: (" +  previousBallPos.getX() + ","+ previousBallPos.getY() + ") "+
							//" Current: (" +  currentBallPos.getX() + ","+ currentBallPos.getY() + ")");			
							
						}else {
							double y = 0;
							if(currentBallPos.getY() < 0 ) {
								y = (-7 *  ((100 * currentBallPos.getY())/(-34)) / 100);	
							}else {
								y = (7 *  ((100 * currentBallPos.getY())/(34)) / 100);
							}
							this.dash(new Vector2D(initPos.getX(),y));
						}
					
						
						
						break ;
					case GOAL_KICK_RIGHT :
						if(this.selfPerc.getSide() == EFieldSide.RIGHT) {
						
							this.dash(currentBallPos);
							if(this.selfPerc.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) <= 1) {
								this.kickToPoint(PlayerUtils.getClosestTeammatePoint(this.fieldPerc,this.selfPerc.getPosition(), this.selfPerc.getSide(), -3).getPosition(), -500);		
							}
						}
						break;
					case GOAL_KICK_LEFT :
						
						if(this.selfPerc.getSide() == EFieldSide.LEFT) {
							
							this.dash(currentBallPos);
							if(this.selfPerc.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) <= 1) {
								this.kickToPoint(PlayerUtils.getClosestTeammatePoint(this.fieldPerc,this.selfPerc.getPosition(), this.selfPerc.getSide(), 3).getPosition(), 500);
							
							}
							}	
						break;
						/* Todos os estados da partida */
					default : break ;
					}
				}
	}

	/*private boolean haveITheBall() {
		double distanceToBall = this.selfPerc.getPosition().distanceTo(fieldPerc.getBall().getPosition());
		for(PlayerPerception p : this.fieldPerc.getAllPlayers()) {
			if(p.getPosition().distanceTo(fieldPerc.getBall().getPosition()) < distanceToBall) {
				return false;
			}
		}
		this.selfPerc.setState(EPlayerState.HAS_BALL);
		return true;
	}*/

	 
}
