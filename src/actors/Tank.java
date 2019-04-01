package actors;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysics.dynamics.vehicle.DefaultVehicleRaycaster;
import com.bulletphysics.dynamics.vehicle.RaycastVehicle;
import com.bulletphysics.dynamics.vehicle.VehicleRaycaster;
import com.bulletphysics.dynamics.vehicle.VehicleTuning;
import com.bulletphysics.dynamics.vehicle.WheelInfo;
import com.bulletphysics.linearmath.Transform;

import abstracts.Actor;
import abstracts.TankController;
import inter.OnCollisionEnter;
import main.World;
import makers.ExplosionMaker;
import makers.SparkMaker;
import utils.TargetSeeker;

public class Tank extends Actor implements OnCollisionEnter {
	public static final float HINGE_EPS = 0.00001f; // 0.00001f;
	public static final float DEFAULT_HINGE_FACTOR = 1f;

	Vector3f cameraLocalPos = new Vector3f(0.0f, 5.0f, -22.0f);
	Quat4f cameraLocalRot = new Quat4f(0, 1, 0, 0);

	float motorTorque = 15f;
	float cannonUpperLimit = 0.5f;
	float muzzleLowerLimit = -0.25f;

	float dragFactor = 30;

	ActorComponent chassis;
	ActorComponent turret;
	ActorComponent muzzle;

	HingeConstraint chassisTurretHinge;
	HingeConstraint turretMuzzleHinge;

	RaycastVehicle raycastVehicle;

	TankController tankController;

	int life = 100;

	int fireInterval = 30;
	int fireIntervalCount = 0;

	boolean isDestroyed = false;

	float engineForce = 3000;
	float brakeForce = 100;

	float maxSteeringValue = 0.75f;
	float innerWheelSteeringFactor = 2f;
	float speedSteeringFactor = 0.1f;
	float speedSteeringLowerLimit = 0;

	int iff;

	Tank targetTank;

	TargetSeeker targetSeeker;

	public Tank(World world, Vector3f spawnPosition, BranchGroup chassisShape, BranchGroup turretShape,
			BranchGroup muzzleShape) {
		Vector3f chassisBoxShapeDimension = new Vector3f(3, 1, 7);
		Vector3f turretBoxShapeDimension = new Vector3f(2.8f, 1, 2.8f);
		Vector3f muzzleBoxShapeDimension = new Vector3f(0.5f, 0.5f, 2.5f);

		BoxShape chassisBoxShape = new BoxShape(chassisBoxShapeDimension);
		BoxShape turretBoxShape = new BoxShape(turretBoxShapeDimension);
		BoxShape muzzleBoxShape = new BoxShape(muzzleBoxShapeDimension);

		Transform3D chassisTransform3D = new Transform3D();
		Transform3D turretTransform3D = new Transform3D();
		Transform3D muzzleTransform3D = new Transform3D();

		Vector3f turretTranslation = new Vector3f(spawnPosition.x, spawnPosition.y + 1, spawnPosition.z);
		Vector3f muzzleTranslation = new Vector3f(spawnPosition.x - 1, spawnPosition.y + 0, spawnPosition.z);

		chassisTransform3D.setTranslation(spawnPosition);
		muzzleTransform3D.setTranslation(muzzleTranslation);
		turretTransform3D.setTranslation(turretTranslation);

		float chassisMass = 800;
		float turretMass = 150;
		float muzzleMass = 50;

		chassis = new ActorComponent(chassisShape, chassisBoxShape, chassisTransform3D, chassisMass);
		turret = new ActorComponent(turretShape, turretBoxShape, turretTransform3D, turretMass);
		muzzle = new ActorComponent(muzzleShape, muzzleBoxShape, muzzleTransform3D, muzzleMass);

		addActorComp(chassis);
		addActorComp(turret);
		addActorComp(muzzle);

		chassis.getRigidBody().setUserPointer(this);
		turret.getRigidBody().setUserPointer(this);
		muzzle.getRigidBody().setUserPointer(this);

		initHinge();
		initVehicle();

		targetSeeker = new TargetSeeker(this);
	}

