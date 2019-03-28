package utils;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.universe.*;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;

import abstracts.Actor;
import abstracts.TankController;
import actors.ActorComponent;
import actors.Cannon;
import actors.Tank;
import inter.OnCollisionEnter;
import main.World;
import utils.EnemyCounter;
import utils.ConversionUtils;
import utils.TargetSeeker;

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

import com.bulletphysics.collision.dispatch.GhostObject;
import com.bulletphysics.collision.dispatch.CollisionFlags;

public class AttackDetector implements OnCollisionEnter {
	GhostObject ghostObject;
	SphereShape sphereShape;
	
	Tank tank;
	
	Tank target;
	
	int cautiousDuration = 0;
	
	int cautiousTimeCount = 0;
	
	public AttackDetector(Tank tank, float detectionRadius, int cautiousDuration) {
		this.tank = tank;
		this.cautiousDuration = cautiousDuration;
		
		sphereShape = new SphereShape(detectionRadius);
		ghostObject = new GhostObject();
		ghostObject.setCollisionShape(sphereShape);
		ghostObject.setUserPointer(this);
		ghostObject.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
		ghostObject.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);
		
		World.getDynamicsWorld().addCollisionObject(ghostObject);
	}
	
	public void step() {
		Transform transform = tank.getBodyTransform();
		ghostObject.setWorldTransform(transform);
		
//		if (target != null) {
//			cautiousTimeCount++;
//		}
//		
//		if (cautiousTimeCount >= cautiousDuration) {
//			target = null;
//			cautiousTimeCount = 0;
//		}
	}
	
	@Override
	public void onCollisionEnter(ManifoldPoint collisionPoint, CollisionObject thisCollision, CollisionObject collidedWith) {
		if (collidedWith.getUserPointer() == null) {
			return;
		}
		if (!collidedWith.getUserPointer().getClass().equals(Cannon.class)) {
			return;
		}
		
		Cannon bullet = (Cannon) collidedWith.getUserPointer();
		Tank firedBy = bullet.getFiredBy();
				
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
}
