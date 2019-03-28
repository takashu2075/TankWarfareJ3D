package controlers;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import abstracts.TankController;
import actors.ActorComponent;
import actors.Tank;
import ai.WayPoint;
import main.World;
import utils.AttackDetector;
import utils.ConversionUtils;
import utils.DirectionUtils;
import utils.TargetSeeker;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.universe.*;

import abstracts.Actor;
import abstracts.TankController;
import utils.ConversionUtils;

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

import java.util.*;
import java.util.List;
import java.util.Timer;

import com.bulletphysics.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.linearmath.*;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.dynamics.vehicle.*;

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
		
		int ATTACK_DETECTION_RADUIS = 30;
		int ATTACK_DETECTION_CAUTIOUS_DURATION = 300;
		float ENEMY_SEEK_RANGE_DIST = 100;
		float ENEMY_SEEK_RANGE_ANGLE = 1.3f;
		float RAY_ORIGIN_LOCAL_POS_Y = 5;
		
		this.tank = tank;
		this.cameraTransformGroup = new TransformGroup();
		this.wayPoints = wayPoints;
		
		this.attackDetector = new AttackDetector(this.tank, ATTACK_DETECTION_RADUIS, ATTACK_DETECTION_CAUTIOUS_DURATION);
		
		Vector3f rayOriginLocalPos = new Vector3f(0, RAY_ORIGIN_LOCAL_POS_Y, 0);
		this.targetSeeker = new TargetSeeker(tank, rayOriginLocalPos, ENEMY_SEEK_RANGE_DIST, ENEMY_SEEK_RANGE_ANGLE);
		
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
		
		Vector3f wayPointLocalPos = ConversionUtils.convPosWorld2Local(tank.getBodyTransform(), wayPoints[wayPointIndex]);
		double wayPointDirX = DirectionUtils.getDirToTargetX(wayPointLocalPos);
		
		if (wayPointDirX >= 0.1f) {
			tank.turnLeft();
		} else if (wayPointDirX < -0.1f) {
			tank.turnRight();
		} else {
			tank.moveForward();
		}
		
		Vector3f tankPosition = tank.getPosition();
		
		if (Math.abs(wayPoints[wayPointIndex].x - tankPosition.x) < 10 && 
				Math.abs(wayPoints[wayPointIndex].z - tankPosition.z) < 10 &&
				wayPointsCount != 0) {
			wayPointIndex++;
		}
	}
	
	private void rotateTurretAndCannon() {
		if (target == null) {
			if (tank.getBodyTurretHingeAngle() > 0.01) {
				tank.rotateTurretLeft();
			} else if (tank.getBodyTurretHingeAngle() < -0.01) {
				tank.rotateTurretRight();
			}
			
			if (tank.getTurretCannonHingeAngle() > 0.01) {
				tank.rotateMuzzleDown();
			} else if (tank.getTurretCannonHingeAngle() < -0.01) {
				tank.rotateMuzzleUp();
			}
			
			return;
		}
		
		Vector3f targetDirLocal = ConversionUtils.convPosWorld2Local(tank.getCannonTransform(), target.getTurretPosition());
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
