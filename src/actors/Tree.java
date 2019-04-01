package actors;

import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.shapes.CylinderShape;
import com.bulletphysics.dynamics.constraintsolver.Generic6DofConstraint;

import abstracts.Actor;
import inter.OnCollisionEnter;
import main.World;

public class Tree extends Actor implements OnCollisionEnter {
	BranchGroup treeShape;

	TransformGroup transformGroup = new TransformGroup();
	TransformGroup smokeTansformGroup = new TransformGroup();
	List<TransformGroup> debriTransformGroups = new ArrayList<TransformGroup>();

	ActorComponent actorComp;

	Generic6DofConstraint constraint;

	public Tree(BranchGroup treeShape, Vector3f position) {
		this.treeShape = treeShape;

		Vector3f cylinderDimention = new Vector3f(1, 5, 1);
		CylinderShape physicalShape = new CylinderShape(cylinderDimention);

		Transform3D trans = new Transform3D();
		trans.setTranslation(position);

		actorComp = new ActorComponent(treeShape, physicalShape, trans, 100);
		addActorComp(actorComp);

		actorComp.getRigidBody().setUserPointer(this);
	}

	public void setConstraint(Generic6DofConstraint constraint) {
		this.constraint = constraint;
		addConstraint(constraint);
	}

	public void step() {
		System.out.println(constraint.getAppliedImpulse());
		if (constraint.getAppliedImpulse() > 100) {
			World.getDynamicsWorld().removeConstraint(constraint);
		}
	}

	@Override
	public void onCollisionEnter(ManifoldPoint collisionPoint, CollisionObject thisCollidion,
			CollisionObject collided) {
		int impulseBreak = 1;

		if (impulseBreak >= collisionPoint.appliedImpulse) {
			World.getDynamicsWorld().removeConstraint(constraint);
		}
	}
}
