package utils;

import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;

public class ConversionUtils {
	public static double getTargetDirectionWorld(Vector3f targetPos, Vector3f playerPosition) {
		Vector3f heading = new Vector3f(targetPos.x - playerPosition.x, targetPos.y - playerPosition.y,
				targetPos.z - playerPosition.z);
		heading.normalize();

		double targetDir = 0;
		if (heading.z >= 0) {
			targetDir = -Math.atan(heading.x / heading.z);
		} else {
			if (heading.x >= 0) {
				targetDir = -Math.atan(1.0f / 0.0f) + Math.atan(heading.z / heading.x);
			} else {
				targetDir = Math.atan(1.0f / 0.0f) + Math.atan(heading.z / heading.x);
			}
		}

		return targetDir;
	}

	public static double getTargetDirectionLocal(double playerDirection, double targetDirection) {
		double targetDirectionLocal = targetDirection - playerDirection;
		if (targetDirectionLocal >= 2 * Math.atan(1.0f / 0.0f)) {
			targetDirectionLocal = -4 * Math.atan(1.0f / 0.0f) + targetDirectionLocal;
		}
		if (targetDirectionLocal < -2 * Math.atan(1.0f / 0.0f)) {
			targetDirectionLocal = 4 * Math.atan(1.0f / 0.0f) + targetDirectionLocal;
		}

		return targetDirectionLocal;
	}

	public static Vector3f convPosWorld2Local(Transform transform, Vector3f worldPos) {
		Vector3f localPos = new Vector3f(worldPos.x, worldPos.y, worldPos.z);

		transform.inverse();
		transform.transform(localPos);

		return localPos;
	}

	public static Vector3f convPosLocal2World(Transform transform, Vector3f localPos) {
		Vector3f worldPos = new Vector3f(localPos.x, localPos.y, localPos.z);

		transform.transform(worldPos);

		return worldPos;
	}

	public static Vector3f convVecWorld2Local(Transform transform, Vector3f worldVec) {
		Vector3f localVec = new Vector3f(worldVec.x, worldVec.y, worldVec.z);

		transform.inverse();
		transform.basis.transform(localVec);

		return localVec;
	}

	public static Vector3f convVecLocal2World(Transform transform, Vector3f localVec) {
		Vector3f worldVec = new Vector3f(localVec.x, localVec.y, localVec.z);

		transform.basis.transform(worldVec);

		return worldVec;
	}
}
