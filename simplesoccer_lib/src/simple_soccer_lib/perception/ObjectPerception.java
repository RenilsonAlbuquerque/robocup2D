package simple_soccer_lib.perception;

import simple_soccer_lib.utils.Vector2D;

public class ObjectPerception {
	private Vector2D position;
	//private Vector2D direction; // TODO: direção do movimento, para versões futuras

	public ObjectPerception(Vector2D position){
		this.position = position;
	}
	
	public ObjectPerception(){		
	}
	
	/**
	 * Retorna posicao do objeto no campo
	 */
	public Vector2D getPosition() {
		return position;
	}

	public void setPosition(Vector2D position) {
		this.position = position;
	}
	
}