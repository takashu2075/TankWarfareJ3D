package actors;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import abstracts.Actor;
import main.World;

public class Flash extends Actor {
	TransformGroup transformGroup = new TransformGroup();

	TransformGroup cameraTransformGroup;

	double scale = 0f;
	double explodeSpeed = 0;
	double maxSize;

	boolean destroyFlg = false;

	public Flash(TransformGroup cameraTransformGroup, BranchGroup branchGroup, double explodeSpeed, double maxSize,
			Vector3f position) {
		this.cameraTransformGroup = cameraTransformGroup;

		this.explodeSpeed = explodeSpeed;
		this.maxSize = maxSize;

		this.transformGroup.addChild(branchGroup);
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		BranchGroup branchGroupTemp = new BranchGroup();
		branchGroupTemp.setCapability(BranchGroup.ALLOW_DETACH);
		branchGroupTemp.addChild(transformGroup);
		setBranchGroup(branchGroupTemp);

		Transform3D transform3D = new Transform3D();
		transform3D.setTranslation(position);
		transformGroup.setTransform(transform3D);
	}

	public void step() {
		Transform3D cameraTrans = new Transform3D();
		cameraTransformGroup.getTransform(cameraTrans);

		Vector3f cameraPos = new Vector3f();
		cameraTrans.get(cameraPos);
		Point3d cameraPoint = new Point3d(cameraPos.x, cameraPos.y, cameraPos.z);

		Transform3D currentTransform = new Transform3D();
		transformGroup.getTransform(currentTransform);

		Vector3f currentPos = new Vector3f();
		currentTransform.get(currentPos);
		Point3d currentPoint = new Point3d(currentPos.x, currentPos.y, currentPos.z);

		currentTransform.lookAt(new Point3d(currentPos.x, currentPoint.y, currentPos.z), cameraPoint,
				new Vector3d(0, 0, 1));
		currentTransform.invert();

		currentTransform.setScale(scale);
		if (destroyFlg) {
			currentTransform.setScale(maxSize);
		}

		transformGroup.setTransform(currentTransform);

		scale = scale + explodeSpeed;

		if (scale > maxSize) {
			destroy();
			destroyFlg = true;
		}
	}

	public void destroy() {
		destroyFlg = true;

		Transform3D transform3D = new Transform3D();
		transform3D.setTranslation(new Vector3f(0, -1000, 0));
		transformGroup.setTransform(transform3D);

		World.destroyActor(this);
	}
}
