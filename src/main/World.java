package main;

import java.util.ArrayList;

import javax.media.j3d.Canvas3D;
import javax.media.j3d.TransformGroup;

import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.InternalTickCallback;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysics.linearmath.Clock;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

import abstracts.Actor;
import actors.ActorComponent;
import inter.OnCollisionEnter;
import makers.CannonMaker;
import makers.ExplosionMaker;
import makers.FlashMaker;
import makers.SparkMaker;
import makers.TreeMaker;

public class World {
	static SimpleUniverse simpleUniverse;

	static DynamicsWorld dynamicsWorld;

	static ArrayList<Actor> actors;

	Clock clock = new Clock();

	TransformGroup cameraTransformGroup;

	ExplosionMaker explosionMaker;
	FlashMaker muzzleMaker;
	CannonMaker bulletMaker;
	SparkMaker sparkMaker;
	TreeMaker treeMaker;

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
		muzzleMaker = new FlashMaker(cameraTransformGroup, this);
		sparkMaker = new SparkMaker();
		treeMaker = new TreeMaker();
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
		muzzleMaker = new FlashMaker(cameraTransformGroup, this);
		bulletMaker = new CannonMaker();
		sparkMaker = new SparkMaker();
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
