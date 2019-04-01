package actors;

import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.BoxShape;

import abstracts.Actor;
import main.World;
import utils.PerticleUtils;

public class Explosion extends Actor {
	TransformGroup targetCameraTransformGroup;

	Shape3D explosionShape;
	Shape3D smokeShape;

	TransformGroup transformGroup = new TransformGroup();
	TransformGroup smokeTansformGroup = new TransformGroup();
	List<TransformGroup> debriTransformGroups = new ArrayList<TransformGroup>();

	double currentScale = 0;
	double initialScale = 0;
	double explodeSpeed = 0;
	double duration = 0;
	double stepCount = 0;

	boolean destroyFlg = false;

	BranchGroup debri;

	public Explosion(TransformGroup targetCameraTransformGroup, Shape3D explosionShape, Shape3D smokeShape,
			Shape3D debriShape, double initialScale, double explodeSpeed, int duration, Vector3f position) {
		this.targetCameraTransformGroup = targetCameraTransformGroup;

		this.explosionShape = explosionShape;
		this.smokeShape = smokeShape;

		this.initialScale = initialScale;
		this.explodeSpeed = explodeSpeed;
		this.duration = duration;

		int NUM_OF_DEBRIS = 10;
		for (int i = 0; i < NUM_OF_DEBRIS; i++) {
			BoxShape boxShape = new BoxShape(new Vector3f(1, 1, 1));

			Transform3D debriTransform3D = new Transform3D();
			Vector3f debriPos = new Vector3f(position.x, position.y + 10, position.z);
			debriTransform3D.setTranslation(debriPos);

			int DEBRI_MASS = 1;
			ActorComponent actorComp = new ActorComponent(debriShape.cloneTree(), boxShape, debriTransform3D,
					DEBRI_MASS);

			addActorComp(actorComp);
		}

		transformGroup.addChild(this.explosionShape);
		transformGroup.addChild(this.smokeShape);

		Transform3D transform3D = new Transform3D();
		transform3D.setTranslation(position);

		transformGroup.setTransform(transform3D);

		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		getBranchGroup().addChild(transformGroup);

		currentScale = initialScale;

		updScale();
	}

	public void step() {
		Transform3D cameraTrans = new Transform3D();
		targetCameraTransformGroup.getTransform(cameraTrans);

		Vector3f cameraPos = new Vector3f();
		cameraTrans.get(cameraPos);

		PerticleUtils.loolAtCamera(transformGroup, targetCameraTransformGroup);

		currentScale = currentScale + explodeSpeed;

		updScale();
		updExplosionTransparency();
		updSmokeTransparency();

		if (stepCount > duration) {
			destroy();
		} else {
			stepCount++;
		}
	}

	public void destroy() {
		World.destroyActor(this);
	}

	public void loolAtCamera(TransformGroup transformGroup) {
		Transform3D newTransform = new Transform3D();
		transformGroup.getTransform(newTransform);

		Vector3f currentPos = new Vector3f();
		newTransform.get(currentPos);
		Point3d currentPoint = new Point3d(currentPos.x, currentPos.y, currentPos.z);

		Transform3D transform3D = new Transform3D();
		targetCameraTransformGroup.getTransform(transform3D);

		Vector3f cameraPos = new Vector3f();
		transform3D.get(cameraPos);
		Point3d cameraPoint = new Point3d(cameraPos.x, cameraPos.y, cameraPos.z);

		newTransform.lookAt(new Point3d(currentPos.x, currentPoint.y, currentPos.z), cameraPoint,
				new Vector3d(0, 0, 1));
		newTransform.invert();

		transformGroup.setTransform(newTransform);
	}

	private void updScale() {
		Transform3D transform3D = new Transform3D();
		transformGroup.getTransform(transform3D);
		transform3D.setScale(currentScale);

		transformGroup.setTransform(transform3D);
	}

	private void updExplosionTransparency() {
		float transparency = ((float) stepCount / ((float) duration / 5));

		TransparencyAttributes transparencyAttributes = new TransparencyAttributes();
		transparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);
		transparencyAttributes.setTransparency(transparency);

		explosionShape.getAppearance().setTransparencyAttributes(transparencyAttributes);
	}

	private void updSmokeTransparency() {
		float transparency = ((float) stepCount / ((float) duration));

		TransparencyAttributes transparencyAttributes = new TransparencyAttributes();
		transparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);
		transparencyAttributes.setTransparency(transparency);

		smokeShape.getAppearance().setTransparencyAttributes(transparencyAttributes);
	}
}
