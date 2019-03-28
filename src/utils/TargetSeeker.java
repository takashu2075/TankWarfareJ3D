package utils;

import java.util.ArrayList;
import java.util.Timer;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.dynamics.vehicle.RaycastVehicle;
import com.bulletphysics.dynamics.vehicle.VehicleRaycaster;

import abstracts.TankController;
import actors.ActorComponent;
import actors.Tank;
import main.Game;
import main.World;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.universe.*;

import abstracts.Actor;
import abstracts.TankController;
import utils.EnemyCounter;
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
import javax.swing.tree.ExpandVetoException;

import java.util.*;
import java.util.List;
import java.util.Timer;

import com.bulletphysics.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.linearmath.*;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.dynamics.vehicle.*;

public class TargetSeeker {
	Tank tank;
	
	Vector3f rayOriginLocalPos;
	
	float rangeDist = 0;
	float rangeAngleRadian = 0;
	
	int CURSOR_LOCAL_POS_Y = 10;
	
	TransformGroup transformGroup;
	BranchGroup branchGroup;
	
	public TargetSeeker(Tank tank, Vector3f rayOriginLocalPos, float rangeDist, float rangeAngleRadian) {
		this.tank = tank;
		this.rayOriginLocalPos = rayOriginLocalPos;
		this.rangeDist = rangeDist;
		this.rangeAngleRadian = rangeAngleRadian;
		
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
		
		Transform rayOriginTrans = tank.getBodyTransform();
		//J3D側で座標を取得して、JBulletの座標に変更している。JBullet側だけで完結するように要修正。
		Transform3D localTransform3d = new Transform3D();
		localTransform3d.setTranslation(rayOriginLocalPos);
		Matrix4f localMatrix4f = new Matrix4f();
		localTransform3d.get(localMatrix4f);
		Transform localTransform = new Transform(localMatrix4f);
		rayOriginTrans.mul(localTransform);
		
		for (Tank opponent: tanks) {
			if (tank.getIff() == opponent.getIff()) {
				continue;
			}
			
			if (isOpponentHidden(rayOriginTrans.origin, opponent)) {
				continue;
			}
			
			Vector3f opponentWorldPos = opponent.getPosition();
			Vector3f opponentLocalPos = ConversionUtils.convPosWorld2Local(tank.getCannonTransform(), opponentWorldPos);
			
			if (!isOpponentInRange(opponentLocalPos)) {
				continue;
			}
			
			target = opponent;
		}
		
		return target;
		
		//初期状態ではカーソルを地面の下によけておく笑
//		Vector3f targetPos = new Vector3f(0, -100, 0);
//		
//		if (target != null) {
//			targetPos = target.getPosition();
//		}
//		
//		Vector3f cursorPos = new Vector3f(targetPos.x, targetPos.y + CURSOR_LOCAL_POS_Y, targetPos.z);
//		
//		Transform3D transform3D = new Transform3D();
//		transform3D.setTranslation(cursorPos);
//		transformGroup.setTransform(transform3D);
	}
	
	public BranchGroup getBranchGroup() {
		return this.branchGroup;
	}
	
	private boolean isOpponentInRange(Vector3f opponentLocalPos) {
		if (DistanceUtils.getDistToTarget(opponentLocalPos) > rangeDist) {
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
		ClosestRayResultCallback rayCallback = new ClosestRayResultCallback(originPos, opponent.getPosition());
		World.getDynamicsWorld().rayTest(originPos, opponent.getPosition(), rayCallback);
		
		CollisionObject collisionObject = rayCallback.collisionObject;
		
		if (collisionObject == null) {
			return true;
		}
		if (collisionObject.getUserPointer() == null) {
			return true;
		}
		if (!collisionObject.getUserPointer().equals(opponent)) {
			return true;
		}
		
		return false;
	}
}
