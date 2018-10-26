package simple_soccer_lib.comm;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;

/**
 * This class implements a monitor that just parses the positions of the players and the ball.
 * 
 */
public class MonitorConnection {
	private static final int  MSG_SIZE = 2052; // Size of socket buffer

	private DatagramSocket socket; // socket to communicate with server
	private InetAddress    host;   // server address
	private int            port;   // server port
	
	boolean isActive;
	long lastMessage;
	
	MonitorMessageParser parser;
	
	public MonitorConnection(InetAddress serverAddress) {
		this.host = serverAddress;
		this.port = 6000;
	}

	public void connect() throws IOException {
		this.isActive = false;
		this.socket = new DatagramSocket();
		
		byte[] buffer = new byte[MSG_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE);

		send("(dispinit)"); //connect to the server

		this.socket.setSoTimeout(2000);
		
		this.socket.receive(packet); //esta primeira mensagem � descartada
		this.lastMessage = System.currentTimeMillis();
		this.isActive = true;
		
		this.socket.setSoTimeout(1); //for the "update()"
		
		this.port = packet.getPort();		
		this.parser = new MonitorMessageParser();
	}
	
	public void disconnect() {
		if (this.isActive) {
			this.socket.close();
			this.isActive = false;
		}
	}
	
	// This destructor closes socket
	public void finalize() {
		if (this.isActive) {
			this.socket.close();
			this.isActive = false;
		}
	}
	
	public boolean isActive() {
		return this.isActive;
	}

	// This is main method, that should be called regularly
	public boolean update(FieldPerception field, MatchPerception match) {
		if (!this.isActive) {
			return false;
		}
		
		byte[] buffer = new byte[MSG_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE);

		try {
			socket.receive(packet);
		
		} catch (SocketTimeoutException st) {
			if (System.currentTimeMillis() > this.lastMessage + 1000) {
				System.err.println("MonitorConnection: too much time without message, auto-disconect.");
				this.disconnect();
			}
			return false;
		
		} catch (IOException e) {
			System.err.println("MonitorConnection: error receiving data, auto-disconect.");
			System.err.println("MonitorConnection: " + e.getMessage());
			this.disconnect();
			return false;
		}
		
		this.lastMessage = System.currentTimeMillis();
		
		boolean hasPerceptions = parser.parse(buffer); 
		
		if (hasPerceptions) {
			field.overwrite( parser.getFieldPerception() );		
			match.overwrite( parser.getMatchPerception() );
			return true;
		}

		return false;
	}

	// This function sends message to the server by socket
	private void send(String message) {
		byte[] buffer = Arrays.copyOf(message.getBytes(), MSG_SIZE);
		try {
			DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE, host, port);
			socket.send(packet);
		} catch (IOException e) {
			System.err.println("socket sending error " + e);
		}

	}


}
