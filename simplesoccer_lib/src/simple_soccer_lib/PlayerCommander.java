package simple_soccer_lib;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import simple_soccer_lib.comm.MonitorConnection;
import simple_soccer_lib.comm.PlayerConnection;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.Vector2D;

/**
 * Esta classe serve para facilitar o controle de um jogador no servidor da RoboCup
 * 2D simulada. Para isso, ele encapsula toda a parte de comunica��o com o servidor.
 * 
 * Ela oferece percep��es do jogo (vindas do simulador) na forma de objetos de alto 
 * n�vel e oferece m�todos de alto n�vel para enviar as a��es de um jogador.
 *  
 * As percep��es funcionam na forma de "consumo". Depois de lida uma vez, s� haver�
 * uma nova percep��o dispon�vel quando o servidor enviar nova mensagem. Se for 
 * requisitada uma percep��o no intervalo, ser� retornando null.
 * 
 */
public class PlayerCommander extends Thread {
	private static final int WAIT_TIME = 5; //testar diferentes valores
	private static final int SIMULATOR_CYCLE = 100;

	private PlayerConnection communicator;
	private MonitorConnection perceiver;
	
	private boolean isGoalie;
	private String teamName;
	private int uniformNumber;
	private EFieldSide fieldSide; //no futuro: 1) incluir no player perception 
	                              //    ou     2) abstrair (para enviar/receber comandos como se atacasse para a direita)
	
	private PlayerPerception self;  //as informacoes sobre o pr�prio jogador comandado
	private boolean selfConsumed;   //MANTER apenas se for retornar a c�pia
	private FieldPerception field;  //as informacoes sobre os objetos m�veis do campo: bola e outros jogadores
	private boolean fieldConsumed;
	private MatchPerception match; 	// as informacoes sobre a partida: nome dos times, placar, lado do campo, tempo e estado do jogo
	private boolean matchConsumed;
		
	//private Vector2D viewDirection; //dire��o absoluta da vis�o, necess�ria para algumas a��es --> agora, acessa direto de "self"
	
	private long nextActionTime;
		
	/**
	 * Recebe o endere�o do servidor e a posi��o inicial do jogador, antes do kick-off.
	 */
	public PlayerCommander(String teamName, String host, int port, boolean isGoalie) throws UnknownHostException {
		InetAddress address = InetAddress.getByName(host);
		
		this.teamName = teamName;
		this.communicator = new PlayerConnection(address, port, teamName); // InetAddress lan�a exce��o
		this.perceiver = new MonitorConnection(address);
		
		this.isGoalie = isGoalie;
		this.self = new PlayerPerception();
		this.field = new FieldPerception();
		this.match = new MatchPerception();
		this.selfConsumed = this.fieldConsumed = this.matchConsumed = true;
		this.start();
	}
	
	/**
	 * Retorna nome do time
	 */
	public String getTeamName() {
		return this.teamName;
	}

	/**
	 * Retorna lado do time
	 */
	public EFieldSide getFieldSide() {
		return this.fieldSide; //TODO: remover (pode pegar em self, sempre retorna o lado inicial, mas nao troca no 2o tempo)
	}
	
	/**
	 * Retorna o n�mero do jogador
	 */
	public int getUniformNumber() {
		return this.communicator.getInitialUniformNumber(); //TODO: remover? (pegar em self, não muda)
	}
	
