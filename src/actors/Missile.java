package actors;

import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.linearmath.Transform;
import com.sun.j3d.utils.geometry.Sphere;

import abstracts.Actor;
import inter.OnCollisionEnter;
import main.World;
import makers.SparkMaker;
import utils.ConversionUtils;
import utils.DirectionUtils;

public class Missile extends Actor implements OnCollisionEnter {
	Tank source;

	static float size = 1.5f;
	static int mass = 10;
	static int damage = 100;
	static int destructImpulse = 500;

	ActorComponent actorComp;

	boolean isAlreadyBounded = false;

	Vector3f thrustForce = new Vector3f(0, 0, 100);

	float rotationFactor = 10;

	Tank target;

	public Missile(Transform transform, Tank source, Tank target) {
		this.source = source;
		Appearance appearance = new Appearance();
		Material material = new Material();
		material.setDiffuseColor(1.0f, 0.7f, 0.5f);
		appearance.setMaterial(material);

		Sphere visualShape = new Sphere(size, appearance);
		SphereShape physicalShape = new SphereShape(size);

		actorComp = new ActorComponent(visualShape, physicalShape, transform, mass);
		addActorComp(actorComp);

		actorComp.getRigidBody().setUserPointer(this);

		this.target = target;
	}

	public void destroy() {
		World.destroyActor(this);
	}

	public Tank getSource() {
		return source;
	}

	public int getDamage() {
		return damage;
	}

	public void applyImpulse(Vector3f impulse) {
		actorComp.getRigidBody().applyCentralImpulse(impulse);
	}

	public void step() {
		Vector3f thrustForceWorld = ConversionUtils.convVecLocal2World(actorComp.getTransform(), thrustForce);

		rotateToTarget();

		actorComp.getRigidBody().setLinearVelocity(thrustForceWorld);
	}

	private void rotateToTarget() {
		if (target == null) {
			return;
		}

		Vector3f targetWorldPos = target.getChassisPosition();
		Vector3f targetLocalPos = ConversionUtils.convPosWorld2Local(actorComp.getTransform(), targetWorldPos);
		double targetLocalDirX = DirectionUtils.getDirToTargetX(targetLocalPos);
		double targetLocalDirY = DirectionUtils.getDirToTargetY(targetLocalPos);

		Vector3f localAngularVelocity = new Vector3f((float) -targetLocalDirY * rotationFactor,
				(float) targetLocalDirX * rotationFactor, 0);
		Vector3f worldAngularVelocity = ConversionUtils.convVecLocal2World(actorComp.getTransform(),
				localAngularVelocity);

		actorComp.getRigidBody().setAngularVelocity(worldAngularVelocity);
	}

	@Override
	public void onCollisionEnter(ManifoldPoint collisionPoint, CollisionObject thisCollision,
			CollisionObject collided) {
		if (collisionPoint.appliedImpulse > destructImpulse
				|| (isAlreadyBounded && collisionPoint.appliedImpulse > 1)) {
			destroy();

			Transform transform = actorComp.getTransform();

			double sparkInitScale = 2;
			double sparkSpeed = 0.05f;
			int sparkDuration = 100;

			SparkMaker.instantiateToWorld(sparkInitScale, sparkSpeed, sparkDuration, transform.origin);

			if (collided.getUserPointer() == null) {
				return;
			}

			if (collided.getUserPointer().getClass().equals(Tank.class)) {
				Tank hitTank = (Tank) collided.getUserPointer();
				hitTank.damage(damage);
			}

		} else {
			isAlreadyBounded = true;
		}
	}
}
