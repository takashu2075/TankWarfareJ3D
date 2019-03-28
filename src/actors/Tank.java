package actors;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.universe.*;

import abstracts.Actor;
import abstracts.TankController;
import inter.OnCollisionEnter;
import main.World;
import makers.BulletMaker;
import makers.ExplosionMaker;
import makers.Spark2Maker;
import makers.SparkMaker;
import utils.AttackDetector;
import utils.EnemyCounter;
import utils.ConversionUtils;
import utils.TargetSeeker;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;

import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.geometry.Box;
import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysics.dynamics.vehicle.*;

import com.sun.j3d.loaders.*;
import com.sun.j3d.loaders.objectfile.ObjectFile;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.TransformAttribute;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.ExpandVetoException;

import java.util.*;
import java.util.List;
import java.util.Timer;

import com.bulletphysics.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.linearmath.*;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.dynamics.vehicle.*;

public class Tank extends Actor implements OnCollisionEnter {
	public static final float PI_2 = 1.57079632679489661923f;
	public static final float TURRET_EPS = 0.00001f; // 0.0000001f;

	private static final Vector3f wheelDirection = new Vector3f(0, -1, 0);
	private static final Vector3f wheelAxle = new Vector3f(-1, 0, 0);

	private static float wheelRadius = 0.5f;
	private static float wheelFriction = 1.0f; // 1000;
	private static float wheelConnectionHeight = -0.5f;
	private static float wheelBaseHalfLength = 5f;
	private static float wheelBaseHalfWidth = 3.0f;

	private static float suspensionRestLength = 0.8f;
	private static float suspensionStiffness = 200f; // 20.f;
	private static float suspensionDamping = 20f; // 2.3f;
	private static float suspensionCompression = 40f; // 4.4f;
	private static float rollInfluence = 1.0f;
	
	private static float cannonUpperLimit = 0.5f;
	private static float muzzleLowerLimit = -0.3f;
	
	float dragFactor = 30;
	
	ActorComponent body;
	ActorComponent turret;
	ActorComponent muzzle;

	HingeConstraint bodyTurretHinge;
	HingeConstraint turretMuzzleHinge;

	VehicleRaycaster vehicleRaycaster;
	RaycastVehicle raycastVehicle;
	
	TankController tankController;

	float turretRotation = 0.0f;
	float muzzleRotation = 0.0f;

	int life = 100;

	int fireInterval = 30;
	int fireIntervalCount = 0;

	boolean isDestroyed = false;

	float engineForce = 3000;
	float brakeForce = 100;
	
	float maxSteeringValue = 0.75f;
	float innerWheelSteeringFactor = 2f;
	float totalVelocity = 0;
	float forwardSpeed = 0;
	float speedSteeringFactor = 0.1f;
	float speedSteeringLowerLimit = 0;

	int iff;

	TargetSeeker targetSeeker;
	AttackDetector attackDetector;

	Tank targetTank;
	
