package utils;

import java.util.List;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.linearmath.Transform;
import com.sun.j3d.utils.geometry.Sphere;

import actors.Tank;
import main.Game;
import main.World;

public class TargetSeeker {
	Tank tank;

	Vector3f rayOriginLocalPos = new Vector3f(0, 5, 0);

	float rangeLimitDist = 30000;
	float rangeAngleRadian = 1;

	int CURSOR_LOCAL_POS_Y = 10;

	TransformGroup transformGroup;
	BranchGroup branchGroup;

	public TargetSeeker(Tank tank) {
		this.tank = tank;
		this.rayOriginLocalPos = rayOriginLocalPos;

		Sphere targetCursor = new Sphere();
		this.transformGroup = new TransformGroup();
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		transformGroup.addChild(targetCursor);

		this.branchGroup = new BranchGroup();
		branchGroup.addChild(transformGroup);
	}

	public Tank seekTarget() {
		Tank target = null;

		List<Tank> tanks = Game.getTanks();

		Transform rayOriginTrans = tank.getChassisTransform();
		// J3D側で座標を取得して、JBulletの座標に変更している。JBullet側だけで完結するように要修正。
		Transform3D localTransform3d = new Transform3D();
		localTransform3d.setTranslation(rayOriginLocalPos);
		Matrix4f localMatrix4f = new Matrix4f();
		localTransform3d.get(localMatrix4f);
		Transform localTransform = new Transform(localMatrix4f);
		rayOriginTrans.mul(localTransform);

		double closestDist = 0;
		for (Tank opponent : tanks) {
			if (tank.getIff() == opponent.getIff()) {
				continue;
			}

			if (isOpponentHidden(rayOriginTrans.origin, opponent)) {
				continue;
			}

			Vector3f opponentWorldPos = opponent.getChassisPosition();
			Vector3f opponentLocalPos = ConversionUtils.convPosWorld2Local(tank.getMuzzleTransform(), opponentWorldPos);

			if (!isOpponentInRange(opponentLocalPos)) {
				continue;
			}

			double distance = DistanceUtils.getDistToTarget(opponentLocalPos);
			if ((closestDist == 0 || distance > closestDist) && !opponent.isDestroyed()) {
				target = opponent;
				closestDist = distance;
			}
		}

		return target;
	}

	public BranchGroup getBranchGroup() {
		return this.branchGroup;
	}

	private boolean isOpponentInRange(Vector3f opponentLocalPos) {
		if (DistanceUtils.getDistToTarget(opponentLocalPos) > rangeLimitDist) {
			return false;
		}

		double targetDirX = Math.abs(DirectionUtils.getDirToTargetX(opponentLocalPos));
		if (targetDirX > rangeAngleRadian) {
			return false;
		}

		double targetDirY = Math.abs(DirectionUtils.getDirToTargetY(opponentLocalPos));
		if (targetDirY > rangeAngleRadian) {
			return false;
		}

		return true;
	}

	private boolean isOpponentHidden(Vector3f originPos, Tank opponent) {
		ClosestRayResultCallback rayCallback = new ClosestRayResultCallback(originPos, opponent.getChassisPosition());
		World.getDynamicsWorld().rayTest(originPos, opponent.getChassisPosition(), rayCallback);

		CollisionObject collisionObject = rayCallback.collisionObject;

		if (collisionObject == null) {
			return true;
		}
		if (collisionObject.getUserPointer() == null) {
			return true;
		}

		if (collisionObject.getUserPointer().getClass().equals(AttackDetector.class)) {
			AttackDetector attackDetector = (AttackDetector) collisionObject.getUserPointer();
			if (attackDetector.getTank() == opponent) {
				return false;
			}
		}
		if (!collisionObject.getUserPointer().equals(opponent)) {
			return true;
		}

		return false;
	}
}
