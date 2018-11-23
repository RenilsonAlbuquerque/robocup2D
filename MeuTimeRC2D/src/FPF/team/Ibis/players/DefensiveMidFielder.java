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


public class DefensiveMidFielder extends Thread {
	private PlayerCommander commander;
	private PlayerPerception selfPerc;
	private FieldPerception fieldPerc;
	private MatchPerception matchPerc;
	private Rectangle areaDef;
	private Rectangle areaCobBaixo;
	private Rectangle areaCobCima;
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

	public DefensiveMidFielder(PlayerCommander player, long nextIteration) {
		commander = player;
		this.updatePerceptions();
		this.xDef=-22;
		this.yDef=0;
		this.xAtc=-10;
		this.yAtc=0;
		this.xEscan=-31;
		this.yEscan=0;
		this.xCob=-33;
		this.yCob=7;
		this.side = this.selfPerc.getSide();
		this.defPos = new Vector2D(this.xDef*this.side.value(), this.yDef*this.side.value());
		this.atcPos = new Vector2D(this.xAtc*this.side.value(), this.yAtc*this.side.value());
		this.cobPos = new Vector2D(this.xCob*this.side.value(), this.yCob*this.side.value());
		this.escanPos = new Vector2D(this.xEscan*this.side.value(), this.yEscan*this.side.value());
		this.goalPos = new Vector2D(50*side.value(), 0);		
		this.areaDef = this.side == EFieldSide.LEFT ?
				new Rectangle(-35, -17, 30, 34):
					new Rectangle(0, -17, 30, 34);
		this.areaCobCima = this.side == EFieldSide.LEFT ?
				new Rectangle(-53, -25, 21, 25):
					new Rectangle(32, -25, 21, 25);					
		this.areaCobBaixo = this.side == EFieldSide.LEFT ?
				new Rectangle(-53, 0, 21, 25):
					new Rectangle(32, 0, 21, 25);
					
		this.action(nextIteration);
		
	}
	
