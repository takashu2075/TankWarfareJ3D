package utils;

import javax.vecmath.Vector3f;
import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.universe.*;

import abstracts.Actor;
import abstracts.TankController;
import actors.ActorComponent;
import main.World;
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
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.linearmath.*;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.dynamics.vehicle.*;


public class ConversionUtils {
	public static double getTargetDirectionWorld(Vector3f targetPos, Vector3f playerPosition) {
		Vector3f heading = new Vector3f(targetPos.x - playerPosition.x, 
				targetPos.y - playerPosition.y, targetPos.z - playerPosition.z);
		heading.normalize();
		
		double targetDir = 0;
		if (heading.z >= 0) {
			targetDir = -Math.atan(heading.x / heading.z);
		} else {
			if (heading.x >= 0) {
				targetDir = -Math.atan(1.0f / 0.0f) + Math.atan(heading.z / heading.x);
			} else {
				targetDir = Math.atan(1.0f / 0.0f) + Math.atan(heading.z / heading.x);
			}
		}
		
		return targetDir;
	}
	
	public static double getTargetDirectionLocal(double playerDirection, double targetDirection) {
		double targetDirectionLocal = targetDirection - playerDirection;
		if (targetDirectionLocal >= 2 * Math.atan(1.0f / 0.0f)) {
			targetDirectionLocal = -4 * Math.atan(1.0f / 0.0f) + targetDirectionLocal;
		}
		if (targetDirectionLocal < -2 * Math.atan(1.0f / 0.0f)) {
			targetDirectionLocal = 4 * Math.atan(1.0f / 0.0f) + targetDirectionLocal;
		}
		
		return targetDirectionLocal;
	}
	
	public static Vector3f convPosWorld2Local(Transform transform, Vector3f worldPos) {
		Vector3f localPos = new Vector3f(worldPos.x, worldPos.y, worldPos.z);
		
		transform.inverse();
		transform.transform(localPos);
		
		return localPos;
	}
	
	public static Vector3f convPosLocal2World(Transform transform, Vector3f localPos) {
		Vector3f worldPos = new Vector3f(localPos.x, localPos.y, localPos.z);
		
		transform.transform(worldPos);
		
		return worldPos;
	}
	
	public static Vector3f convVecWorld2Local(Transform transform, Vector3f worldVec) {
		Vector3f localVec = new Vector3f(worldVec.x, worldVec.y, worldVec.z);
		
		transform.inverse();
		transform.basis.transform(localVec);
		
		return localVec;
	}
	
	public static Vector3f convVecLocal2World(Transform transform, Vector3f localVec) {
		Vector3f worldVec = new Vector3f(localVec.x, localVec.y, localVec.z);
		
		transform.basis.transform(worldVec);
		
		return worldVec;
	}
}