	public void run() {
		try {
			this.communicator.connect(this.isGoalie); //conecta com o servidor, no modo jogador ou goleiro
			this.uniformNumber = this.communicator.getInitialUniformNumber();
			this.fieldSide = this.communicator.getInitialSide();			
			this.perceiver.connect();    //conecta com o servidor, no modo monitor					
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.nextActionTime = System.currentTimeMillis();
			
		while (isActive()) {			
					
			synchronized (this) {
				// 1. Recebe as perce��es e faz o parsing delas
				// 2. Constroi uma representa��o de alto n�vel das percep��es
				//    2.1 Calcular a posi��o e orienta��o absoluta do jogador (self) 
				//    2.2 Calcular as posi��es absolutas dos objetos m�veis do campo (field)
				//	  2.3 Calcular as informa��es da partida (match)

				//TODO: problema: cria novas inst�ncias a cada itera��o -- problema de mem�ria
				//      ideia 1: manter uma inst�ncia fixa de cada um no monitor, e copiar para os atributos desta classe
				//      ideia 2: manter os atributos desta classe fixos (final?) e s� retornar c�pias
				FieldPerception newField = (this.fieldConsumed) ? new FieldPerception() : this.field;
				MatchPerception newMatch = (this.matchConsumed) ? new MatchPerception() : this.match;
														
				boolean hasNewPerceptions = this.perceiver.update(newField, newMatch);
				
				if (hasNewPerceptions) {
					this.field = newField;
					this.match = newMatch;
					this.self = this.field.getTeamPlayer(this.fieldSide, this.uniformNumber);
					if (this.self != null) {   //may happen due to some bug in the server
						this.self.setGoalie(this.isGoalie);
						this.selfConsumed = false;
					} else {
						this.selfConsumed = true;
					}
					this.fieldConsumed = false;
					this.matchConsumed = false;
					
					//this.viewDirection = self.getDirection(); //TODO: remover?
				}
			}
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		this.communicator.bye();
		System.out.println("PlayerCommander finished.");
	}
	
	/**
	 * Verifica se o agente esta conectado
	 */
	public boolean isActive() {
		//to detect if connection with the server was closed
		//queries only the perceiver because it sends messages regularly 
		return perceiver.isActive();
	}
	
	/**
	 * Retorna percepcao do agente.
	 * Este metodo nao aguarda novas percepcoes.
	 */
	synchronized public PlayerPerception perceiveSelf() {
		if (selfConsumed) {
			return null;
		}
		PlayerPerception s = this.self;
		//self = null;
		selfConsumed = true;
		return s; //.copy();
	}
	
	/**
	 * Retorna percepcao do agente.
	 * Este metodo aguarda novas percepcoes.
	 */
	public PlayerPerception perceiveSelfBlocking() {
		PlayerPerception s = perceiveSelf();
		while (s == null) {   //TODO: atencao: risco de live lock, ideia: criar um semaforo apenas para as percep��es
			try {
				Thread.sleep(WAIT_TIME/2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			s = perceiveSelf();
		}
		return s;  
	}
	
	/**
	 * Retorna percepcoes do campo.
	 * Este metodo nao aguarda novas percepcoes.
	 */
	synchronized public FieldPerception perceiveField() {
		if (fieldConsumed) {
			return null;
		}
		FieldPerception f = this.field;
		//field = null;
		fieldConsumed = true;
		return f; //.copy();
	}
	
	/**
	 * Retorna percepcao do campo.
	 * Este metodo aguarda novas percepcoes.
	 */
	public FieldPerception perceiveFieldBlocking() {
		FieldPerception f = perceiveField();
		while (f == null) {   //atencao: risco de live lock, ideia: criar um semaforo apenas para as percep��es
			try {
				Thread.sleep(WAIT_TIME/2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			f = perceiveField();
		}
		return f;
	}
	
	/**
	 * Retorna percepcao da partida.
	 * Este metodo nao aguarda novas percepcoes.
	 */
	synchronized public MatchPerception perceiveMatch() {
		if (matchConsumed) {
			return null;
		}
		MatchPerception m = this.match;
		//field = null;
		matchConsumed = true;
		return m; //.copy();
	}
	
	/**
	 * Retorna percepcao da partida.
	 * Este metodo aguarda novas percepcoes.
	 */
	public MatchPerception perceiveMatchBlocking() {
		MatchPerception m = perceiveMatch();
		while (m == null) {   //atencao: risco de live lock, ideia: criar um semaforo apenas para as percep��es
			try {
				Thread.sleep(WAIT_TIME/2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			m = perceiveMatch();
		}
		return m;
	}
	
	/**
	 * O agente gira o corpo (e a vis�o) de modo que o futuro eixo de visao forme 
	 * o dado angulo em relacao ao eixo de visual atual.
	 */
	synchronized public boolean doTurn(double degreeAngle) {
		if (System.currentTimeMillis() < nextActionTime) {
			return false;
		}
		if (degreeAngle > 180.0) {
			degreeAngle -= 360;
		} else if (degreeAngle < -180.0) {
			degreeAngle += 360; 
		}
		communicator.turn(degreeAngle);		
		nextActionTime = System.currentTimeMillis() + SIMULATOR_CYCLE;		
		return true;
	}
	
	public void doTurnBlocking(double degreeAngle) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		doTurn(degreeAngle);
	}

	/**
	 * Faz o agente girar para uma direcao.
	 * Este metodo nao aguarda a janela de envio e por isso pode nao ser executado.
	 */
	synchronized public boolean doTurnToDirection(Vector2D orientation) {
		double angle = orientation.angleFrom(this.self.getDirection());
		return doTurn(angle);
	}
	
	/**
	 * Faz o agente girar para uma direcao.
	 * Este metodo garante o envio do comando e, por isso, pode bloquear a execu��o por um tempo 
	 * de at� SIMULATOR_CYCLE (100ms) para ser executado.
	 */
	public void doTurnToDirectionBlocking(Vector2D orientation) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		doTurnToDirection(orientation);
	}
	
	/**
	 * Faz o agente girar para um ponto no campo.
	 * Este metodo nao aguarda a janela de envio e por isso pode nao ser executado.
	 */
	synchronized public boolean doTurnToPoint(Vector2D referencePoint) {				
		Vector2D newDirection = referencePoint.sub(this.self.getPosition());
		double angle = newDirection.angleFrom(this.self.getDirection());
		return doTurn(angle);
	}

	/**
	 * Faz o agente girar para um ponto no campo.
	 * Este metodo garante a execucao do comando e por isso pode demorar para ser executado.
	 */
	public void doTurnToPointBlocking(Vector2D referencePoint) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		doTurnToPoint(referencePoint);
	}
		
	/**
	 * Faz o agente andar ou correr de acordo com um esforco.
	 * Este metodo nao aguarda a janela de envio e por isso pode nao ser executado.
	 * @param intensity esforco entre 0 e 100
	 */
	synchronized public boolean doDash(double intensity) {
		if (System.currentTimeMillis() < nextActionTime) {
			return false;
		}
		communicator.dash(intensity); 
		nextActionTime += SIMULATOR_CYCLE;
		return true;
	}
	
	/**
	 * Faz o agente andar ou correr de acordo com um esforco.
	 * Este metodo garante a execucao do comando e por isso pode demorar para ser executado.
	 * @param intensity esforco entre 0 e 100
	 */
	public void doDashBlocking(double intensity) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		doDash(intensity);
	}
	
	/**
	 * Faz o agente chutar, tocar ou conduzir a bola de acordo com um esforco e direcao relativa (angulo em relacao a direcao de movimento/visao do jogador).
	 * Este metodo nao aguarda a janela de envio e por isso pode nao ser executado.
	 * @param intensity esforco entre 0 e 100
	 * @param relativeAngle angulo do chute
	 */
	synchronized public boolean doKick(double intensity, double relativeAngle) {
		if (System.currentTimeMillis() < nextActionTime) {
			return false;
		}
		communicator.kick(intensity, relativeAngle); 
		nextActionTime += SIMULATOR_CYCLE;
		return true;
	}
	
	/**
	 * Faz o agente chutar, tocar ou conduzir a bola de acordo com um esforco e direcao relativa (angulo em relacao a direcao de movimento/visao do jogador).
	 * Este metodo garante a execucao do comando e por isso pode demorar para ser executado.
	 * @param intensity esforco entre 0 e 100
	 * @param relativeAngle angulo do chute
	 */
	public void doKickBlocking(double intensity, double relativeAngle) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		doKick(intensity, relativeAngle);
	}

	/**
	 * Faz o agente chutar, tocar ou conduzir a bola de acordo com um esforco e direcao absoluta (ponto no campo).
	 * Este metodo nao aguarda a janela de envio e por isso pode nao ser executado.
	 * @param intensity esforco entre 0 e 100
	 */
	synchronized public boolean doKickToDirection(double intensity, Vector2D direction) {
		if (System.currentTimeMillis() < nextActionTime) {
			return false;
		}
		double angle = direction.angleFrom(self.getDirection());
		communicator.kick(intensity, angle);
		nextActionTime += SIMULATOR_CYCLE;
		return true;
	}

	/**
	 * Faz o agente chutar, tocar ou conduzir a bola de acordo com um esforco e uma direcao absoluta na forma de um vetor.
	 * Este metodo garante a execucao do comando e por isso pode demorar para ser executado.
	 * @param intensity esforco entre 0 e 100
	 */
	public void doKickToDirectionBlocking(double intensity, Vector2D direction) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		doKickToDirection(intensity, direction);
	}

	synchronized public boolean doKickToPoint(double intensity, Vector2D targetPoint) {
		Vector2D newDirection = targetPoint.sub(self.getPosition()); //TODO: check -- usar posicao da bola?
		return doKickToDirection(intensity, newDirection);
	}

	public void doKickToPointBlocking(double intensity, Vector2D targetPoint) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		doKickToPoint(intensity, targetPoint);
	}

	/**
	 * Move o jogador para a coordenada dada. Isso s� pode ser feito em alguns
	 * momentos do jogo (por exemplo: no estado "prepare to kickoff").
	 * Este metodo nao aguarda a janela de envio e por isso pode nao ser executado.
	 */
	synchronized public boolean doMove(double x, double y) {
		if (System.currentTimeMillis() < nextActionTime) {  //TESTAR tamb�m se PODE, dependendo do status da partida
			return false;
		}
		communicator.move(x, y); 
		nextActionTime += SIMULATOR_CYCLE;
		return true;
	}
	
	/**
	 * Move o jogador para a coordenada dada. Isso s� pode ser feito em
	 * alguns momentos do jogo (por exemplo: antes do kickoff).
	 * Este metodo garante a execucao do comando e por isso pode demorar para ser executado
	 */
	public void doMoveBlocking(double x, double y) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		doMove(x, y);
	}
	
	/**
	 * Comando exclusivo do goleiro para agarrar a bola (entre -45 e 45 graus).
	 * Este metodo nao aguarda a janela de envio e por isso pode nao ser executado
	 */
	synchronized public boolean doCatch(double angle) {
		if (System.currentTimeMillis() < nextActionTime) {  //TESTAR tamb�m se PODE, dependendo do status da partida
			return false;
		}
		communicator.catchBall(angle); 
		nextActionTime += SIMULATOR_CYCLE;
		return true;
	}
	
	/**
	 * Comando exclusivo do goleiro para agarrar a bola (entre -45 e 45 graus).
	 * Este metodo garante a execucao do comando e por isso pode demorar para ser executado
	 */
	public void doCatchBlocking(double angle) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		doCatch(angle);
	}
	
	/**
	 * Desconectar agente
	 * */
	public void disconnect() {
		communicator.bye();
	}
}

