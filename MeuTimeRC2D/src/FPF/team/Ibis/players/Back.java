package FPF.team.Ibis.players;

import java.awt.Rectangle;
//import java.util.ArrayList;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.EPlayerState;
import simple_soccer_lib.utils.Vector2D;
import utils.PlayerUtils;


public class Back extends Thread {
	//private int LOOP_INTERVAL = 100; //0.1s
	private PlayerCommander commander;
	private PlayerPerception selfPerc;
	private FieldPerception fieldPerc;
	private MatchPerception matchPerc;
	private Rectangle areaDef;
	private Rectangle areaCob;
	private EFieldSide side;
	private double xDef;
	private double yDef;
	private double xAtc;
	private double yAtc;
	private double xCob;
	private double yCob;
	private double xEscan;
	private double yEscan;
	private Vector2D defPos;
	private Vector2D atcPos;
	private Vector2D cobPos;
	private Vector2D escanPos;
	private Vector2D goalPos;
	private PlayerPerception tocarPara;
	//private ArrayList<Rectangle> myAreas;

	public Back(PlayerCommander player, long nextIteration,int pos) {
		commander = player;
		this.updatePerceptions();
		this.xDef=-36;
		this.yDef=6.5*pos;
		this.xAtc=-30;
		this.yAtc=8*pos;
		this.xEscan=-42;
		this.yEscan=7*pos;
		this.xCob=-47;
		this.yCob=7*pos*-1;
		this.side = this.selfPerc.getSide();
		this.defPos = new Vector2D(this.xDef*this.side.value(), this.yDef*this.side.value());
		this.atcPos = new Vector2D(this.xAtc*this.side.value(), this.yAtc*this.side.value());
		this.cobPos = new Vector2D(this.xCob*this.side.value(), this.yCob*this.side.value());
		this.escanPos = new Vector2D(this.xEscan*this.side.value(), this.yEscan*this.side.value());
		this.goalPos = new Vector2D(50*side.value(), 0);  
		if(pos != -1) {
			this.areaDef = this.side == EFieldSide.LEFT ?
					new Rectangle(-53, 0, 32, 35):
						new Rectangle(21, -34, 32, 35);
			this.areaCob = this.side == EFieldSide.LEFT ?
					new Rectangle(-53, -25, 21, 25):
						new Rectangle(32, 0, 21, 25);
		}else {
			this.areaDef = this.side == EFieldSide.LEFT ?
					new Rectangle(-53, -34, 32, 35):
						new Rectangle(21, 0, 32, 35);
			this.areaCob = this.side == EFieldSide.LEFT ?
					new Rectangle(-53, 0, 21, 25):
						new Rectangle(32, -25, 21, 25);
		}
		/*if(pos == -1) {
			this.areaCob = this.side == EFieldSide.LEFT ?
					new Rectangle(-53, 0, 21, 25):
						new Rectangle(32, -25, 21, 25);
		}else {
			this.areaCob = this.side == EFieldSide.LEFT ?
					new Rectangle(-53, -25, 21, 25):
						new Rectangle(32, 0, 21, 25);
		}*/
		this.action(nextIteration, pos);
		
	}
	
