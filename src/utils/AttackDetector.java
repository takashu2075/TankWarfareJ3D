package utils;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.GhostObject;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.linearmath.Transform;

import actors.Cannon;
import actors.Tank;
import inter.OnCollisionEnter;
import main.World;

public class AttackDetector implements OnCollisionEnter {
	GhostObject ghostObject;
	SphereShape sphereShape;

	Tank tank;

	Tank target;

	int detectionRadius = 30;
	int cautiousDuration = 300;

	int cautiousTimeCount = 0;

	public AttackDetector(Tank tank) {
		this.tank = tank;

		sphereShape = new SphereShape(detectionRadius);
		ghostObject = new GhostObject();
		ghostObject.setCollisionShape(sphereShape);
		ghostObject.setUserPointer(this);
		ghostObject.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
		ghostObject.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);

		World.getDynamicsWorld().addCollisionObject(ghostObject);
	}

	public void step() {
		Transform transform = tank.getChassisTransform();
		ghostObject.setWorldTransform(transform);
	}

	@Override
	public void onCollisionEnter(ManifoldPoint collisionPoint, CollisionObject thisCollision,
			CollisionObject collidedWith) {
		if (collidedWith.getUserPointer() == null) {
			return;
		}
		if (!collidedWith.getUserPointer().getClass().equals(Cannon.class)) {
			return;
		}

		Cannon bullet = (Cannon) collidedWith.getUserPointer();
		Tank firedBy = bullet.getSource();

		if (firedBy.getIff() != tank.getIff()) {
			target = firedBy;
			cautiousTimeCount = 0;
		}
	}

	public GhostObject getDetectorObject() {
		return this.ghostObject;
	}

	public Tank getTarget() {
		return target;
	}

	public void resetTarget() {
		target = null;
	}

	public Tank getTank() {
		return this.tank;
	}
}
