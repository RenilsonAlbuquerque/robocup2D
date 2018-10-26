package simple_soccer_lib.perception;

import java.util.ArrayList;

import simple_soccer_lib.utils.EFieldSide;

/**
 * Guarda apenas os objetos m�veis do campo: jogadores e bola.
 *
 */
public class FieldPerception {
	private ArrayList<PlayerPerception> playersA;
	private ArrayList<PlayerPerception> playersB;
	private ObjectPerception ball;

	public FieldPerception() {
		this.playersA = new ArrayList<PlayerPerception>();
		this.playersB = new ArrayList<PlayerPerception>();
		this.ball = new ObjectPerception();
	}
	
	public FieldPerception(ArrayList<PlayerPerception> playersA, ArrayList<PlayerPerception> playersB, ObjectPerception ball) {
		this.playersA = playersA;
		this.playersB = playersB;
		this.ball = ball;
	}

	/**
	 * Retorna lista de percepcoes de todos os jogadores em campo
	 */
	public ArrayList<PlayerPerception> getAllPlayers() {
		ArrayList<PlayerPerception> all = new ArrayList<>();
		all.addAll(playersA);
		all.addAll(playersB);
		return all;
	}
		
	/**
	 * Retorna percepcao da bola
	 */
	public ObjectPerception getBall() {
		return ball;
	}

	public void setBall(ObjectPerception ball) {
		this.ball = ball;
	}

	/**
	 * Substitui valores do objeto pelos valores de "other"
	 */
	public void overwrite(FieldPerception other) {
		this.ball = other.ball;
		setTeamPlayers(EFieldSide.LEFT, other.playersA);
		setTeamPlayers(EFieldSide.RIGHT, other.playersB);
	}

	
	/**
	 * Retorna lista de percepcoes dos jogadores de um time
	 */
	public ArrayList<PlayerPerception> getTeamPlayers(EFieldSide side){
		if(side == EFieldSide.LEFT)
			return playersA;
		else if (side == EFieldSide.RIGHT)
			return playersB;
		else 
			return null;
	}
	
	public void setTeamPlayers(EFieldSide side, ArrayList<PlayerPerception> newslp){	
		ArrayList<PlayerPerception> lpTemp;
		if(side == EFieldSide.LEFT){
			lpTemp = this.playersA;
		}else if(side == EFieldSide.RIGHT){
			lpTemp = this.playersB;
		}else{
			return;
		}
		
		lpTemp.clear();
		for (PlayerPerception pp : newslp) {
			if(pp != null && pp.getPosition() != null)
				lpTemp.add(pp);
		}
	}
	
	/**
	 * Retorna percepcao do jogador de um time
	 * @param uniformNumber numero do jogador
	 */
	public PlayerPerception getTeamPlayer(EFieldSide side, int uniformNumber){
		try{
			if(side == EFieldSide.LEFT)
				return playersA.get(uniformNumber-1);	
			else if(side == EFieldSide.RIGHT)
				return playersB.get(uniformNumber-1);	
		}catch (Exception e) {
			e.printStackTrace(); //caso ocorra erro de index (bug do servidor?)
		}		
		return null;
	}
	
	/**
	 * Retorna copia do objeto
	 */
	public FieldPerception copy() {
		return new FieldPerception(playersA, playersB, ball);
	}
	
}