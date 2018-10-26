package simple_soccer_lib.utils;

public enum EFieldSide {
	LEFT(1), NONE(0), RIGHT(-1);
	
	private int value;
	
	EFieldSide(int value){
		this.value = value;
	}
	
	public int value(){
		return this.value;
	}

	public static EFieldSide valueOf(char side) {
		if(side == 'l' || side == 'L') return LEFT;
		if(side == 'r' || side == 'R') return RIGHT;
		return NONE;
	}
	
	public static EFieldSide valueOf(int side) {
		if(side == 1) return LEFT;
		if(side == -1) return RIGHT;
		return NONE;
	}
	
	public static EFieldSide invert(EFieldSide side){
		if(side == LEFT) return RIGHT;
		if(side == RIGHT) return LEFT;
		return NONE;
	}
}