	private void initHinge() {
		Vector3f pivotA = new Vector3f(0.0f, 1.0f, 0.0f);
		Vector3f pivotB = new Vector3f(0.0f, -1f, 0.0f);
		Vector3f axleA = new Vector3f(0.0f, 1.0f, 0.0f);
		Vector3f axleB = new Vector3f(0.0f, 1.0f, 0.0f);
		chassisTurretHinge = new HingeConstraint(chassis.getRigidBody(), turret.getRigidBody(), pivotA, pivotB, axleA,
				axleB);
		chassisTurretHinge.buildJacobian();
		chassisTurretHinge.setLimit(chassisTurretHinge.getHingeAngle() - HINGE_EPS,
				chassisTurretHinge.getHingeAngle() + HINGE_EPS);
		addConstraint(chassisTurretHinge);

		pivotA = new Vector3f(0.0f, 0.0f, 1.5f);
		pivotB = new Vector3f(0.0f, 0.0f, -3.5f);
		axleA = new Vector3f(1.0f, 0.0f, 0.0f);
		axleB = new Vector3f(1.0f, 0.0f, 0.0f);
		turretMuzzleHinge = new HingeConstraint(turret.getRigidBody(), muzzle.getRigidBody(), pivotA, pivotB, axleA,
				axleB);
		turretMuzzleHinge.buildJacobian();
		turretMuzzleHinge.setLimit(turretMuzzleHinge.getHingeAngle() - HINGE_EPS,
				turretMuzzleHinge.getHingeAngle() + HINGE_EPS);
		addConstraint(turretMuzzleHinge);
	}

	private void initVehicle() {
		Vector3f wheelDirection = new Vector3f(0, -1, 0);
		Vector3f wheelAxle = new Vector3f(-1, 0, 0);

		float wheelRadius = 0.5f;
		float wheelFriction = 1.0f; // 1000;
		float wheelConnectionHeight = -0.5f;
		float wheelBaseHalfLength = 5f;
		float wheelBaseHalfWidth = 3.0f;

		float suspensionRestLength = 1f;
		float suspensionStiffness = 200f; // 20.f;
		float suspensionDamping = 20f; // 2.3f;
		float suspensionCompression = 40f; // 4.4f;
		float rollInfluence = 1.0f;

		VehicleRaycaster vehicleRaycaster;

		VehicleTuning tuning = new VehicleTuning();
		vehicleRaycaster = new DefaultVehicleRaycaster(World.getDynamicsWorld());
		raycastVehicle = new RaycastVehicle(tuning, chassis.getRigidBody(), vehicleRaycaster);

		chassis.getRigidBody().setActivationState(CollisionObject.DISABLE_DEACTIVATION);

		World.getDynamicsWorld().addVehicle(raycastVehicle);

		Vector3f connectionPoint = new Vector3f();

		boolean isFrontWheel = true;

		connectionPoint = new Vector3f(wheelBaseHalfWidth, wheelConnectionHeight, wheelBaseHalfLength);
		raycastVehicle.addWheel(connectionPoint, wheelDirection, wheelAxle, suspensionRestLength, wheelRadius, tuning,
				isFrontWheel);
		connectionPoint.set(-wheelBaseHalfWidth, wheelConnectionHeight, wheelBaseHalfLength);
		raycastVehicle.addWheel(connectionPoint, wheelDirection, wheelAxle, suspensionRestLength, wheelRadius, tuning,
				isFrontWheel);
		connectionPoint.set(-wheelBaseHalfWidth, wheelConnectionHeight, -wheelBaseHalfLength);
		raycastVehicle.addWheel(connectionPoint, wheelDirection, wheelAxle, suspensionRestLength, wheelRadius, tuning,
				isFrontWheel);
		connectionPoint.set(wheelBaseHalfWidth, wheelConnectionHeight, -wheelBaseHalfLength);
		raycastVehicle.addWheel(connectionPoint, wheelDirection, wheelAxle, suspensionRestLength, wheelRadius, tuning,
				isFrontWheel);

		for (int i = 0; i < raycastVehicle.getNumWheels(); i++) {
			WheelInfo wheel = raycastVehicle.getWheelInfo(i);
			wheel.suspensionStiffness = suspensionStiffness;
			wheel.wheelsDampingRelaxation = suspensionDamping;
			wheel.wheelsDampingCompression = suspensionCompression;
			wheel.frictionSlip = wheelFriction;
			wheel.rollInfluence = rollInfluence;
		}
	}

