package simple_soccer_lib.perception;

import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.EMatchState;

public class MatchPerception {
	/*
	 * Nome dos times
	 * Tempo da partida
	 * Lado de cada time
	 * Estado da partida
	 */
	private String teamAName;
	private String teamBName;
	
	private int teamAScore;
	private int teamBScore;
	
	private int time;	
	private EMatchState state;

	public MatchPerception() {
		this.state = EMatchState.BEFORE_KICK_OFF;
		this.time = 0;
		this.teamAScore = 0;
		this.teamBScore = 0;
		this.teamAName = "";
		this.teamBName = "";
	}

	public MatchPerception(String teamAName, String teamBName, int teamAScore, int teamBScore, int time, EMatchState state) {
		this.teamAName = teamAName;
		this.teamBName = teamBName;
		this.teamAScore = teamAScore;
		this.teamBScore = teamBScore;
		this.time = time;
		this.state = state;
	}

	/**
	 * Retorna o nome do time conectado
	 * @param side lado do campo
	 */
	public String getTeamName(EFieldSide side) {
		if(side == EFieldSide.LEFT){
			return this.teamAName;
		}else if(side == EFieldSide.RIGHT){
			return this.teamBName;
		}
		return null;
	}
	
	public void setTeamName(EFieldSide side, String teamName) {
		if(side == EFieldSide.LEFT){
			this.teamAName = teamName;
		}else if(side == EFieldSide.RIGHT){
			this.teamBName = teamName;
		}
	}
		
	/**
	 * Retorna o lado do time
	 * @param teamName nome do time
	 */
	public EFieldSide getTeamSide(String teamName) {
		if(teamName.equals(teamAName)){
			return EFieldSide.LEFT;
		}else if(teamName.equals(teamBName)){
			return EFieldSide.RIGHT;
		}
		return EFieldSide.NONE;
	}

	/**
	 * Retorna o placar do time
	 * @param side lado do campo
	 */
	public int getTeamScore(EFieldSide side) {
		if(side == EFieldSide.LEFT){
			return this.teamAScore;
		}else if(side == EFieldSide.RIGHT){
			return this.teamBScore;
		}
		return -1;
	}

	public void setTeamScore(EFieldSide side, int teamScore) {
		if(side == EFieldSide.LEFT){
			this.teamAScore = teamScore;
		}else if(side == EFieldSide.RIGHT){
			this.teamBScore = teamScore;
		}
	}

	/**
	 * Retorna o tempo da partida
	 */
	public int getTime() {
		return time;
	}
	
	public void setTime(int time) {
		this.time = time;
	}
	
	/**
	 * Retorna o estado da partida
	 */
	public EMatchState getState() {
		return state;
	}
	
	public void setState(EMatchState state) {
		this.state = state;
	}

	/**
	 * Substitui valores do objeto pelos valores de "matchPerception"
	 */
	public void overwrite(MatchPerception matchPerception) {
		this.teamAName = matchPerception.getTeamName(EFieldSide.LEFT);
		this.teamBName = matchPerception.getTeamName(EFieldSide.RIGHT);
		this.teamAScore = matchPerception.getTeamScore(EFieldSide.LEFT);
		this.teamBScore = matchPerception.getTeamScore(EFieldSide.RIGHT);
		this.time = matchPerception.getTime();
		this.state = matchPerception.getState();
	}
	
	/**
	 * Retorna copia do objeto
	 */
	public MatchPerception clone(){
		return new MatchPerception(teamAName, teamBName, teamAScore, teamBScore, time, state);
	}
	
}