	public Tank(World world, Vector3f spawnPosition, BranchGroup bodyShape, BranchGroup turretShape,
			BranchGroup muzzleShape) {
		BoxShape bodyBoxShape = new BoxShape(new Vector3f(3, 1, 7));
		bodyBoxShape.setUserPointer(this);
		BoxShape turretBoxShape = new BoxShape(new Vector3f(2.8f, 1, 2.8f));
		turretBoxShape.setUserPointer(this);
		BoxShape muzzleBoxShape = new BoxShape(new Vector3f(0.5f, 0.5f, 2.5f));
		muzzleBoxShape.setUserPointer(this);

		Transform3D bodyTransform3D = new Transform3D();
		Transform3D turretTransform3D = new Transform3D();
		Transform3D muzzleTransform3D = new Transform3D();
		Transform3D cameraTransform3D = new Transform3D();

		bodyTransform3D.setTranslation(new Vector3f(spawnPosition.x, spawnPosition.y, spawnPosition.z));
		muzzleTransform3D.setTranslation(new Vector3d(spawnPosition.x - 1, spawnPosition.y + 0, spawnPosition.z));
		turretTransform3D.setTranslation(new Vector3f(spawnPosition.x, spawnPosition.y + 1, spawnPosition.z));
		cameraTransform3D.setTranslation(new Vector3d(spawnPosition.x - 9, spawnPosition.y, spawnPosition.z));
		
		float bodyMass = 800;
		float turretMass = 150;
		float muzzleMass = 50;
		
		body = new ActorComponent(bodyShape, bodyBoxShape, bodyTransform3D, bodyMass);
		turret = new ActorComponent(turretShape, turretBoxShape, turretTransform3D, turretMass);
		muzzle = new ActorComponent(muzzleShape, muzzleBoxShape, muzzleTransform3D, muzzleMass);

		addActorComp(body);
		addActorComp(turret);
		addActorComp(muzzle);
		
		body.getRigidBody().setUserPointer(this);
		turret.getRigidBody().setUserPointer(this);
		muzzle.getRigidBody().setUserPointer(this);

		Vector3f pivotA = new Vector3f(0.0f, 1.0f, 0.0f);
		Vector3f pivotB = new Vector3f(0.0f, -1f, 0.0f);
		Vector3f axleA = new Vector3f(0.0f, 1.0f, 0.0f);
		Vector3f axleB = new Vector3f(0.0f, 1.0f, 0.0f);
		bodyTurretHinge = new HingeConstraint(body.getRigidBody(), turret.getRigidBody(), pivotA, pivotB, axleA, axleB);
		bodyTurretHinge.buildJacobian();
		bodyTurretHinge.setLimit(bodyTurretHinge.getHingeAngle() - TURRET_EPS,
				bodyTurretHinge.getHingeAngle() + TURRET_EPS);
		addConstraint(bodyTurretHinge);

		pivotA = new Vector3f(0.0f, 0.0f, 1.5f);
		pivotB = new Vector3f(0.0f, 0.0f, -3.5f);
		axleA = new Vector3f(1.0f, 0.0f, 0.0f);
		axleB = new Vector3f(1.0f, 0.0f, 0.0f);
		turretMuzzleHinge = new HingeConstraint(turret.getRigidBody(), muzzle.getRigidBody(), pivotA, pivotB, axleA,
				axleB);
		turretMuzzleHinge.buildJacobian();
		turretMuzzleHinge.setLimit(turretMuzzleHinge.getHingeAngle() - TURRET_EPS,
				turretMuzzleHinge.getHingeAngle() + TURRET_EPS);
		addConstraint(turretMuzzleHinge);

		initVehicle();
	}

