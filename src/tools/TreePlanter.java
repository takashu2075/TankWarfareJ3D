package tools;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.Generic6DofConstraint;
import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.dynamics.constraintsolver.RotationalLimitMotor;
import com.bulletphysics.linearmath.Transform;

import actors.Tree;
import main.World;
import makers.TreeMaker;

public class TreePlanter {
	List<Vector3f> plantPositions = new ArrayList<Vector3f>();

	HingeConstraint chassisTurretHinge;

	public static void plantTrees(RigidBody terrain, List<Vector3f> treePositions) {

		for (Vector3f pos : treePositions) {

			Tree tree = TreeMaker.instantiate(pos);

			RigidBody treeRigidBody = tree.getActorComps().get(0).getRigidBody();

			Matrix4f terrainTransformMatrix4 = new Matrix4f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0);
			Matrix4f treeTransformMatrix4 = new Matrix4f(1, 0, 0, pos.x, 0, 1, 0, pos.y, 0, 0, 1, pos.z, 0, 0, 0, 0);
			Transform terrainTransform = new Transform(terrainTransformMatrix4);
			Transform treeTransform = new Transform(treeTransformMatrix4);

			Generic6DofConstraint constraint = new Generic6DofConstraint(terrain, treeRigidBody, terrainTransform,
					treeTransform, true);

			constraint.setAngularLowerLimit(new Vector3f(0f, 0f, 0f));
			constraint.setAngularUpperLimit(new Vector3f(0f, 0f, 0f));
			constraint.setLinearLowerLimit(pos);
			constraint.setLinearUpperLimit(pos);

			constraint.buildJacobian();
			tree.setConstraint(constraint);

			RotationalLimitMotor rr = constraint.getRotationalLimitMotor(0);

			rr.maxLimitForce = 1000000000;
			rr.limitSoftness = 0.00000000001f;
			rr.damping = 10;
			rr.enableMotor = true;

			World.add(tree);
		}
	}
}
