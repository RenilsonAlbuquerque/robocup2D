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
	//private ArrayList<Rectangle> myAreas;

	public Back(PlayerCommander player, long nextIteration,int pos) {
		commander = player;
		this.updatePerceptions();
		this.xDef=-36;
		this.yDef=10*pos;
		this.xAtc=-30;
		this.yAtc=10*pos;
		this.xEscan=-42;
		this.yEscan=10*pos;
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
					new Rectangle(-53, 0, 36, 34):
						new Rectangle(17, -34, 36, 34);
			this.areaCob = this.side == EFieldSide.LEFT ?
					new Rectangle(-53, -25, 21, 25):
						new Rectangle(32, 0, 21, 25);
		}else {
			this.areaDef = this.side == EFieldSide.LEFT ?
					new Rectangle(-53, -34, 36, 34):
						new Rectangle(17, 0, 36, 34);
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
					if(ballPos.distanceTo(selfPerc.getPosition())<=1) {
						//System.out.println("Chuta para longe "+selfPerc.getUniformNumber());
						kickToPoint(goalPos,100);
					}else {
						//Marca a bola
						if(areaDef.contains(ballPos.getX(), ballPos.getY())) {
							//System.out.println("Marca bola"+selfPerc.getUniformNumber());
							this.dash(ballPos);
						}else {
							//Cobertura
							if(areaCob.contains(ballPos.getX(), ballPos.getY())) {
								//System.out.println("Cobertura "+selfPerc.getUniformNumber());
								this.dash(cobPos);								
							}else {
								//System.out.println("Posiciona "+selfPerc.getUniformNumber());
								this.dash(defPos);
							}							
						}
					}										
				}else{
					//System.out.println("Time tem bola "+selfPerc.getTeam());
					if(this.selfPerc.getState() == EPlayerState.HAS_BALL){ //eu estou com a bola?
						//toca pra alguém desmarcado
						//System.out.println("Toca "+selfPerc.getUniformNumber());
						kickToPoint(PlayerUtils.getClosestTeammatePoint(this.fieldPerc,this.selfPerc.getPosition(), this.selfPerc.getSide(), 10).getPosition(), 10);
					}else{
						this.dash(atcPos);
					}
				}
				break ;
			case GOAL_KICK_RIGHT :
				commander.doMoveBlocking(xDef, yDef);
				break ;
			case GOAL_KICK_LEFT :
				commander.doMoveBlocking(xDef, yDef);
				break ;
			case KICK_IN_RIGHT :
				if(selfPerc.getSide().equals(EFieldSide.RIGHT)) {
					if(pos == -1 && fieldPerc.getBall().getPosition().getX() >=36 && fieldPerc.getBall().getPosition().getY() <= -33){
						if(ballPos.distanceTo(selfPerc.getPosition())<=1) {
							kickToPoint(fieldPerc.getTeamPlayer(side, 4).getPosition() ,50);
						}else {
							commander.doMoveBlocking(fieldPerc.getBall().getPosition().getX(),fieldPerc.getBall().getPosition().getY());
							//dash(fieldPerc.getBall().getPosition());
						}			
						
					}else if(fieldPerc.getBall().getPosition().getX() >= 36 && fieldPerc.getBall().getPosition().getY() >= 33){
						if(ballPos.distanceTo(selfPerc.getPosition())<=1) {
							kickToPoint(fieldPerc.getTeamPlayer(side, 4).getPosition() ,50);
						}else {
							commander.doMoveBlocking(fieldPerc.getBall().getPosition().getX(),fieldPerc.getBall().getPosition().getY());
							//dash(fieldPerc.getBall().getPosition());
						}						
					}
				}
				break ;
			case KICK_IN_LEFT :
				if(selfPerc.getSide().equals(EFieldSide.LEFT)) {
					if(pos == -1 && fieldPerc.getBall().getPosition().getX() <= -36 && fieldPerc.getBall().getPosition().getY() <= -33){
						if(ballPos.distanceTo(selfPerc.getPosition())<=1) {
							kickToPoint(fieldPerc.getTeamPlayer(side, 4).getPosition() ,50);
						}else {
							//System.out.println("Aqui");
							//commander.doMoveBlocking(fieldPerc.getBall().getPosition().getX(),fieldPerc.getBall().getPosition().getY());
							this.dash(fieldPerc.getBall().getPosition());
						}			
						
					}else if(fieldPerc.getBall().getPosition().getX() <= -36 && fieldPerc.getBall().getPosition().getY() >= 33){
						if(ballPos.distanceTo(selfPerc.getPosition())<=1) {
							kickToPoint(fieldPerc.getTeamPlayer(side, 4).getPosition() ,50);
						}else {
							//System.out.println("Aqui");
							//commander.doMoveBlocking(fieldPerc.getBall().getPosition().getX(),fieldPerc.getBall().getPosition().getY());
							this.dash(fieldPerc.getBall().getPosition());
						}
						
					}
				}
				break ;
			case CORNER_KICK_LEFT :
				if(selfPerc.getSide().equals(EFieldSide.LEFT))
					dash(escanPos);
				break ;
			case CORNER_KICK_RIGHT :
				if(selfPerc.getSide().equals(EFieldSide.RIGHT))
					dash(escanPos);
				break ;
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
				if(this.fieldPerc.getBall().getPosition().getX() > 10 ) {
					this.dash(new Vector2D(0 - 2,originalPosition.getY()));
				}
				else {
					this.dash(originalPosition);
				}
			}else {
				if(this.fieldPerc.getBall().getPosition().getX() < - 10 ) {
					this.dash(new Vector2D(0 +2, originalPosition.getY()));
				}else {
					this.dash(originalPosition);
				}
			}
	
	}
	/*private boolean teamHasBall() {
		for(PlayerPerception p: this.fieldPerc.getTeamPlayers(this.selfPerc.getSide())) {
			if(p.getState().equals(EPlayerState.HAS_BALL))
				return true;
		}
		return false;
	}*/
	
	private boolean teamHasBall() {
		double smallerDistanceMyTeam = 2000;
		double smallerDistanceOutherTeam = 200;
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
				return this.teamHasBall() && this.fieldPerc.getBall().getPosition().getX() >= -10;
			}
			
		}else{
			if(this.fieldPerc.getBall().getPosition().getX() <= 20) {
				return true;
			}else{
				//System.out.println("Right : "+ (this.teamHasBall() && this.fieldPerc.getBall().getPosition().getX() <= 10));
				return this.teamHasBall() && this.fieldPerc.getBall().getPosition().getX() <= 10;
			}
			
		}
	}
	
	private PlayerPerception hasAdversaryInMyArea() {
		for(PlayerPerception p: this.fieldPerc.getTeamPlayers(EFieldSide.invert(selfPerc.getSide()))) {
			if(this.areaDef.contains(p.getPosition().getX(), p.getPosition().getY()))
				return p;
		}
		return null;
	}

	

}
