package abstracts;

import javax.media.j3d.TransformGroup;

import java.util.ArrayList;

import javax.media.j3d.BranchGroup;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.universe.*;

import actors.ActorComponent;
import controlers.PlayerTankController;
import main.World;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.*;
import com.bulletphysics.dynamics.*;

import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.geometry.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
import java.util.Timer;

import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.geometry.Box;
import com.bulletphysics.dynamics.constraintsolver.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
import java.util.Timer;

import com.bulletphysics.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.linearmath.*;
import com.sun.j3d.utils.geometry.Box;

public abstract class Actor {
	BranchGroup branchGroup = new BranchGroup();
	ArrayList<ActorComponent> actorComps = new ArrayList<ActorComponent>();
	ArrayList<TypedConstraint> constraints = new ArrayList<TypedConstraint>();
	
	public Actor() {
		branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
	}
	
	public BranchGroup getBranchGroup() {
		return branchGroup;
	}
	
	public ArrayList<ActorComponent> getActorComps() {
		return actorComps;
	}
	
	public ArrayList<TypedConstraint> getConstraints() {
		return constraints;
	}
	
	public void step() {
		
	}
	
	public void destroy() {
		World.destroyActor(this);
	}
	
	public void updateVisual() {
		for (ActorComponent actorComponent: actorComps) {
			actorComponent.updateVisual();
		}
	}
	
	public Vector3f getPosition() {
		Transform transform = new Transform();
		actorComps.get(0).getRigidBody().getMotionState().getWorldTransform(transform);
		Vector3f position = new Vector3f();
		transform.transform(position);
		return position;
	}
	
	public void addActorComp(ActorComponent actorComp) {
		branchGroup.addChild(actorComp.getTransformGroup());
		actorComps.add(actorComp);
	}
	
	public void addConstraint(TypedConstraint constraint) {
		constraints.add(constraint);
	}
	
	public void setBranchGroup(BranchGroup branchGroup) {
		this.branchGroup = branchGroup;
	}
}