	public void step() {
		updateDrag();

		if (!isDestroyed) {
			if (tankController != null) {
				tankController.step();
			}
			fireIntervalCount++;
			if (fireIntervalCount > fireInterval) {
				fireIntervalCount = fireInterval;
			}
		} else {
			brake();
		}
	}

	public void fire() {
		if (isDestroyed()) {
			return;
		}
		if (fireIntervalCount < fireInterval) {
			return;
		}

		// ”­ŽË“_‚ÌˆÊ’uEŒü‚«‚ðŒvŽZ
		Transform worldMuzzleTransform = new Transform();
		muzzle.getRigidBody().getWorldTransform(worldMuzzleTransform);
		Vector3f muzzleLocalPos = new Vector3f(0.0f, 1, 5);
		Transform3D localMuzzleTrans3D = new Transform3D();
		localMuzzleTrans3D.setTranslation(muzzleLocalPos);
		Matrix4f muzzleMatrix = new Matrix4f();
		localMuzzleTrans3D.get(muzzleMatrix);
		Transform localMuzzleTrans = new Transform(muzzleMatrix);
		worldMuzzleTransform.mul(localMuzzleTrans);

		// ƒLƒƒƒmƒ“‚ð¶¬
		Cannon cannon = new Cannon(worldMuzzleTransform, this);
		World.add(cannon);

		// ƒLƒƒƒmƒ“‚É—Í‚ð‰Á‚¦‚Ä‘Å‚¿o‚·
		int cannonMass = 10;
		int fireImpulse = 1000;
		Transform transform = muzzle.getTransform();
		Vector3f velocity = muzzle.getLinearVelocity();
		Vector3f axle = new Vector3f(transform.basis.getElement(0, 2), transform.basis.getElement(1, 2),
				transform.basis.getElement(2, 2));
		Vector3f tatalImpulse = new Vector3f(axle.x * fireImpulse + velocity.x * cannonMass,
				axle.y * fireImpulse + velocity.y * cannonMass, axle.z * fireImpulse + velocity.z * cannonMass);
		cannon.applyImpulse(tatalImpulse);

		// ”­ŽËŽž‚Ì‘MŒõ‚ð¶¬‚·‚é
		Transform3D bulletTransform3D = new Transform3D();
		cannon.getActorComps().get(0).getTransformGroup().getTransform(bulletTransform3D);
		Vector3f sparkPos = new Vector3f();
		bulletTransform3D.get(sparkPos);
		SparkMaker.instantiateToWorld(2, 0.01, 100, sparkPos);

		fireIntervalCount = 0;
	}

	public void fireMissile() {
		if (isDestroyed()) {
			return;
		}
		if (fireIntervalCount < fireInterval) {
			return;
		}

		// ”­ŽË“_‚ÌˆÊ’uEŒü‚«‚ðŒvŽZ
		Transform worldMuzzleTransform = new Transform();
		muzzle.getRigidBody().getWorldTransform(worldMuzzleTransform);
		Vector3f muzzleLocalPos = new Vector3f(0.0f, 1, 5);
		Transform3D localMuzzleTrans3D = new Transform3D();
		localMuzzleTrans3D.setTranslation(muzzleLocalPos);
		Matrix4f muzzleMatrix = new Matrix4f();
		localMuzzleTrans3D.get(muzzleMatrix);
		Transform localMuzzleTrans = new Transform(muzzleMatrix);
		worldMuzzleTransform.mul(localMuzzleTrans);

		// ƒLƒƒƒmƒ“‚ð¶¬
		Tank target = targetSeeker.seekTarget();
		Missile cannon = new Missile(worldMuzzleTransform, this, target);
		World.add(cannon);

		fireIntervalCount = 0;
	}

