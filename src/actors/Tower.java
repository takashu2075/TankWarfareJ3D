package actors;

import javax.media.j3d.Transform3D;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.BoxShape;
import com.sun.j3d.loaders.Scene;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.universe.*;

import abstracts.Actor;
import main.World;

import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.geometry.Box;
import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.dynamics.vehicle.*;

import com.sun.j3d.loaders.*;
import com.sun.j3d.loaders.objectfile.ObjectFile;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.TransformAttribute;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
import java.util.Timer;

import com.bulletphysics.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.linearmath.*;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.dynamics.vehicle.*;

import abstracts.Actor;
import tools.ObjLoader;

public class Tower extends Actor{
	public Tower() {
		ObjLoader objLoader = new ObjLoader();
		Scene tankModel = null;		
		tankModel = objLoader.load(getClass().getClassLoader().getResource("tower/20950_Tower_v1_NEW.obj").toString());
		
		BoxShape boxShape = new BoxShape(new Vector3f(5.0f, 5.0f, 10.0f));
		Transform3D transform3D = new Transform3D();
		
		transform3D.setRotation(new Quat4f(-1, 0, 0, 1));
		transform3D.setTranslation(new Vector3f(-10, -2, -10));

		ActorComponent actorComponent = new ActorComponent(tankModel.getSceneGroup(), boxShape, transform3D, 0);
		
		addActorComp(actorComponent);
	}
}
