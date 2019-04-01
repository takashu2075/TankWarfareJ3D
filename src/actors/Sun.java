package actors;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import abstracts.Actor;

public class Sun extends Actor {
	TransformGroup transformGroup = new TransformGroup();
	DirectionalLight directionalLight;

	public Sun() {
		directionalLight = new DirectionalLight(true, new Color3f(1.0f, 1.0f, 1.0f), new Vector3f(-0.5f, -0.5f, -1.0f));
		transformGroup.addChild(directionalLight);

		Transform3D sunTransform3D = new Transform3D();
		sunTransform3D.setRotation(new AxisAngle4f(0.0f, 1.0f, 0.0f, 1.0f));
		transformGroup.setTransform(sunTransform3D);

		directionalLight.setInfluencingBounds(new BoundingSphere(new Point3d(), 1000.0));

		getBranchGroup().addChild(transformGroup);
	}
}
