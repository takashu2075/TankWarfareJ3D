package main;

import com.bulletphysics.dynamics.DiscreteDynamicsWorld;

import com.bulletphysics.dynamics.constraintsolver.*;

import com.bulletphysics.dynamics.constraintsolver.ContactConstraint;

import abstracts.Actor;

import javax.media.j3d.*;
import javax.vecmath.*;

import org.omg.CORBA.DynamicImplementation;

import actors.ActorComponent;
import actors.Atmosphere;
import actors.Cannon;
import actors.Explosion;
import actors.Spark;
import actors.Sun;
import actors.Tank;
import actors.Terrain;
import controlers.PlayerTankController;

import com.sun.j3d.utils.universe.*;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Clock;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.*;
import com.bulletphysics.dynamics.*;

import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.geometry.*;

import inter.OnCollisionEnter;
import makers.BulletMaker;
import makers.ExplosionMaker;
import makers.Spark2Maker;
import makers.SparkMaker;
import utils.AttackDetector;
import utils.EnemyCounter;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
import java.util.Timer;
import com.bulletphysics.collision.dispatch.CollisionObject;

import com.bulletphysics.collision.dispatch.GhostObject;

public class World {
	static SimpleUniverse simpleUniverse;

	static DynamicsWorld dynamicsWorld;

	static ArrayList<Actor> actors;

	Clock clock = new Clock();

	TransformGroup cameraTransformGroup;

	ExplosionMaker explosionMaker;
	SparkMaker muzzleMaker;
	BulletMaker bulletMaker;
	Spark2Maker spark2Maker;

	public World(Canvas3D canvas3D) {
		simpleUniverse = new SimpleUniverse(canvas3D);

		DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
		CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);

		DbvtBroadphase overlappingPairCache = new DbvtBroadphase();
		SequentialImpulseConstraintSolver constraintSolver = new SequentialImpulseConstraintSolver();
		dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, overlappingPairCache, constraintSolver,
				collisionConfiguration);

		dynamicsWorld.setInternalTickCallback(new InternalTickCallback() {
			@Override
			public void internalTick(DynamicsWorld dynamicsWorld, float timeStep) {
				resolveColision();
			}
		}, null);

		actors = new ArrayList<Actor>();

		explosionMaker = new ExplosionMaker(cameraTransformGroup, simpleUniverse.getCanvas(), this);
		muzzleMaker = new SparkMaker(cameraTransformGroup, this);
		spark2Maker = new Spark2Maker();
	}

	public static void add(Actor actor) {
		simpleUniverse.addBranchGraph(actor.getBranchGroup());

		for (ActorComponent actorComponent : actor.getActorComps()) {
			dynamicsWorld.addRigidBody(actorComponent.getRigidBody());
		}

		for (TypedConstraint constraint : actor.getConstraints()) {
			dynamicsWorld.addConstraint(constraint, true);
		}

		actors.add(actor);
	}

	public void step() {
		boolean idle = false;
		float dt = getDeltaTimeMicroseconds() * 0.000001f;
		int maxSimSubSteps = idle ? 1 : 2;
		if (idle) {
			dt = 1f / 420f;
		}
		
		int numSimSteps = dynamicsWorld.stepSimulation(dt, maxSimSubSteps);

		for (Actor actor : actors) {
			actor.step();
			actor.updateVisual();
		}
	}

	public static SimpleUniverse getSimpleUniverse() {
		return simpleUniverse;
	}

	public static DynamicsWorld getDynamicsWorld() {
		return dynamicsWorld;
	}

	public float getDeltaTimeMicroseconds() {
		float dt = clock.getTimeMicroseconds();
		clock.reset();
		return dt;
	}

	public void setCamera(ViewingPlatform viewingPlatform) {
		this.cameraTransformGroup = viewingPlatform.getViewPlatformTransform();
	}

	public void initMakers() {
		explosionMaker = new ExplosionMaker(cameraTransformGroup, simpleUniverse.getCanvas(), this);
		muzzleMaker = new SparkMaker(cameraTransformGroup, this);
		bulletMaker = new BulletMaker();
		spark2Maker = new Spark2Maker();
	}

	public void resolveColision() {
		Dispatcher dispatcher = dynamicsWorld.getDispatcher();
		int manifoldCount = dispatcher.getNumManifolds();

		for (int i = 0; i < manifoldCount; i++) {
			PersistentManifold manifold = dispatcher.getManifoldByIndexInternal(i);

			if (manifold == null) {
				break;
			}

			CollisionObject ghostObject1 = (CollisionObject) manifold.getBody0();
			CollisionObject ghostObject2 = (CollisionObject) manifold.getBody1();

			boolean hit = false;
			for (int j = 0; j < manifold.getNumContacts(); j++) {
				ManifoldPoint contactPoint = manifold.getContactPoint(j);
				
				onCollisionEnter(contactPoint, ghostObject1, ghostObject2);
				onCollisionEnter(contactPoint, ghostObject2, ghostObject1);
			}

			if (hit) {
				// Collision happened between physicsObject1 and physicsObject2. Collision
				// normal is in variable 'normal'.
			}
		}
	}

	public static void destroyActor(Actor actor) {
		for (ActorComponent actorComp : actor.getActorComps()) {
			dynamicsWorld.removeRigidBody(actorComp.getRigidBody());
		}
		for (TypedConstraint constraint : actor.getConstraints()) {
			dynamicsWorld.removeConstraint(constraint);
		}
		actor.getBranchGroup().detach();
		actor = null;
	}

	private void onCollisionEnter(ManifoldPoint collisionPoint, CollisionObject collisionBody,
			CollisionObject opponent) {
		if (collisionBody.getUserPointer() == null) {
			return;
		}

		Class[] interfaces = collisionBody.getUserPointer().getClass().getInterfaces();
		for (Class interF : interfaces) {
			if (interF.getSimpleName().equals(OnCollisionEnter.class.getSimpleName())) {
				OnCollisionEnter colPros = (OnCollisionEnter) collisionBody.getUserPointer();
				colPros.onCollisionEnter(collisionPoint, collisionBody, opponent);
			}
		}
	}
}
