package actors;

import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

public class ActorComponent {
	TransformGroup transformGroup;
	RigidBody rigidBody;

	public ActorComponent(Node visualShape, CollisionShape physicalShape, Transform3D transform3D, float mass) {
		transformGroup = new TransformGroup();
		transformGroup.addChild(visualShape);
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		transformGroup.setTransform(transform3D);

		Matrix4f matrix4f = new Matrix4f();
		transform3D.get(matrix4f);

		MotionState motionState = new DefaultMotionState(new Transform(matrix4f));

		Vector3f localInertia = new Vector3f(0, 0, 0);

		boolean isDynamic = (mass != 0f);

		if (isDynamic) {
			physicalShape.calculateLocalInertia(mass, localInertia);
		}

		RigidBodyConstructionInfo cInfo = new RigidBodyConstructionInfo(mass, motionState, physicalShape, localInertia);

		rigidBody = new RigidBody(cInfo);
		rigidBody.setActivationState(RigidBody.DISABLE_DEACTIVATION);
	}

	public ActorComponent(Node visualShape, CollisionShape physicalShape, Transform transform, float mass) {
		Matrix4f matrix4f = new Matrix4f();
		transform.getMatrix(matrix4f);

		transformGroup = new TransformGroup();
		transformGroup.addChild(visualShape);
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		transformGroup.setTransform(new Transform3D(matrix4f));

		MotionState motionState = new DefaultMotionState(new Transform(matrix4f));

		Vector3f localInertia = new Vector3f(0, 0, 0);
		physicalShape.calculateLocalInertia(mass, localInertia);

		rigidBody = new RigidBody(mass, motionState, physicalShape, localInertia);
		rigidBody.setActivationState(RigidBody.DISABLE_DEACTIVATION);
	}

	public void updateVisual() {
		Transform transform = new Transform();
		Matrix4f matrix4f = new Matrix4f();
		Transform3D tramsform3d = new Transform3D();

		rigidBody.getMotionState().getWorldTransform(transform);
		transform.getMatrix(matrix4f);
		tramsform3d = new Transform3D(matrix4f);
		transformGroup.setTransform(tramsform3d);
	}

	public TransformGroup getTransformGroup() {
		return transformGroup;
	}

	public RigidBody getRigidBody() {
		return rigidBody;
	}

	public void setTransformGroup(TransformGroup transformGroup) {
		this.transformGroup = transformGroup;
	}

	public void setRigidBody(RigidBody rigidBody) {
		this.rigidBody = rigidBody;
	}

	public Transform getTransform() {
		Transform trans = new Transform();
		rigidBody.getMotionState().getWorldTransform(trans);
		return trans;
	}

	public Vector3f getLinearVelocity() {
		Vector3f velocity = new Vector3f();
		this.rigidBody.getLinearVelocity(velocity);
		return velocity;
	}

	public Vector3f getAngularVelocity() {
		Vector3f velocity = new Vector3f();
		this.rigidBody.getAngularVelocity(velocity);
		return velocity;
	}
}
