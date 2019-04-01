package inter;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;

public interface OnCollisionEnter {
	public void onCollisionEnter(ManifoldPoint collisionPoint, CollisionObject thisCollidion, CollisionObject collided);
}
