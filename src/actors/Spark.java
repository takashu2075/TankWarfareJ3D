package actors;

import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3f;

import abstracts.Actor;
import main.World;

public class Spark extends Actor {
	Shape3D sparkShape;

	TransformGroup transformGroup = new TransformGroup();

	double currentScale = 0;
	double initialScale = 0;
	double explodeSpeed = 0;
	double duration = 0;
	double stepCount = 0;

	boolean destroyFlg = false;

	public Spark(Shape3D explosionShape, double initialScale, double explodeSpeed, int duration, Vector3f position) {
		this.sparkShape = explosionShape;

		this.initialScale = initialScale;
		this.explodeSpeed = explodeSpeed;
		this.duration = duration;

		transformGroup.addChild(this.sparkShape);
		Transform3D transform3D = new Transform3D();
		transform3D.setTranslation(position);
		transformGroup.setTransform(transform3D);
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		getBranchGroup().addChild(transformGroup);

		updateScale();
	}

	public void step() {
		currentScale = currentScale + explodeSpeed;

		updateScale();
		updateTransparency();

		if (stepCount > duration) {
			destroy();
		}

		stepCount++;
	}

	public void destroy() {
		World.destroyActor(this);
	}

	private void updateScale() {
		Transform3D transform3D = new Transform3D();
		transformGroup.getTransform(transform3D);
		transform3D.setScale(currentScale);

		transformGroup.setTransform(transform3D);
	}

	private void updateTransparency() {
		float transparency = ((float) stepCount / ((float) duration / 5));
		sparkShape.getAppearance().getTransparencyAttributes().setTransparency(transparency);
	}

	public void setPosition(Vector3f position) {
		Transform3D transform3D = new Transform3D();
		transform3D.setTranslation(position);
		transformGroup.setTransform(transform3D);
	}
}
