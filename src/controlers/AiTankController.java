package controlers;

import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;

import abstracts.TankController;
import actors.Tank;
import utils.AttackDetector;
import utils.ConversionUtils;
import utils.DirectionUtils;
import utils.TargetSeeker;

public class AiTankController extends TankController {
	Tank tank;

	Vector3f[] wayPoints = new Vector3f[100];
	private int wayPointsCount = 0;
	private int wayPointIndex = 0;

	TransformGroup cameraTransformGroup;

	Tank target = null;
	TargetSeeker targetSeeker;
	AttackDetector attackDetector;

	int fireInterval = 100;
	int fireCount = 0;

	int unseekedCount = 0;
	int maxUnseeked = 300;

	public AiTankController(Tank tank, Vector3f[] wayPoints) {
		super(tank);
		this.tank = tank;
		this.cameraTransformGroup = new TransformGroup();
		this.wayPoints = wayPoints;

		this.attackDetector = new AttackDetector(this.tank);

		this.targetSeeker = new TargetSeeker(tank);

		wayPointsCount = wayPoints.length;
	}

	public AiTankController(Tank tank, Tank target) {
		super(tank);

		this.tank = tank;
		this.cameraTransformGroup = new TransformGroup();
		this.target = target;
	}

	public void setCameraView() {
		cameraTransformGroup.setTransform(tank.getCameraTransform3D());
	}

	public void step() {
		moveTank();
		rotateTurretAndCannon();

		seekTarget();

		if (fireCount < fireInterval) {
			fireCount++;
		}
	}

	private void moveTank() {
		if (wayPointIndex == wayPointsCount) {
			wayPointIndex = 0;
		}

		Vector3f wayPointLocalPos = ConversionUtils.convPosWorld2Local(tank.getChassisTransform(),
				wayPoints[wayPointIndex]);
		double wayPointDirX = DirectionUtils.getDirToTargetX(wayPointLocalPos);

		if (wayPointDirX >= 0.1f) {
			tank.turnLeft();
		} else if (wayPointDirX < -0.1f) {
			tank.turnRight();
		} else {
			tank.moveForward();
		}

		Vector3f tankPosition = tank.getChassisPosition();

		if (Math.abs(wayPoints[wayPointIndex].x - tankPosition.x) < 10
				&& Math.abs(wayPoints[wayPointIndex].z - tankPosition.z) < 10 && wayPointsCount != 0) {
			wayPointIndex++;
		}
	}

	private void rotateTurretAndCannon() {
		if (target == null) {
			if (tank.getChassisTurretHingeAngle() > 0.01) {
				tank.rotateTurretLeft();
			} else if (tank.getChassisTurretHingeAngle() < -0.01) {
				tank.rotateTurretRight();
			}

			if (tank.getTurretCannonHingeAngle() > 0.01) {
				tank.rotateMuzzleDown();
			} else if (tank.getTurretCannonHingeAngle() < -0.01) {
				tank.rotateMuzzleUp();
			}

			return;
		}

		Vector3f targetDirLocal = ConversionUtils.convPosWorld2Local(tank.getMuzzleTransform(),
				target.getTurretPosition());
		double targetDirX = DirectionUtils.getDirToTargetX(targetDirLocal);
		double targetDirY = DirectionUtils.getDirToTargetY(targetDirLocal);

		Transform transform = new Transform();
		tank.getActorComps().get(2).getRigidBody().getWorldTransform(transform);

		if (targetDirX > 0.01f) {
			tank.rotateTurretLeft();
		} else if (targetDirX < -0.01f) {
			tank.rotateTurretRight();
		} else {
			if (fireCount >= fireInterval) {
				tank.fire();
				fireCount = 0;
			}
		}

		if (targetDirY > 0.01f) {
			tank.rotateMuzzleUp();
		} else if (targetDirY < -0.01f) {
			tank.rotateMuzzleDown();
		}
	}

	public void addWayPoint(int index, Vector3f position) {
		wayPoints[index] = position;
		wayPointsCount++;
	}

	private void seekTarget() {
		attackDetector.step();

		Tank seekedTarget = targetSeeker.seekTarget();
		if (seekedTarget == null) {
			seekedTarget = attackDetector.getTarget();
		}
		attackDetector.resetTarget();

		if (seekedTarget == null) {

			if (unseekedCount >= maxUnseeked) {
				target = null;
				unseekedCount = 0;
			} else {
				unseekedCount++;
			}
		} else {
			target = seekedTarget;
		}
	}
}
