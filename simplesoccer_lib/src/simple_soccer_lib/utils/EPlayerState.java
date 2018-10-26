package simple_soccer_lib.utils;

public enum EPlayerState {
	NULL 	(-1),
	DISABLE 	(0),
	STAND 		(0x01),
	KICK		(0x02),
	KICK_FAULT 	(0x04),
	GOALIE		(0x08),
	CATCH		(0x10),
	CATCH_FAULT (0x2),
	HAS_BALL	(0x441);
	
	private int value;
	
	EPlayerState(int value){
		this.value = value;
	}
	
	public static EPlayerState valueOf(int value){
		for (EPlayerState v : EPlayerState.values()) {
			if(v.value == value){
				return v;
			}
		}
		return NULL;
	}
}
