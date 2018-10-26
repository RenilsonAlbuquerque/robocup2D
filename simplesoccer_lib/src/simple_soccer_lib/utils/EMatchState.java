package simple_soccer_lib.utils;

public enum EMatchState {
	NULL					(0),
	BEFORE_KICK_OFF			(1),
	TIME_OVER				(2),
	PLAY_ON 				(3),
	KICK_OFF_LEFT 			(4),
	KICK_OFF_RIGHT 			(5),
	KICK_IN_LEFT 			(6),
	KICK_IN_RIGHT 			(7),
	FREE_KICK_LEFT 			(8),
	FREE_KICK_RIGHT 		(9),
	CORNER_KICK_LEFT 		(10),
	CORNER_KICK_RIGHT 		(11),
	GOAL_KICK_LEFT 			(12),
	GOAL_KICK_RIGHT 		(13),
	AFTER_GOAL_LEFT 		(14),
	AFTER_GOAL_RIGHT 		(15),
	DROP_BALL 				(16),
	OFFSIDE_LEFT 			(17),
	OFFSIDE_RIGHT 			(18),
	MAX 					(19),
	BACK_PASS_LEFT			(32),	// Falta: recuo para o goleiro do time left
	BACK_PASS_RIGHT			(33),	// Falta: recuo para o goleiro do time right
	FREE_KICK_FAULT_LEFT  	(34),
	FREE_KICK_FAULT_RIGHT 	(35),
	INDIRECT_FREE_KICK_LEFT (38),	// Cobrança indireta para o time left (2 toques)
	INDIRECT_FREE_KICK_RIGHT(39);	// Cobrança indireta para o time right (2 toques)
	
	private int value;

	EMatchState(int value){
		this.value = value;
	}
	
	public static EMatchState valueOf(int matchStateValue){
		EMatchState[] values = EMatchState.values();
		for (EMatchState v : values) {
			if(matchStateValue == v.value){
				return v;
			}
		}
		System.err.println("New match state found: "+matchStateValue);
		return NULL;
	}
}
