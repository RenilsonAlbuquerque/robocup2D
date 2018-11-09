package FPF.team.Ibis;

import java.awt.Rectangle;
import java.util.ArrayList;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.EPlayerState;
import simple_soccer_lib.utils.Vector2D;


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
		System.out.println(point.getX() + " " + point.getY());
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
				if(!p.getPosition().equals(point)) {
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
		}
		return np;
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
							if(ballX < 0 ) {//se a bola est� antes do meio do campo
								newX = 37; 
							}
							else */if(ballX > 37) { // se a bola est� dentro da grande �rea
								newX = 50;
							}else {// se a bola est� na intermedi�ria
								newX = 47;
							}
						}else {
							/*if(ballX > 0 ) {//se a bola est� antes do meio do campo
								newX = -37; 
							}
							else */if(ballX < -36) { // se a bola est� dentro da grande �rea
								newX = -50;
							}else {// se a bola est� na intermedi�ria
								newX = -47;
							}
						}
						
						
						double distanceUp = new Vector2D(ballX,ballY).distanceTo(new Vector2D(newX,this.selfPerc.getPosition().getY() - 1f));
						double distanceDown = new Vector2D(ballX,ballY).distanceTo(new Vector2D(newX,this.selfPerc.getPosition().getY() + 1f));
						//System.out.println(distanceUp + " " + distanceDown);
						System.out.println(this.selfPerc.getPosition().getY());
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
							
							System.out.println(this.getClosestPlayerPoint(this.selfPerc.getPosition(), this.selfPerc.getSide(), 3).getUniformNumber());
							this.dash(ballPos);
							this.kickToPoint(
							this.getClosestPlayerPoint(this.selfPerc.getPosition(), this.selfPerc.getSide(), -3).getPosition(), -500);
						}
						break;
					case GOAL_KICK_LEFT :
						
						
						if(this.selfPerc.getSide() == EFieldSide.LEFT) {
							
							this.dash(ballPos);
							this.kickToPoint(
									this.getClosestPlayerPoint(this.selfPerc.getPosition(), this.selfPerc.getSide(), 3).getPosition(), 500);
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
		Vector2D initPos =
				new Vector2D(xInit*side.value(), yInit*side.value());
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
				if (isPointsAreClose(selfPerc.getPosition(),
						ballPos, 1)){
					if (selfPerc.getUniformNumber() == 2){
						// toca para o jogador 3
						vTemp = fieldPerc.getTeamPlayer(
								side, 3).getPosition();
					} else {
						// toca para o jogador 4
						vTemp = fieldPerc.getTeamPlayer(
								side, 4).getPosition();
					}
					Vector2D vTempF = vTemp.sub(selfPerc
							.getPosition());
					double intensity =
							(vTempF.magnitude()*100)/40;
					kickToPoint(vTemp, intensity);
				} else {
					pTemp = getClosestPlayerPoint(ballPos,
							side, 3);
					if (pTemp != null &&
							pTemp.getUniformNumber() == selfPerc
							.getUniformNumber()){
						// pega a bola
						dash(ballPos);
					} else if (!isPointsAreClose(selfPerc
							.getPosition(), initPos, 3)){
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
				if (isPointsAreClose(selfPerc.getPosition(),
						ballPos, 1)){
					if (selfPerc.getUniformNumber() == 2){
						// toca para o jogador 3
						vTemp = fieldPerc.getTeamPlayer(
								side, 3).getPosition();
					} else {
						// toca para o jogador 4
						vTemp = fieldPerc.getTeamPlayer(
								side, 4).getPosition();
					}
					Vector2D vTempF = vTemp.sub(selfPerc
							.getPosition());
					double intensity =
							(vTempF.magnitude()*100)/40;
					kickToPoint(vTemp, intensity);
				} else {
					pTemp = getClosestPlayerPoint(ballPos,
							side, 3);
					if (pTemp != null &&
							pTemp.getUniformNumber() == selfPerc
							.getUniformNumber()){
						// pega a bola
						dash(ballPos);
					} else if (!isPointsAreClose(selfPerc
							.getPosition(), initPos, 3)){
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
				if (isPointsAreClose(selfPerc.getPosition(),
						ballPos, 1)){
					if (isPointsAreClose(ballPos, goalPos, 30)){
						// chuta para o gol
						kickToPoint(goalPos, 100);
					} else {
						// conduz para o gol
						kickToPoint(goalPos, 25);
					}
				} else {
					pTemp = getClosestPlayerPoint(ballPos,
							side, 3);
					if (pTemp != null &&
							pTemp.getUniformNumber() == selfPerc
							.getUniformNumber()){
						// pega a bola
						dash(ballPos);
					} else if (!isPointsAreClose(selfPerc
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
		if(this.isPointsAreClose(this.selfPerc.getPosition(),this.fieldPerc.getBall().getPosition(),2)) {
			this.selfPerc.setState(EPlayerState.HAS_BALL);
			return true;
		}
		if(!this.getClosestPlayerPoint(this.selfPerc.getPosition(), this.selfPerc.getSide(), 3).getSide().equals(this.selfPerc.getSide()) ) {
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
		if(this.isPointsAreClose(selfPerc.getPosition(),this.fieldPerc.getBall().getPosition(), 4)) {
			if(iAmTheLastPlayer()) {
				this.dash(this.fieldPerc.getBall().getPosition());
			}
			
		}else {
			//this.dash();
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