	public void step() {
		updateDrag();
		updateForwardSpeed();
		
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

	public void moveForward() {
		if (isDestroyed()) {
			return;
		}
		
		raycastVehicle.setSteeringValue(0.0f, 0);
		raycastVehicle.setSteeringValue(0.0f, 1);
		raycastVehicle.setSteeringValue(0.0f, 2);
		raycastVehicle.setSteeringValue(0.0f, 3);

		raycastVehicle.getWheelInfo(0).brake = 0;
		raycastVehicle.getWheelInfo(0).engineForce = 3000;
		raycastVehicle.getWheelInfo(1).brake = 0;
		raycastVehicle.getWheelInfo(1).engineForce = 3000;
		raycastVehicle.getWheelInfo(2).brake = 0;
		raycastVehicle.getWheelInfo(2).engineForce = 3000;
		raycastVehicle.getWheelInfo(3).brake = 0;
		raycastVehicle.getWheelInfo(3).engineForce = 3000;
	}

	public void moveForwardRight() {
		if (isDestroyed()) {
			return;
		}
		
		float steeringValue = getSteeringValue();

		raycastVehicle.setSteeringValue(-steeringValue, 0);
		raycastVehicle.setSteeringValue(-steeringValue * innerWheelSteeringFactor, 1);
		raycastVehicle.setSteeringValue(steeringValue* innerWheelSteeringFactor, 2);
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
		turretMuzzleHinge.setLimit(muzzleLowerLimit, cannonUpperLimit, 1, 1, 1);
		turretMuzzleHinge.enableAngularMotor(true, 1f, 100.0f);

	}

	public void rotateMuzzleDown() {
		turretMuzzleHinge.setLimit(muzzleLowerLimit, cannonUpperLimit, 1, 1, 1);
		turretMuzzleHinge.enableAngularMotor(true, -1f, 100.0f);
	}

	public void stopMuzzleRotation() {
		muzzleRotation = turretMuzzleHinge.getHingeAngle();
		turretMuzzleHinge.enableAngularMotor(false, 0.0f, 0.0f);
		turretMuzzleHinge.setLimit(muzzleRotation - TURRET_EPS, muzzleRotation + TURRET_EPS, 1, 1, 1);
	}

	public void rotateTurretRight() {
		bodyTurretHinge.setLimit(0, 0);
		bodyTurretHinge.enableAngularMotor(true, 1, 100.0f);

	}

	public void rotateTurretLeft() {
		bodyTurretHinge.setLimit(0, 0);
		bodyTurretHinge.enableAngularMotor(true, -1, 100.0f);
	}

	public void stopTurretRotation() {
		turretRotation = bodyTurretHinge.getHingeAngle();
		bodyTurretHinge.enableAngularMotor(false, 0, 0);
		bodyTurretHinge.setLimit(turretRotation - TURRET_EPS, turretRotation + TURRET_EPS, 1, 1, 1);
	}

	public HingeConstraint getHingeConstraint() {
		return bodyTurretHinge;
	}

	public RaycastVehicle getRaycastVehicle() {
		return raycastVehicle;
	}

	public Transform3D getCameraTransform3D() {
		Transform3D out = new Transform3D();
		muzzle.getTransformGroup().getTransform(out);

		Transform3D localTransform3D = new Transform3D();
		localTransform3D.setTranslation(new Vector3f(0.0f, 5.0f, -22.0f));
		Quat4f rotation = new Quat4f(0, 1, 0, 0);
		localTransform3D.setRotation(rotation);
		
		out.mul(localTransform3D);

		return out;
	}

	public void fire() {
		if (isDestroyed()) {
			return;
		}
		
		if (fireIntervalCount >= fireInterval) {
			Transform worldMuzzleTransform = new Transform();
			muzzle.getRigidBody().getWorldTransform(worldMuzzleTransform);
			
			Vector3f muzzleLocalPos = new Vector3f(0.0f, 1, 5);
			Transform3D localMuzzleTrans3D = new Transform3D();
			localMuzzleTrans3D.setTranslation(muzzleLocalPos);
			Matrix4f muzzleMatrix = new Matrix4f();
			localMuzzleTrans3D.get(muzzleMatrix);
			Transform localMuzzleTrans = new Transform(muzzleMatrix);

//			Vector3f fireDirection = new Vector3f();
//			fireDirection.set(worldMuzzleTransform.basis.getElement(0, 2), 
//					worldMuzzleTransform.basis.getElement(1, 2), worldMuzzleTransform.basis.getElement(2, 2));
			
			worldMuzzleTransform.mul(localMuzzleTrans);
			
			Cannon cannon = BulletMaker.instantiate(worldMuzzleTransform, this);
			World.add(cannon);

			Transform transform = new Transform();
			
			this.getActorComps().get(2).getRigidBody().getMotionState().getWorldTransform(transform);
			Vector3f axle = new Vector3f(transform.basis.getElement(0, 2), transform.basis.getElement(1, 2),
					transform.basis.getElement(2, 2));

			Vector3f worldCannonVelocity = new Vector3f();
			muzzle.getRigidBody().getVelocityInLocalPoint(new Vector3f(), worldCannonVelocity);
			
			
			Vector3f velo = new Vector3f();
			this.getActorComps().get(2).getRigidBody().getLinearVelocity(velo);

			Vector3f tatalImpulse = new Vector3f(axle.x * 1000 + velo.x * 10, axle.y * 1000 + velo.y * 10,
					axle.z * 1000 + velo.z * 10);

			cannon.applyImpulse(tatalImpulse);

			Transform3D bulletTransform3D = new Transform3D();
			cannon.getActorComps().get(0).getTransformGroup().getTransform(bulletTransform3D);
			Vector3f sparkPos = new Vector3f();
			bulletTransform3D.get(sparkPos);

			Spark2Maker.instantiateToWorld(2, 0.01, 100, sparkPos);
			
			fireIntervalCount = 0;
		}
	}

	private void initVehicle() {
		VehicleTuning tuning = new VehicleTuning();
		vehicleRaycaster = new DefaultVehicleRaycaster(World.getDynamicsWorld());
		raycastVehicle = new RaycastVehicle(tuning, body.getRigidBody(), vehicleRaycaster);

		body.getRigidBody().setActivationState(CollisionObject.DISABLE_DEACTIVATION);

		World.getDynamicsWorld().addVehicle(raycastVehicle);

		Vector3f connectionPoint = new Vector3f();
		

		boolean isFrontWheel = true;

		connectionPoint = new Vector3f(wheelBaseHalfWidth, wheelConnectionHeight, wheelBaseHalfLength);
		raycastVehicle.addWheel(connectionPoint, wheelDirection, wheelAxle, suspensionRestLength, wheelRadius,
				tuning, isFrontWheel);
		connectionPoint.set(-wheelBaseHalfWidth, wheelConnectionHeight, wheelBaseHalfLength);
		raycastVehicle.addWheel(connectionPoint, wheelDirection, wheelAxle, suspensionRestLength, wheelRadius,
				tuning, isFrontWheel);
		connectionPoint.set(-wheelBaseHalfWidth, wheelConnectionHeight, -wheelBaseHalfLength);
		raycastVehicle.addWheel(connectionPoint, wheelDirection, wheelAxle, suspensionRestLength, wheelRadius,
				tuning, isFrontWheel);
		connectionPoint.set(wheelBaseHalfWidth, wheelConnectionHeight, -wheelBaseHalfLength);
		raycastVehicle.addWheel(connectionPoint, wheelDirection, wheelAxle, suspensionRestLength, wheelRadius,
				tuning, isFrontWheel);

		for (int i = 0; i < raycastVehicle.getNumWheels(); i++) {
			WheelInfo wheel = raycastVehicle.getWheelInfo(i);
			wheel.suspensionStiffness = suspensionStiffness;
			wheel.wheelsDampingRelaxation = suspensionDamping;
			wheel.wheelsDampingCompression = suspensionCompression;
			wheel.frictionSlip = wheelFriction;
			wheel.rollInfluence = rollInfluence;
		}
	}

	public Vector3f getPosition() {
		Transform transform = new Transform();
		body.getRigidBody().getMotionState().getWorldTransform(transform);
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

	public double getCannonDirection() {
		Transform transform = new Transform();
		muzzle.getRigidBody().getMotionState().getWorldTransform(transform);

		Matrix3f basis = new Matrix3f(transform.basis);

		double tankDirection = 0;
		if (basis.getElement(2, 0) < 0) {
			tankDirection = Math.atan(1.0f / 0.0f) - Math.atan(basis.getElement(0, 0) / basis.getElement(2, 0));
		} else {
			if (basis.getElement(0, 0) < 0) {
				tankDirection = +Math.atan(basis.getElement(2, 0) / basis.getElement(0, 0));
			} else {
				tankDirection = -Math.atan(1.0f / 0.0f) - Math.atan(basis.getElement(0, 0) / basis.getElement(2, 0));
			}
		}

		return tankDirection;
	}

	public double getBodyDirection() {
		Transform transform = new Transform();
		body.getRigidBody().getMotionState().getWorldTransform(transform);

		Matrix3f basis = new Matrix3f(transform.basis);

		double tankDirection = 0;
		if (basis.getElement(2, 0) < 0) {
			tankDirection = Math.atan(1.0f / 0.0f) - Math.atan(basis.getElement(0, 0) / basis.getElement(2, 0));
		} else {
			if (basis.getElement(0, 0) < 0) {
				tankDirection = +Math.atan(basis.getElement(2, 0) / basis.getElement(0, 0));
			} else {
				tankDirection = -Math.atan(1.0f / 0.0f) - Math.atan(basis.getElement(0, 0) / basis.getElement(2, 0));
			}
		}

		return tankDirection;
	}

	public double getTurretDirection() {
		Transform transform = new Transform();
		turret.getRigidBody().getMotionState().getWorldTransform(transform);

		Matrix3f basis = new Matrix3f(transform.basis);

		double tankDirection = 0;
		if (basis.getElement(2, 0) < 0) {
			tankDirection = Math.atan(1.0f / 0.0f) - Math.atan(basis.getElement(0, 0) / basis.getElement(2, 0));
		} else {
			if (basis.getElement(0, 0) < 0) {
				tankDirection = +Math.atan(basis.getElement(2, 0) / basis.getElement(0, 0));
			} else {
				tankDirection = -Math.atan(1.0f / 0.0f) - Math.atan(basis.getElement(0, 0) / basis.getElement(2, 0));
			}
		}

		return tankDirection;
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

	public void setController(TankController tankController) {
		this.tankController = tankController;
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

		Explosion explosion = ExplosionMaker.instantiate(1, 0.03f, 500, getPosition());
		World.add(explosion);
	}

	public void updateForwardSpeed() {
		Vector3f velocity = new Vector3f();
		body.getRigidBody().getLinearVelocity(velocity);
		totalVelocity = Math.abs(velocity.x) + Math.abs(velocity.y) + Math.abs(velocity.z);

		Transform transform = new Transform();

		body.getRigidBody().getWorldTransform(transform);

		transform.inverse();
		transform.basis.transform(velocity);

		this.forwardSpeed = velocity.z;
	}

	public void updateDrag() {
		Vector3f velocity = new Vector3f();
		body.getRigidBody().getLinearVelocity(velocity);
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
		body.getRigidBody().applyCentralForce(drag);
	}

	public void setIff(int iff) {
		this.iff = iff;
	}

	public int getIff() {
		return this.iff;
	}

	@Override
	public void onCollisionEnter(ManifoldPoint collisionPoint, CollisionObject thisCollision, CollisionObject collided) {
		int damage = (int) collisionPoint.appliedImpulse / 1000;
		
		int minimumDamage = 5;
		int damageFactor = 2;
		
		if (damage >= minimumDamage) {
			damage(damage * damageFactor);
		}
	}

	public Tank getTargetTank() {
		return targetTank;
	}

	public float getBodyTurretHingeAngle() {
		return bodyTurretHinge.getHingeAngle();
	}
	
	public float getTurretCannonHingeAngle() {
		return turretMuzzleHinge.getHingeAngle();
	}
	
	public Transform getBodyTransform() {
		Transform trans = new Transform();
		body.getRigidBody().getMotionState().getWorldTransform(trans);
		return trans;
	}
	
	public Transform getCannonTransform() {
		Transform trans = new Transform();
		muzzle.getRigidBody().getMotionState().getWorldTransform(trans);
		return trans;
	}
	
	public AttackDetector getAttackDetector() {
		return this.attackDetector;
	}
	
	public boolean isDestroyed() {
		return isDestroyed;
	}
	
	public Vector3f getVelocityWorld() {
		Vector3f velocity = new Vector3f();
		body.getRigidBody().getLinearVelocity(velocity);
		return velocity;
	}
	
	public void setDrag(float drag) {
		this.dragFactor = drag;
	}
	
	public int getLife() {
		return this.life;
	}
	
	private float getSteeringValue() {
		float steeringValue = maxSteeringValue;
		if (speedSteeringLowerLimit < Math.abs(forwardSpeed)) {
			float adjustedSpeed = Math.abs(forwardSpeed) - speedSteeringLowerLimit;
			steeringValue = maxSteeringValue
					/ (1 + ((adjustedSpeed * speedSteeringFactor) * (adjustedSpeed * speedSteeringFactor)));
		}
		
		return steeringValue;
	}
}