	private void action( long nextIteration) {
		//System.out.println("Cheguei");
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
					if(ballPos.distanceTo(selfPerc.getPosition())<=1 && (((ballPos.getX()<-32)&&(selfPerc.getSide().value() == 1)) || ((ballPos.getX()>32)&&(selfPerc.getSide().value() == -1)))) {
						//System.out.println("Chuta para longe "+selfPerc.getUniformNumber());
						kickToPoint(goalPos,150);
					}else if(ballPos.distanceTo(selfPerc.getPosition())<=1){
						this.tocarPara = this.getBestPlayerToWork();
						//Toca para meias
						if(tocarPara != null) {
							//System.out.println("Jogador "+selfPerc.getTeam()+" Toca para "+tocarPara.getUniformNumber() + tocarPara.getTeam());
							turnToPoint(this.tocarPara.getPosition());
							//kickToPoint(this.tocarPara.getPosition(), 4*ballPos.distanceTo(this.getBestPlayerToWork().getPosition()));
							//System.out.println(selfPerc.getPosition().distanceTo(this.tocarPara.getPosition()));
							if(selfPerc.getPosition().distanceTo(this.tocarPara.getPosition())<15) {
								kickToPoint(this.tocarPara.getPosition(), 5*selfPerc.getPosition().distanceTo(this.tocarPara.getPosition()));
							}else if(selfPerc.getPosition().distanceTo(this.tocarPara.getPosition())<30){
								kickToPoint(this.tocarPara.getPosition(), 4*selfPerc.getPosition().distanceTo(this.tocarPara.getPosition()));
							}/*else if(selfPerc.getPosition().distanceTo(this.tocarPara.getPosition())<30){
								kickToPoint(this.tocarPara.getPosition(),7*selfPerc.getPosition().distanceTo(this.tocarPara.getPosition()));
							}*/else {

								kickToPoint(this.tocarPara.getPosition(),3*selfPerc.getPosition().distanceTo(this.tocarPara.getPosition()));
							}
						//Conduz
						}else {
							if(ballPos.distanceTo(new Vector2D(0,0))<=15) {
								kickToPoint(fieldPerc.getTeamPlayer(side, 7).getPosition(),6*selfPerc.getPosition().distanceTo(fieldPerc.getTeamPlayer(side, 7).getPosition()));
							}else {
								ballPos.setX(ballPos.getX()+4*side.value());
								kickToPoint(ballPos,10);
							}
							
						}
						
					}else {
						//Marca a bola
						if(areaDef.contains(ballPos.getX(), ballPos.getY())) {
							//System.out.println("Marca bola"+selfPerc.getUniformNumber());
							if(selfPerc.getPosition().distanceTo(ballPos) > 5)
								ballPos.setX(ballPos.getX()+(-3*side.value()));
							this.dash(ballPos);
						}else {
							//Cobertura
							if(areaCobCima.contains(ballPos.getX(), ballPos.getY())) {
								//System.out.println("Cobertura "+selfPerc.getUniformNumber());
								if(selfPerc.getPosition().distanceTo(ballPos)<5) {
									this.dash(fieldPerc.getBall().getPosition());
								}else
								cobPos.setY(yCob);
								this.dash(cobPos);								
							}else if(areaCobBaixo.contains(ballPos.getX(), ballPos.getY())){
								cobPos.setY(yCob*-1);
								this.dash(cobPos);
							}else {
								//System.out.println("Posiciona defesa"+selfPerc.getUniformNumber()+" Time"+selfPerc.getTeam());
								this.walk(defPos);
							}							
						}
					}										
				}else{
					//System.out.println("Time tem bola "+selfPerc.getTeam());
					if(this.selfPerc.getState() == EPlayerState.HAS_BALL){ //eu estou com a bola?
						//toca pra alguém desmarcado
						//System.out.println("Toca "+selfPerc.getUniformNumber());
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
				if(selfPerc.getSide().value() == 1 && ballPos.getX() <= 0) {
					dash(defPos);
				}else {
					dash(atcPos);
				}
				break ;
			case KICK_IN_LEFT :
				if(selfPerc.getSide().value() != 1 && ballPos.getX() >= 0) {
					dash(defPos);
				}else {
					dash(atcPos);
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
				dash(defPos);
				break;
			case GOAL_KICK_LEFT :
				dash(defPos);	
				break;
				/* Todos os estados da partida */
			default :
				//System.out.println(matchPerc.getState());
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
		//System.out.println("Alinhou "+selfPerc.getTeam());
		commander.doTurnToDirectionBlocking(newDirection);
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
	
	private boolean teamIsAtc() {
		if(this.selfPerc.getSide().equals(EFieldSide.LEFT)) {
			if(this.fieldPerc.getBall().getPosition().getX() >= 20) {
				return true;
			}else {
				//System.out.println("Left : "+ (this.teamHasBall() && this.fieldPerc.getBall().getPosition().getX() >= -10));
				return this.teamHasBall() && this.fieldPerc.getBall().getPosition().getX() >= -2;
			}
			
		}else{
			if(this.fieldPerc.getBall().getPosition().getX() <= -20) {
				return true;
			}else{
				//System.out.println("Right : "+ (this.teamHasBall() && this.fieldPerc.getBall().getPosition().getX() <= 10));
				return this.teamHasBall() && this.fieldPerc.getBall().getPosition().getX() <= 2;
			}
			
		}
	}
	
	private PlayerPerception getBestPlayerToWork() {
		double marcadores5=0;
		double marcadores6=0;
		for (PlayerPerception p : fieldPerc.getTeamPlayers(EFieldSide.invert(side))) {
			if(fieldPerc.getTeamPlayer(side, 5).getPosition().distanceTo(p.getPosition())<3) {
				marcadores5 = 10;
			}else if(fieldPerc.getTeamPlayer(side, 5).getPosition().distanceTo(p.getPosition())<8){
				marcadores5++;
			}
			if(fieldPerc.getTeamPlayer(side, 6).getPosition().distanceTo(p.getPosition())<3) {
				marcadores6 = 10;
			}else if(fieldPerc.getTeamPlayer(side, 6).getPosition().distanceTo(p.getPosition())<8){
				marcadores6++;
			}
				
		}
		if(selfPerc.getPosition().distanceTo(fieldPerc.getTeamPlayer(side, 5).getPosition()) < selfPerc.getPosition().distanceTo(fieldPerc.getTeamPlayer(side, 6).getPosition()) && marcadores5 <1){
			return fieldPerc.getTeamPlayer(side, 5);
		}else if(marcadores6<1) {
			return fieldPerc.getTeamPlayer(side, 6);
		}
		return null;
		
	}

}