	public void moveForward() {
		if (isDestroyed()) {
			return;
		}

		int frontLeftWheelIndex = 0;
		int frontRightWheelIndex = 1;
		int RearLeftWheelIndex = 2;
		int RearRightWheelIndex = 3;

		raycastVehicle.setSteeringValue(0, frontLeftWheelIndex);
		raycastVehicle.setSteeringValue(0, frontRightWheelIndex);
		raycastVehicle.setSteeringValue(0, RearLeftWheelIndex);
		raycastVehicle.setSteeringValue(0, RearRightWheelIndex);

		raycastVehicle.getWheelInfo(frontLeftWheelIndex).brake = 0;
		raycastVehicle.getWheelInfo(frontLeftWheelIndex).engineForce = engineForce;
		raycastVehicle.getWheelInfo(frontRightWheelIndex).brake = 0;
		raycastVehicle.getWheelInfo(frontRightWheelIndex).engineForce = engineForce;
		raycastVehicle.getWheelInfo(RearLeftWheelIndex).brake = 0;
		raycastVehicle.getWheelInfo(RearLeftWheelIndex).engineForce = engineForce;
		raycastVehicle.getWheelInfo(RearRightWheelIndex).brake = 0;
		raycastVehicle.getWheelInfo(RearRightWheelIndex).engineForce = engineForce;
	}

	public void moveForwardRight() {
		if (isDestroyed()) {
			return;
		}

		float steeringValue = getSteeringValue();

		raycastVehicle.setSteeringValue(-steeringValue, 0);
		raycastVehicle.setSteeringValue(-steeringValue * innerWheelSteeringFactor, 1);
		raycastVehicle.setSteeringValue(steeringValue * innerWheelSteeringFactor, 2);
		raycastVehicle.setSteeringValue(steeringValue, 3);

		raycastVehicle.getWheelInfo(0).brake = 0;
		raycastVehicle.getWheelInfo(0).engineForce = engineForce;
		raycastVehicle.getWheelInfo(1).brake = 0;
		raycastVehicle.getWheelInfo(1).engineForce = engineForce;
		raycastVehicle.getWheelInfo(2).brake = 0;
		raycastVehicle.getWheelInfo(2).engineForce = engineForce;
		raycastVehicle.getWheelInfo(3).brake = 0;
		raycastVehicle.getWheelInfo(3).engineForce = engineForce;
	}

	public void moveForwardLeft() {
		if (isDestroyed()) {
			return;
		}

		float steeringValue = getSteeringValue();

		raycastVehicle.setSteeringValue(steeringValue * innerWheelSteeringFactor, 0);
		raycastVehicle.setSteeringValue(steeringValue, 1);
		raycastVehicle.setSteeringValue(-steeringValue, 2);
		raycastVehicle.setSteeringValue(-steeringValue * innerWheelSteeringFactor, 3);

		raycastVehicle.getWheelInfo(0).brake = 0;
		raycastVehicle.getWheelInfo(0).engineForce = engineForce;
		raycastVehicle.getWheelInfo(1).brake = 0;
		raycastVehicle.getWheelInfo(1).engineForce = engineForce;
		raycastVehicle.getWheelInfo(2).brake = 0;
		raycastVehicle.getWheelInfo(2).engineForce = engineForce;
		raycastVehicle.getWheelInfo(3).brake = 0;
		raycastVehicle.getWheelInfo(3).engineForce = engineForce;
	}