	private void action( long nextIteration, int pos) {
		//("Cheguei");
		this.updatePerceptions();
		Vector2D ballPos;
		while ( true ) {
			updatePerceptions();
			ballPos = fieldPerc.getBall().getPosition();
			switch (matchPerc.getState()) {
			case BEFORE_KICK_OFF :
				commander.doMoveBlocking(this.xDef, this.yDef);
				break ;
			case PLAY_ON :
				if(!(this.teamIsAtc())){ // meu time não tem a bola?
					//Chuta para longe do gol
					if(ballPos.distanceTo(selfPerc.getPosition())<=1.3 && ballPos.getY() < 17 && ballPos.getY() > -17 && (((ballPos.getX()<-32)&&(selfPerc.getSide().value() == 1)) || ((ballPos.getX()>32)&&(selfPerc.getSide().value() == -1)))) {
						//("Chuta para longe "+selfPerc.getUniformNumber());
						//this.turnToPoint(goalPos);
						//kickToPoint(new Vector2D(-53*side.value(),18*pos),150);
						kickToPoint(goalPos,150);
					}else if(fieldPerc.getBall().getPosition().distanceTo(selfPerc.getPosition())<=1){
						if(pos != -1) {
							this.tocarPara = fieldPerc.getTeamPlayer(side, 5);
						}else {
							this.tocarPara = fieldPerc.getTeamPlayer(side, 6);
						}
						//("Jogador "+selfPerc.getTeam()+" Toca para "+tocarPara.getUniformNumber() + tocarPara.getTeam());
						turnToPoint(this.tocarPara.getPosition());
						//kickToPoint(this.tocarPara.getPosition(), 4*ballPos.distanceTo(this.getBestPlayerToWork().getPosition()));
						//(selfPerc.getPosition().distanceTo(this.tocarPara.getPosition()));
						if(selfPerc.getPosition().distanceTo(this.tocarPara.getPosition())<12) {
							kickToPoint(this.tocarPara.getPosition(), 6*selfPerc.getPosition().distanceTo(this.tocarPara.getPosition()));
						}else if(selfPerc.getPosition().distanceTo(this.tocarPara.getPosition())<30){
							kickToPoint(this.tocarPara.getPosition(), 7*selfPerc.getPosition().distanceTo(this.tocarPara.getPosition()));
						}/*else if(selfPerc.getPosition().distanceTo(this.tocarPara.getPosition())<30){
							kickToPoint(this.tocarPara.getPosition(),7*selfPerc.getPosition().distanceTo(this.tocarPara.getPosition()));
						}*/else {

							kickToPoint(this.tocarPara.getPosition(),5*selfPerc.getPosition().distanceTo(this.tocarPara.getPosition()));
						}
						}else {
						//Marca a bola
						if(areaDef.contains(ballPos.getX(), ballPos.getY())) {
							//("Marca bola"+selfPerc.getUniformNumber());
							if(selfPerc.getPosition().distanceTo(ballPos) > 6)
								ballPos.setX(ballPos.getX()+(-5*side.value()));
							this.dash(fieldPerc.getBall().getPosition());
						}else {
							//Cobertura
							if(areaCob.contains(ballPos.getX(), ballPos.getY())) {
								if(selfPerc.getPosition().distanceTo(ballPos)<7) {
									this.dash(fieldPerc.getBall().getPosition());
								}else
								//("Cobertura "+selfPerc.getUniformNumber());
								this.dash(cobPos);								
							}else {
								//("Posiciona defesa"+selfPerc.getUniformNumber()+" Time"+selfPerc.getTeam());
								this.walk(defPos);
							}							
						}
					}										
				}else{
					//("Time tem bola "+selfPerc.getTeam());
					if(this.selfPerc.getState() == EPlayerState.HAS_BALL){ //eu estou com a bola?
						//toca pra alguém desmarcado
						//("Toca "+selfPerc.getUniformNumber());
						kickToPoint(PlayerUtils.getClosestTeammatePoint(this.fieldPerc,this.selfPerc.getPosition(), this.selfPerc.getSide(), 10).getPosition(), 50);
					}else{
						this.walk(atcPos);
					}
				}
				break ;
			case KICK_OFF_RIGHT :
				//commander.doMoveBlocking(xDef, yDef);
				dash(defPos);
				break ;
			case KICK_OFF_LEFT :
				dash(defPos);
				//commander.doMoveBlocking(xDef, yDef);
				break ;
			case AFTER_GOAL_RIGHT :
				//commander.doMoveBlocking(xDef, yDef);
				dash(defPos);
				break ;
			case AFTER_GOAL_LEFT :
				dash(defPos);
				//commander.doMoveBlocking(xDef, yDef);
				break ;
			case KICK_IN_RIGHT :
				if(selfPerc.getSide().equals(EFieldSide.RIGHT)) {
					if(pos != -1 && fieldPerc.getBall().getPosition().getX() >=36 && fieldPerc.getBall().getPosition().getY() <= -1){
						if(ballPos.distanceTo(selfPerc.getPosition())<=1) {
							kickToPoint(fieldPerc.getTeamPlayer(side, 4).getPosition(),250);
						}else {
							commander.doMoveBlocking(fieldPerc.getBall().getPosition().getX(),fieldPerc.getBall().getPosition().getY());
							dash(fieldPerc.getBall().getPosition());
						}			
						
					}else if(pos == -1 && fieldPerc.getBall().getPosition().getX() >= 36 && fieldPerc.getBall().getPosition().getY() >= 1){
						if(ballPos.distanceTo(selfPerc.getPosition())<=1) {
							kickToPoint(fieldPerc.getTeamPlayer(side, 4).getPosition(),250);
						}else {
							commander.doMoveBlocking(fieldPerc.getBall().getPosition().getX(),fieldPerc.getBall().getPosition().getY());
							dash(fieldPerc.getBall().getPosition());
						}						
					}else {
						this.dash(defPos);
					}
				}else {
					this.dash(defPos);
				}
				break ;
			case KICK_IN_LEFT :
				if(selfPerc.getSide().equals(EFieldSide.LEFT)) {
					if(pos == -1 && fieldPerc.getBall().getPosition().getX() <= -36 && fieldPerc.getBall().getPosition().getY() <= -1){
						if(ballPos.distanceTo(selfPerc.getPosition())<=1) {
							kickToPoint(fieldPerc.getTeamPlayer(side, 4).getPosition() ,250);
						}else{
							//("Aqui");
							//commander.doMoveBlocking(fieldPerc.getBall().getPosition().getX(),fieldPerc.getBall().getPosition().getY());
							this.dash(fieldPerc.getBall().getPosition());
						}			
						
					}else if(pos != -1 && fieldPerc.getBall().getPosition().getX() <= -36 && fieldPerc.getBall().getPosition().getY() >= 1){
						if(ballPos.distanceTo(selfPerc.getPosition())<=1) {
							kickToPoint(fieldPerc.getTeamPlayer(side, 4).getPosition() ,250);
						}else {
							//("Aqui");
							//commander.doMoveBlocking(fieldPerc.getBall().getPosition().getX(),fieldPerc.getBall().getPosition().getY());
							this.dash(fieldPerc.getBall().getPosition());
						}
						
					}else {
						this.dash(defPos);
					}
				}else {
					this.dash(defPos);
				}
				break ;
			case CORNER_KICK_LEFT :
				if(selfPerc.getSide().equals(EFieldSide.RIGHT))
					dash(escanPos);
				break ;
			case CORNER_KICK_RIGHT :
				if(selfPerc.getSide().equals(EFieldSide.LEFT))
					dash(escanPos);
				break ;
			case GOAL_KICK_RIGHT :
				walk(defPos);
				break;
			case GOAL_KICK_LEFT :
				walk(defPos);	
				break;
				/* Todos os estados da partida */
			default :
				//(matchPerc.getState());
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

	private void dash(Vector2D point){
		if (selfPerc.getPosition().distanceTo(point) <= 1) return ;
		if (!isAlignToPoint(point, 10)) turnToPoint(point);
		if(selfPerc.getPosition().distanceTo(point) <= 1.5) {
			commander.doDashBlocking(50);
		}else if(selfPerc.getPosition().distanceTo(point) < 2) {
			commander.doDashBlocking(60);			
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
	
	private void walk(Vector2D point){
		if (selfPerc.getPosition().distanceTo(point) <= 1) return ;
		if (!isAlignToPoint(point, 10)) turnToPoint(point);
			commander.doDashBlocking(50);
		
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
				//(smallerDistanceOutherTeam-smallerDistanceMyTeam);
			}return smallerDistanceMyTeam <= smallerDistanceOutherTeam || smallerDistanceOutherTeam-smallerDistanceMyTeam >= 10;
		
		}else{
			for(PlayerPerception p: this.fieldPerc.getTeamPlayers(EFieldSide.LEFT)) {
				if(p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) < smallerDistanceMyTeam)
					smallerDistanceOutherTeam = p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition());
			}
			for(PlayerPerception p: this.fieldPerc.getTeamPlayers(EFieldSide.RIGHT)) {
				if(p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition()) < smallerDistanceMyTeam)
					smallerDistanceMyTeam = p.getPosition().distanceTo(this.fieldPerc.getBall().getPosition());
				//(smallerDistanceOutherTeam-smallerDistanceMyTeam);
			}return smallerDistanceMyTeam <= smallerDistanceOutherTeam || smallerDistanceOutherTeam-smallerDistanceMyTeam >= 10;
		}
	}
	
	private boolean teamIsAtc() {
		if(this.selfPerc.getSide().equals(EFieldSide.LEFT)) {
			if(this.fieldPerc.getBall().getPosition().getX() >= 20) {
				return true;
			}else {
				//("Left : "+ (this.teamHasBall() && this.fieldPerc.getBall().getPosition().getX() >= -10));
				return this.teamHasBall() && this.fieldPerc.getBall().getPosition().getX() >= -2;
			}
			
		}else{
			if(this.fieldPerc.getBall().getPosition().getX() <= -20) {
				return true;
			}else{
				//("Right : "+ (this.teamHasBall() && this.fieldPerc.getBall().getPosition().getX() <= 10));
				return this.teamHasBall() && this.fieldPerc.getBall().getPosition().getX() <= 2;
			}
			
		}
	}
	
}
