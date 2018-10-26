package simple_soccer_lib.comm;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import simple_soccer_lib.utils.EFieldSide;

/**
 * Adaptado do client krislet.
 * 
 */
public class PlayerConnection {

	private static final int  MSG_SIZE = 4096; // size of socket buffer

	private DatagramSocket socket; // UDP socket to communicate with server
	private InetAddress host;      // server address
	private int port;              // server port
	
	private String team;      // team name
	private boolean isActive; // false only after sending a 'bye' command
	private int  initialUniformNumber;
	private char initialSide;
	
	public PlayerConnection(InetAddress serverAddress, int serverPort, String teamName) {
		host = serverAddress;
		port = serverPort;
		team = teamName;
	}
	
	public EFieldSide getInitialSide() {
		return EFieldSide.valueOf(initialSide);
	}
	
	public int getInitialUniformNumber() {
		return initialUniformNumber;
	}
	
	/**
	 * Conecta com o servidor
	 **/
	public void connect(boolean isGoalie) throws IOException {
		isActive = false;
		socket = new DatagramSocket();
		
		byte[] buffer = new byte[MSG_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE);

		sendInitialCommand(isGoalie);
		
		socket.receive(packet);
		parseInitialResponse(new String(buffer));
		port = packet.getPort();
		isActive = true;
	}
	
	/**
	 * Verifica se o agente esta conectado com o servidor
	 **/
	public boolean isActive() {
		return isActive;
	}
	
	/**
	 * Finaliza a conexao
	 **/
	public void finalize() {
		if (isActive) {
			isActive = false;
			socket.close();
		}
	}
	
	/**
	 * Envia comando inicial para conectar com o servidor
	 **/
	private void sendInitialCommand(boolean isGoalie) {
		send("(init " + team + " (version 9) "+(isGoalie? "(goalie)":"")+")");
	}
	
	/**
	 * Envia comando para posicionar o agente no campo
	 **/
	public void move(double x, double y) {
		send("(move " + Double.toString(x) + " " + Double.toString(y) + ")");
	}

	/**
	 * Envia comando para girar o corpo do agente (A visao acompanha o giro do corpo)
	 **/
	public void turn(double moment) {
		send("(turn " + Double.toString(moment) + ")");
	}

	/**
	 * Envia comando para girar a visao do agente
	 **/
	public void turn_neck(double moment) {
		send("(turn_neck " + Double.toString(moment) + ")");
	}

	/**
	 * Envia comando para correr
	 **/
	public void dash(double power) {
		send("(dash " + Double.toString(power) + ")");
	}

	/**
	 * Envia comando para chutar
	 **/
	public void kick(double power, double direction) {
		send("(kick " + Double.toString(power) + " " + Double.toString(direction) + ")");
	}

	/**
	 * Envia comando para troca de mensagens entre os agentes
	 **/
	public void say(String message) {
		send("(say " + message + ")");
	}
	
	/**
	 * Envia comando para o goleiro agarrar a bola
	 **/
	public void catchBall(double angle) {
		send("(catch " + Double.toString(angle) + ")");
	}

	/**
	 * This function sends chage_view command to the server
	 **/
	public void changeView(String angle, String quality) {
		send("(change_view " + angle + " " + quality + ")");
	}

	/**
	 * Envia comando para desconectar o agente
	 **/
	public void bye() {
		send("(bye)");
		isActive = false;
	}
	
	/**
	 * Envia mensagens para o servidor
	 **/
	private void send(String message) {
		byte[] buffer = Arrays.copyOf(message.getBytes(), MSG_SIZE);
		try {
			DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE, host, port);
			socket.send(packet);
		} catch (IOException e) {
			this.isActive = false;
			System.err.println("socket sending error " + e);
		}
	}
	
	/**
	 * Recebe mensagens do servidor
	 **/
	private String receive() {
		byte[] buffer = new byte[MSG_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE);
		try {
			socket.receive(packet);
		} catch (SocketException e) {
			System.err.println("shutting down...");
			e.printStackTrace();
			isActive = false;
		} catch (IOException e) {
			System.err.println("socket receiving error " + e);
			e.printStackTrace();
		}
		return new String(buffer);
	}
	
	/**
	 * Converte as informacoes iniciais da mensagem de conexao enviada pelo servidor
	 **/
	protected void parseInitialResponse(String message) throws IOException {
		Matcher m = Pattern.compile("^\\(init\\s([lr])\\s(\\d{1,2})\\s(\\w+)\\).*$").matcher(message);
		if (!m.matches()) {
			throw new IOException(message);
		}
		initialSide = m.group(1).charAt(0);  // l ou r
		initialUniformNumber = Integer.parseInt(m.group(2));
		System.out.println("Status inicial da partida: " + m.group(3)); 
	}
	
	/** 
	 * Recebe mensagens do servidor com percepções (imperfeitas) e atualiza informações do 
	 * player e dos objetos do campo. Retorna true/false para indicar se as percepções foram 
	 * atualizadas.
	 * 
	 * (Atenção: não está mais sendo chamado, após a criação do MonitorCommunicator que recebe
	 * percepções perfeitas por outra conexão).
	 */
	/*public boolean update(PlayerPerception self, FieldPerception field, MatchPerception match) {
		isActive = socket.isConnected();
		if(isActive){
			String message = receive();
	
			Matcher m = Patterns.MESSAGE_PATTERN.matcher(message);
			if (!m.matches()) {
				throw new Error("Invalid message: " + message);
			}
			
			if (m.group(1).equals("see")) {
				VisualInfo info = new VisualInfo(message);
				info.parse();
	
				if (self != null && field != null) {
					PerceptionParser.parse(self, field, match, info);
				}
				
				return true;						
			} 
		}
		return false;
	}*/
}