	public void moveBackward() {
		if (isDestroyed()) {
			return;
		}

		raycastVehicle.setSteeringValue(0.0f, 0);
		raycastVehicle.setSteeringValue(0.0f, 1);
		raycastVehicle.setSteeringValue(0.0f, 2);
		raycastVehicle.setSteeringValue(0.0f, 3);

		for (int i = 0; i < raycastVehicle.getNumWheels(); i++) {
			WheelInfo wheel = raycastVehicle.getWheelInfo(i);
			wheel.brake = 0;
			wheel.engineForce = -engineForce;
		}
	}

	public void moveBackwardRight() {
		if (isDestroyed()) {
			return;
		}

		float steeringValue = getSteeringValue();

		raycastVehicle.setSteeringValue(-steeringValue, 0);
		raycastVehicle.setSteeringValue(-steeringValue * innerWheelSteeringFactor, 1);
		raycastVehicle.setSteeringValue(steeringValue * innerWheelSteeringFactor, 2);
		raycastVehicle.setSteeringValue(steeringValue, 3);

		for (int i = 0; i < raycastVehicle.getNumWheels(); i++) {
			WheelInfo wheel = raycastVehicle.getWheelInfo(i);
			wheel.brake = 0;
			wheel.engineForce = -engineForce;
		}
	}

	public void moveBackwardLeft() {
		if (isDestroyed()) {
			return;
		}

		float steeringValue = getSteeringValue();

		raycastVehicle.setSteeringValue(steeringValue * innerWheelSteeringFactor, 0);
		raycastVehicle.setSteeringValue(steeringValue, 1);
		raycastVehicle.setSteeringValue(-steeringValue, 2);
		raycastVehicle.setSteeringValue(-steeringValue * innerWheelSteeringFactor, 3);

		for (int i = 0; i < raycastVehicle.getNumWheels(); i++) {
			WheelInfo wheel = raycastVehicle.getWheelInfo(i);
			wheel.brake = 0;
			wheel.engineForce = -engineForce;
		}
	}

	public void turnRight() {
		if (isDestroyed()) {
			return;
		}

		float steeringValue = getSteeringValue();

		raycastVehicle.setSteeringValue(-steeringValue, 0);
		raycastVehicle.setSteeringValue(-steeringValue * innerWheelSteeringFactor, 1);
		raycastVehicle.setSteeringValue(steeringValue * innerWheelSteeringFactor, 2);
		raycastVehicle.setSteeringValue(steeringValue, 3);

		raycastVehicle.getWheelInfo(0).brake = 0;
		raycastVehicle.getWheelInfo(0).engineForce = 6000;
		raycastVehicle.getWheelInfo(1).brake = 0;
		raycastVehicle.getWheelInfo(1).engineForce = 0;
		raycastVehicle.getWheelInfo(2).brake = 0;
		raycastVehicle.getWheelInfo(2).engineForce = 6000;
		raycastVehicle.getWheelInfo(3).brake = 0;
		raycastVehicle.getWheelInfo(3).engineForce = 0;
	}

	public void turnLeft() {
		if (isDestroyed()) {
			return;
		}

		float steeringValue = getSteeringValue();

		raycastVehicle.setSteeringValue(steeringValue * innerWheelSteeringFactor, 0);
		raycastVehicle.setSteeringValue(steeringValue, 1);
		raycastVehicle.setSteeringValue(-steeringValue, 2);
		raycastVehicle.setSteeringValue(-steeringValue * innerWheelSteeringFactor, 3);

		raycastVehicle.getWheelInfo(0).brake = 0;
		raycastVehicle.getWheelInfo(0).engineForce = 0;
		raycastVehicle.getWheelInfo(1).brake = 0;
		raycastVehicle.getWheelInfo(1).engineForce = 6000;
		raycastVehicle.getWheelInfo(2).brake = 0;
		raycastVehicle.getWheelInfo(2).engineForce = 0;
		raycastVehicle.getWheelInfo(3).brake = 0;
		raycastVehicle.getWheelInfo(3).engineForce = 6000;
	}

