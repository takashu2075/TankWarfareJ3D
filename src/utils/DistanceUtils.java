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

public class DistanceUtils {
	public static double getDistToTarget(Vector3f targetLocalPos) {
		double distanceXZ = Math.sqrt((targetLocalPos.x * targetLocalPos.x) + (targetLocalPos.z * targetLocalPos.z));
		return Math.sqrt((distanceXZ * distanceXZ) + (targetLocalPos.y * targetLocalPos.y));
	}
}
