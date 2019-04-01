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

public class Cannon extends Actor implements OnCollisionEnter {
	Tank source;

	static float size = 1.5f;
	static int mass = 10;
	static int damage = 30;
	static int destructImpulse = 500;

	ActorComponent actorComp;

	boolean isAlreadyBounded = false;

	public Cannon(Transform transform, Tank source) {
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