	public void brake() {
		raycastVehicle.setSteeringValue(0.0f, 0);
		raycastVehicle.setSteeringValue(0.0f, 1);
		raycastVehicle.setSteeringValue(0.0f, 2);
		raycastVehicle.setSteeringValue(0.0f, 3);
		for (int i = 0; i < raycastVehicle.getNumWheels(); i++) {
			WheelInfo wheel = raycastVehicle.getWheelInfo(i);
			wheel.engineForce = 0;
			wheel.brake = brakeForce;
		}
	}

	public void rotateMuzzleUp() {
		turretMuzzleHinge.setLimit(muzzleLowerLimit, cannonUpperLimit, DEFAULT_HINGE_FACTOR, DEFAULT_HINGE_FACTOR,
				DEFAULT_HINGE_FACTOR);
		turretMuzzleHinge.enableAngularMotor(true, DEFAULT_HINGE_FACTOR, motorTorque);

	}

	public void rotateMuzzleDown() {
		turretMuzzleHinge.setLimit(muzzleLowerLimit, cannonUpperLimit, DEFAULT_HINGE_FACTOR, DEFAULT_HINGE_FACTOR,
				DEFAULT_HINGE_FACTOR);
		turretMuzzleHinge.enableAngularMotor(true, -DEFAULT_HINGE_FACTOR, motorTorque);
	}

	public void stopMuzzleRotation() {
		float muzzleRotation = turretMuzzleHinge.getHingeAngle();
		turretMuzzleHinge.enableAngularMotor(false, 0.0f, 0.0f);
		turretMuzzleHinge.setLimit(muzzleRotation - HINGE_EPS, muzzleRotation + HINGE_EPS, DEFAULT_HINGE_FACTOR,
				DEFAULT_HINGE_FACTOR, DEFAULT_HINGE_FACTOR);
	}

	public void rotateTurretRight() {
		chassisTurretHinge.setLimit(0.0f, 0.0f);
		chassisTurretHinge.enableAngularMotor(true, DEFAULT_HINGE_FACTOR, motorTorque);
	}

	public void rotateTurretLeft() {
		chassisTurretHinge.setLimit(0.0f, 0.0f);
		chassisTurretHinge.enableAngularMotor(true, -DEFAULT_HINGE_FACTOR, motorTorque);
	}

	public void stopTurretRotation() {
		float turretRotation = chassisTurretHinge.getHingeAngle();
		chassisTurretHinge.enableAngularMotor(false, 0.0f, 0.0f);
		chassisTurretHinge.setLimit(turretRotation - HINGE_EPS, turretRotation + HINGE_EPS, DEFAULT_HINGE_FACTOR,
				DEFAULT_HINGE_FACTOR, DEFAULT_HINGE_FACTOR);
	}

	public Transform3D getCameraTransform3D() {
		Transform3D out = new Transform3D();
		muzzle.getTransformGroup().getTransform(out);

		Transform3D localTransform3D = new Transform3D();
		localTransform3D.setTranslation(cameraLocalPos);
		localTransform3D.setRotation(cameraLocalRot);

		out.mul(localTransform3D);

		return out;
	}

	public Vector3f getChassisPosition() {
		Transform transform = new Transform();
		chassis.getRigidBody().getMotionState().getWorldTransform(transform);
		Vector3f position = new Vector3f();
		transform.transform(position);
		return position;
	}

	public Vector3f getTurretPosition() {
		Transform transform = new Transform();
		turret.getRigidBody().getMotionState().getWorldTransform(transform);
		Vector3f position = new Vector3f();
		transform.transform(position);
		return position;
	}

	public void damage(int damage) {
		life = life - damage;

		if (life < 0) {
			life = 0;
		}

		if (life <= 0 && !isDestroyed) {
			destroy();
		}
	}

