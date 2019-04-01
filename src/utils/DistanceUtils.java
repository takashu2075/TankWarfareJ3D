package utils;

import javax.vecmath.Vector3f;

public class DistanceUtils {
	public static double getDistToTarget(Vector3f targetLocalPos) {
		double distanceXZ = Math.sqrt((targetLocalPos.x * targetLocalPos.x) + (targetLocalPos.z * targetLocalPos.z));
		return Math.sqrt((distanceXZ * distanceXZ) + (targetLocalPos.y * targetLocalPos.y));
	}
}
