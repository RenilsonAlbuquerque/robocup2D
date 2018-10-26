package simple_soccer_lib.perception;

//import simple_soccer_lib.comm.PlayerInfo;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.EPlayerState;
import simple_soccer_lib.utils.Vector2D;


public class PlayerPerception extends ObjectPerception {
	private String team;
	private int number; 
	
	private Vector2D direction;
	
	boolean goalie = false;
	private EFieldSide side;
	private EPlayerState state;
	
	/*public PlayerPerception(PlayerInfo playerInfo) {
		super();
		this.number = playerInfo.getTeamNumber();
		this.team = playerInfo.getTeamName();
		this.goalie = playerInfo.isGoalie();
		this.direction = null;
	}*/
	
	public PlayerPerception(Vector2D position, String team, int number,
			Vector2D direction, boolean goalie, EFieldSide side, EPlayerState state) {
		super(position);
		this.team = team;
		this.number = number;
		this.direction = direction;
		this.goalie = goalie;
		this.side = side;
		this.state = state;
	}

	public PlayerPerception() {
		this.number = 0; //not being able to see number
	}

	/**
	 * Retorna o nome do time
	 */
	public String getTeam(){
		return this.team;
	}
	
	public void setTeam(String team) {
		this.team = team;
	}
	
	/**
	 * Retorna o numero do jogador
	 */
	public int getUniformNumber(){
		return this.number;
	}
	
	public void setUniformNumber(int num) {
		this.number = num;
	}
	
	/**
	 * Retorna a direcao para onde o jogador esta apontado
	 */
	public Vector2D getDirection() {
		return direction;
	}
	
	public void setDirection(Vector2D dir) {
		this.direction = dir;
	}	
	
	/**
	 * Retorna verdadeiro se o jogador for um goleiro
	 */
	public boolean isGoalie() {
		return goalie;
	}
	
	public void setGoalie(boolean isGoalie) {
		this.goalie = isGoalie;
	}
	
	/**
	 * Retorna a copia do objeto
	 */
	public PlayerPerception copy() {
		return new PlayerPerception(super.getPosition(), team, number,
				direction, goalie, side, state);
	}

	/**
	 * Retorna o lado do campo que esta defendendo
	 */
	public EFieldSide getSide() {
		return side;
	}

	public void setSide(EFieldSide side) {
		this.side = side;
	}

	/**
	 * Retorna o estado do jogador
	 */
	public EPlayerState getState() {
		return state;
	}

	public void setState(EPlayerState state) {
		this.state = state;
	}

	@Override
	public String toString(){
		return "Team:	"+team+
				"\nNumber:	"+number+
				"\nGoalie:	"+goalie+
				"\nDirection:	"+direction+
				"\nSide:	"+side;
	}
}