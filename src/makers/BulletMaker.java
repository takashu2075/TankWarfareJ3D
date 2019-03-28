package makers;

import java.util.List;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.universe.*;

import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.geometry.*;
import com.bulletphysics.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.linearmath.*;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.dynamics.vehicle.*;

import abstracts.Actor;
import abstracts.Maker;
import actors.ActorComponent;
import actors.Cannon;
import actors.Tank;
import inter.OnCollisionEnter;
import main.World;
import tools.ObjLoader;
import utils.MeshUtil;

public class BulletMaker extends Maker {
	
	public BulletMaker() {
	}
	
	public static Cannon instantiate(Transform transform, Tank tank) {
		return new Cannon(transform, tank);
	}
	
	public static void instantiateToWorld(Transform transform, Tank tank) {
		Cannon cannon = new Cannon(transform, tank);
		World.add(cannon);
	}
}
