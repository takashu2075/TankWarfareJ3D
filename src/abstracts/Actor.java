package abstracts;

import java.util.ArrayList;

import javax.media.j3d.BranchGroup;
import javax.vecmath.Vector3f;

import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysics.linearmath.Transform;

import actors.ActorComponent;
import main.World;

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
		for (ActorComponent actorComponent : actorComps) {
			actorComponent.updateVisual();
		}
	}

	public Vector3f getChassisPosition() {
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
