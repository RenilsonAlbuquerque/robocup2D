package FPF.team.Ibis;

import java.awt.Rectangle;


import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.EPlayerState;
import simple_soccer_lib.utils.Vector2D;
import utils.PlayerUtils;


public class IbisPlayer extends Thread {
	private int LOOP_INTERVAL = 100; //0.1s
	private PlayerCommander commander;
	private PlayerPerception selfPerc;
	private FieldPerception fieldPerc;
	private MatchPerception matchPerc;

	public IbisPlayer(PlayerCommander player) {
		commander = player;
	}
	@Override
	public void run() {
		System. out .println(">> Executando...");
		long nextIteration = System.currentTimeMillis() + LOOP_INTERVAL;
		updatePerceptions();
		switch (selfPerc.getUniformNumber()) {
		case 1:
			actionGoalKeeper(nextIteration);
			break ;
		case 2:
			actionBack(nextIteration, -1); // cima
			break ;
		case 3:
			actionBack(nextIteration, 1); // baixo
			break ;
		case 4:
			actionMinFielder(nextIteration, -1); // cima
			break ;
		case 5:
			actionMinFielder(nextIteration, 0); // centro
			break ;
		case 6:
			actionMinFielder(nextIteration, 1); // baixo
			break ;
		case 7:
			actionForward(nextIteration);
			break ;
		default : break ;
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
		this.updateState();
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
	private void gkDash(Vector2D point){
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

	

	private void actionGoalKeeper( long nextIteration) {
		//new Arc2D.Double(50, 50, 300, 300, 180, 90, Arc2D.PIE)
		double xInit=-48, yInit=0, ballX=0, ballY=0;
		double newX = 0, newY =0;
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
						
						ballX=fieldPerc.getBall().getPosition().getX();
						ballY=fieldPerc.getBall().getPosition().getY();
						
						
						
						if(side == EFieldSide.RIGHT) {
							/*
							if(ballX < 0 ) {//se a bola está antes do meio do campo
								newX = 37; 
							}
							else */if(ballX > 37) { // se a bola está dentro da grande área
								newX = 50;
							}else {// se a bola está na intermediária
								newX = 47;
							}
						}else {
							/*if(ballX > 0 ) {//se a bola está antes do meio do campo
								newX = -37; 
							}
							else */if(ballX < -36) { // se a bola está dentro da grande área
								newX = -50;
							}else {// se a bola está na intermediária
								newX = -47;
							}
						}
						
						
						double distanceUp = new Vector2D(ballX,ballY).distanceTo(new Vector2D(newX,this.selfPerc.getPosition().getY() - 1f));
						double distanceDown = new Vector2D(ballX,ballY).distanceTo(new Vector2D(newX,this.selfPerc.getPosition().getY() + 1f));
						//System.out.println(distanceUp + " " + distanceDown);
						
						if(distanceUp < distanceDown) {
							//System.out.println("Pra cima");
							if(this.selfPerc.getPosition().getY() > -6.0) {
								//this.turnToPoint(new Vector2D(newX,this.selfPerc.getPosition().getY() - 0.5));
								this.gkDash(new Vector2D(newX,this.selfPerc.getPosition().getY() - 0.5));
							}
							
						}else{
							
							if(this.selfPerc.getPosition().getY() < 6.0) {
								//this.turnToPoint(new Vector2D(newX,this.selfPerc.getPosition().getY() + 0.5));
								this.gkDash(new Vector2D(newX,this.selfPerc.getPosition().getY() + 0.5));
							}
							
						}
						
						
						
						
						/*
						if (isPointsAreClose(selfPerc.getPosition(),
								ballPos, 1)){
							// chutar
							kickToPoint( new Vector2D(0,0), 100);
						} else if (area.contains(ballX, ballY)){
							// defender
							dash(ballPos);
						} else if (!isPointsAreClose(selfPerc.getPosition(),
								initPos, 3)){
							// recuar
							dash(initPos);
						} else {
							// olhar para a bola
							turnToPoint(ballPos);
						}
						*/
						
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
	
	private void actionBack( long nextIteration, int pos) {
		double xInit=-32, yInit=12*pos;
		EFieldSide side = selfPerc.getSide();
		Vector2D initPos = new Vector2D(xInit*side.value(), yInit*side.value());
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
						kickToPoint(PlayerUtils.getClosestTeammatePoint(this.fieldPerc,this.selfPerc.getPosition(), this.selfPerc.getSide(), 5).getPosition(), 5);
					}else{
						this.freeFromMark(new Vector2D(xInit,yInit));
					}
				}else{
					this.mark();
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

	private void actionMinFielder( long nextIteration, int pos) {
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
						System.out.println("I have the ball");
						//toca pra alguém desmarcado
						kickToPoint(PlayerUtils.getClosestTeammatePoint(this.fieldPerc,this.selfPerc.getPosition(), this.selfPerc.getSide(), 5).getPosition(), 5);
					}else{
						this.freeFromMark(new Vector2D(xInit,yInit));
					}
				}else{
					this.mark();
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

	private void actionForward( long nextIteration) {
		double xInit=-9, yInit=0;
		EFieldSide side = selfPerc.getSide();
		Vector2D initPos = new Vector2D(xInit*side.value(), yInit);
		Vector2D goalPos = new Vector2D(50*side.value(), 0);
		Vector2D ballPos;
		PlayerPerception pTemp;
		while ( true ) {
			
			updatePerceptions();
			ballPos = fieldPerc.getBall().getPosition();
			switch (matchPerc.getState()) {
			case BEFORE_KICK_OFF :
				commander.doMoveBlocking(xInit, yInit);
				break ;
			case PLAY_ON :
				if (PlayerUtils.isPointsAreClose(selfPerc.getPosition(),
						ballPos, 1)){
					if (PlayerUtils.isPointsAreClose(ballPos, goalPos, 30)){
						// chuta para o gol
						kickToPoint(goalPos, 100);
					} else {
						// conduz para o gol
						kickToPoint(goalPos, 25);
					}
				} else {
					pTemp = PlayerUtils.getClosestTeammatePoint(this.fieldPerc,ballPos,
							side, 3);
					if (pTemp != null &&
							pTemp.getUniformNumber() == selfPerc
							.getUniformNumber()){
						// pega a bola
						dash(ballPos);
					} else if (!PlayerUtils.isPointsAreClose(selfPerc
							.getPosition(),initPos, 3)){
						// recua
						dash(initPos);
					} else {
						// olha para a bola
						turnToPoint(ballPos);
					}
				}
				break ;
				/* Todos os estados da partida */
			default :
				break ;
			}
		}
	}
	
	/*------------*/
	private boolean updateState() {
		if(PlayerUtils.isPointsAreClose(this.selfPerc.getPosition(),this.fieldPerc.getBall().getPosition(),2)) {
			this.selfPerc.setState(EPlayerState.HAS_BALL);
			return true;
		}
		if(!PlayerUtils.getClosestTeammatePoint(this.fieldPerc,this.selfPerc.getPosition(), this.selfPerc.getSide(), 3).getSide().equals(this.selfPerc.getSide()) ) {
			this.selfPerc.setState(EPlayerState.CATCH);
			return true;
		}
		return true;
		
	}
	private boolean teamHasBall() {
		for(PlayerPerception p: this.fieldPerc.getTeamPlayers(this.selfPerc.getSide())) {
			if(p.getState().equals(EPlayerState.HAS_BALL))
				return true;
		}
		return false;
	}
	private void mark() {
		//Estou no raio de ação da jogada?
		if(PlayerUtils.isPointsAreClose(selfPerc.getPosition(),this.fieldPerc.getBall().getPosition(), 4)) {
			//Sou o marcador mais próximo?
			if(iAmTheLastPlayer()) {
				//tenta roubar a bola
				this.dash(this.fieldPerc.getBall().getPosition());
			}else{
				//faz a cobertura
				this.dash(this.fieldPerc.getBall().getPosition());
			}
		}else{ //procuro alguém próximo pra marcar
			this.dash(PlayerUtils. searchNearbyEnemy(fieldPerc, this.selfPerc.getSide(), this.selfPerc.getPosition(), 5).getPosition());
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
		if(marker.getPosition().distanceTo(this.selfPerc.getPosition()) <= 3){
			this.dash(playerOriginalPosition);
		}
	}
	

}

/*
 * 
 * if(ballY <  0) { //cima
							double lessDistance = 100;
							float i = 0;
							for(i = 0; i >= -7; i -= 0.5f) {
								double newDistance = new Vector2D(ballX,ballY).distanceTo(new Vector2D(newX,i));
								if(newDistance < lessDistance) {
									lessDistance = newDistance;
								}
							}
							
							this.turnToPoint(new Vector2D(newX,i));
							this.dash(new Vector2D(newX,i));
						}
						else if(ballY > 0) {//baixo
							
							double lessDistance = 100;
							float i = 0;
							for(i =0; i <= 7; i += 0.5f) {
								double newDistance = new Vector2D(ballX,ballY).distanceTo(new Vector2D(newX,i));
								if(newDistance < lessDistance) {
									lessDistance = newDistance;
								}
							}
							
							this.turnToPoint(new Vector2D(newX,i));
							this.dash(new Vector2D(newX,i));
						}else {
							
								this.turnToPoint(new Vector2D(newX,0));
								this.dash(new Vector2D(newX,0));
							
							
						}
						*/