	public void destroy() {
		for (ActorComponent component : getActorComps()) {
			double randomX = (Math.random() - 0.5) * 5000;
			double randomY = (Math.random() - 0.5) * 5000;
			double randomZ = (Math.random() - 0.5) * 5000;

			component.getRigidBody().applyImpulse(new Vector3f((float) randomX, (float) randomY, (float) randomZ),
					new Vector3f(0, 0, 0));
			component.getRigidBody().applyTorque(new Vector3f((float) randomX, (float) randomY, (float) randomZ));
		}

		for (TypedConstraint constraint : getConstraints()) {
			World.getDynamicsWorld().removeConstraint(constraint);
		}

		isDestroyed = true;

		ExplosionMaker.instantiateToWorld(1, 0.03f, 500, getChassisPosition());
	}

	public float getTotalVelocity() {
		Vector3f velocity = new Vector3f();
		chassis.getRigidBody().getLinearVelocity(velocity);
		double totalVelocity = Math.abs(velocity.x) + Math.abs(velocity.y) + Math.abs(velocity.z);

		Transform transform = new Transform();

		chassis.getRigidBody().getWorldTransform(transform);

		transform.inverse();
		transform.basis.transform(velocity);

		return velocity.z;
	}

	public void updateDrag() {
		Vector3f velocity = new Vector3f();
		chassis.getRigidBody().getLinearVelocity(velocity);
		float velocityX = velocity.x;
		float velocityY = velocity.y;
		float velocityZ = velocity.z;

		float dragX = 0;
		float dragY = 0;
		float dragZ = 0;

		if (velocityX <= 0) {
			dragX = velocityX * velocityX * dragFactor;
		} else {
			dragX = -(velocityX * velocityX) * dragFactor;
		}
		if (velocityY <= 0) {
			dragY = velocityY * velocityY * dragFactor;
		} else {
			dragY = -(velocityY * velocityY) * dragFactor;
		}
		if (velocityZ <= 0) {
			dragZ = velocityZ * velocityZ * dragFactor;
		} else {
			dragZ = -(velocityZ * velocityZ) * dragFactor;
		}

		Vector3f drag = new Vector3f(dragX, dragY, dragZ);
		chassis.getRigidBody().applyCentralForce(drag);
	}

	public void setIff(int iff) {
		this.iff = iff;
	}

	public int getIff() {
		return this.iff;
	}

	@Override
	public void onCollisionEnter(ManifoldPoint collisionPoint, CollisionObject thisCollision,
			CollisionObject collided) {
		int damage = (int) collisionPoint.appliedImpulse / 1000;

		int minimumDamage = 5;
		int damageFactor = 2;

		if (damage >= minimumDamage) {
			damage(damage * damageFactor);
		}
	}

	public float getChassisTurretHingeAngle() {
		return chassisTurretHinge.getHingeAngle();
	}

	public float getTurretCannonHingeAngle() {
		return turretMuzzleHinge.getHingeAngle();
	}

	public Transform getChassisTransform() {
		Transform trans = new Transform();
		chassis.getRigidBody().getMotionState().getWorldTransform(trans);
		return trans;
	}

	public Transform getMuzzleTransform() {
		Transform trans = new Transform();
		muzzle.getRigidBody().getMotionState().getWorldTransform(trans);
		return trans;
	}

	public boolean isDestroyed() {
		return isDestroyed;
	}

	public void setDrag(float drag) {
		this.dragFactor = drag;
	}

	public int getLife() {
		return this.life;
	}

	private float getSteeringValue() {
		float forwardSpeed = getTotalVelocity();
		float steeringValue = maxSteeringValue;
		if (speedSteeringLowerLimit < Math.abs(forwardSpeed)) {
			float adjustedSpeed = Math.abs(forwardSpeed) - speedSteeringLowerLimit;
			steeringValue = maxSteeringValue
					/ (1 + ((adjustedSpeed * speedSteeringFactor) * (adjustedSpeed * speedSteeringFactor)));
		}

		return steeringValue;
	}
}
