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

public class DirectionUtils {
	public static final float PI = 3.14159265358979323846f;
	public static final float PI_2 = 1.57079632679489661923f;
	public static final float PI_4 = 0.785398163397448309616f;
	
	public static double getDirToTargetX(Vector3f targetLocalPos) {
		double angle = Math.atan(targetLocalPos.x / targetLocalPos.z);
		
		if (targetLocalPos.z < 0) {
			if (targetLocalPos.x < 0) {
				angle = -PI + angle;
			} else {
				angle = PI + angle;
			}
		}
		
		return angle;
	}
	
	public static double getDirToTargetY(Vector3f targetLocalPos) {
		double angle = Math.atan(targetLocalPos.y / targetLocalPos.z);
		
		if (targetLocalPos.z < 0) {
			if (targetLocalPos.y < 0) {
				angle = -PI + angle;
			} else {
				angle = PI + angle;
			}
		}
		
		return angle;
	}
}
