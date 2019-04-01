package utils;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class PerticleUtils {
	public static void loolAtCamera(TransformGroup transformGroup, TransformGroup cameraTransformGroup) {
		Transform3D newTransform = new Transform3D();
		transformGroup.getTransform(newTransform);

		Vector3f currentPos = new Vector3f();
		newTransform.get(currentPos);
		Point3d currentPoint = new Point3d(currentPos.x, currentPos.y, currentPos.z);

		Transform3D transform3D = new Transform3D();
		cameraTransformGroup.getTransform(transform3D);

		Vector3f cameraPos = new Vector3f();
		transform3D.get(cameraPos);
		Point3d cameraPoint = new Point3d(cameraPos.x, cameraPos.y, cameraPos.z);

		newTransform.lookAt(new Point3d(currentPos.x, currentPoint.y, currentPos.z), cameraPoint,
				new Vector3d(0, 0, 1));
		newTransform.invert();

		transformGroup.setTransform(newTransform);
	}
}
