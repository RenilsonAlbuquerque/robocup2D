package utils;

import simple_soccer_lib.utils.Vector2D;

public class PlayerUtils {

	


	public static boolean isPointsAreClose(Vector2D reference,
			Vector2D point, double margin){
		return reference.distanceTo(point) <= margin;
	}
}
