package utils;

import javax.vecmath.Vector3f;

public class DirectionUtils {
	public static final float PI = 3.14159265358979323846f;
	public static final float PI_2 = 1.57079632679489661923f;
	public static final float PI_4 = 0.785398163397448309616f;

	public static double getDirToTargetX(Vector3f targetLocalPos) {
		double angle = Math.atan(targetLocalPos.x / targetLocalPos.z);

		if (targetLocalPos.z < 0) {
			if (targetLocalPos.x < 0) {
				angle = -PI + angle;
			} else {
				angle = PI + angle;
			}
		}

		return angle;
	}

	public static double getDirToTargetY(Vector3f targetLocalPos) {
		double angle = Math.atan(targetLocalPos.y / targetLocalPos.z);

		if (targetLocalPos.z < 0) {
			if (targetLocalPos.y < 0) {
				angle = -PI + angle;
			} else {
				angle = PI + angle;
			}
		}

		return angle;
	}
}
