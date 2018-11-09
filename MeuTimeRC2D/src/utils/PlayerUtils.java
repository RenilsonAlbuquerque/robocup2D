package utils;

import java.util.ArrayList;

import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.Vector2D;

public class PlayerUtils {

	


	public static boolean isPointsAreClose(Vector2D reference,
			Vector2D point, double margin){
		return reference.distanceTo(point) <= margin;
	}
	public static PlayerPerception searchNearbyEnemy(FieldPerception fieldPerc, EFieldSide side,Vector2D point,double margin){
		ArrayList<PlayerPerception> lp = fieldPerc.getTeamPlayers((side == EFieldSide.LEFT) ? EFieldSide.RIGHT:  EFieldSide.LEFT );
		PlayerPerception np = null ;
		if (lp != null && !lp.isEmpty()){
			double dist,temp;
			dist = lp.get(0).getPosition().distanceTo(point);
			np = lp.get(0);
			if (isPointsAreClose(np.getPosition(), point, margin))
				return np;
			for (PlayerPerception p : lp) {
				if(!p.getPosition().equals(point)) {
					if (p.getPosition() == null )
						break ;
					if (isPointsAreClose(p.getPosition(), point, margin))
						return p;
					temp = p.getPosition().distanceTo(point);
					if (temp < dist){
						dist = temp;
						np = p;
					}
				}
				
			}
		}
		return np;
	}
	public static PlayerPerception getClosestTeammatePoint(FieldPerception fieldPerc,Vector2D point, EFieldSide side, double margin){
		ArrayList<PlayerPerception> lp = fieldPerc.getTeamPlayers(side);
		PlayerPerception np = null ;
		if (lp != null && !lp.isEmpty()){
			double dist,temp;
			dist = lp.get(0).getPosition().distanceTo(point);
			np = lp.get(0);
			if (isPointsAreClose(np.getPosition(), point, margin))
				return np;
			for (PlayerPerception p : lp) {
				if(!p.getPosition().equals(point)) {
					if (p.getPosition() == null )
						break ;
					if (isPointsAreClose(p.getPosition(), point, margin))
						return p;
					temp = p.getPosition().distanceTo(point);
					if (temp < dist){
						dist = temp;
						np = p;
					}
				}
				
			}
		}
		return np;
	}
}
