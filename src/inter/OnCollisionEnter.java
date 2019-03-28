package inter;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;

import com.sun.j3d.utils.universe.*;

import abstracts.Actor;
import abstracts.TankController;
import actors.ActorComponent;
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

public interface OnCollisionEnter {
	public void onCollisionEnter(ManifoldPoint collisionPoint, CollisionObject thisCollidion, CollisionObject collided);
}
