package makers;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.universe.*;

import abstracts.Actor;
import abstracts.Maker;
import actors.ActorComponent;
import actors.Explosion;
import main.World;
import tools.ObjLoader;
import utils.ConversionUtils;

import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.image.TextureLoader;
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

public class ExplosionMaker extends Maker {
	
	TransformGroup explosionTransformGroup = new TransformGroup();
	TransformGroup smokeTransformGroup = new TransformGroup();
	
	BranchGroup explosion = new BranchGroup();
	
	static BranchGroup branchGroup = new BranchGroup();
	
	private static TransformGroup cameraTransformGroup;
	
	private static Shape3D explosionShape;
	private static Shape3D smokeShape;
	private static Shape3D debriShape;
	
	static BranchGroup debriBranchGroup;
	
	String EXPLOSION_TEXTURE_PATH = "C:\\Eclipse\\workspace\\TankWarfareJ3D\\resources\\explosion\\explosion_sphere.jpg";
	
	private static World world;
	
	public ExplosionMaker(TransformGroup cameraTransformGroup, Canvas3D canvas3D, World world) {
		ExplosionMaker.world = world;
		ExplosionMaker.cameraTransformGroup = cameraTransformGroup;
		
		TransparencyAttributes explosionTransparencyAttributes = new TransparencyAttributes();
		TransparencyAttributes smokeTransparencyAttributes = new TransparencyAttributes();
		
		explosionTransparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);
		smokeTransparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);
		
		ObjLoader objLoader1 = new ObjLoader();
		Scene explosionModel = objLoader1.load("C:\\Eclipse\\workspace\\TankWarfareJ3D\\resources\\explosion\\explosion_sphere.obj");
		ObjLoader objLoader2 = new ObjLoader();
		Scene smokeModel = objLoader2.load("C:\\Eclipse\\workspace\\TankWarfareJ3D\\resources\\smoke\\model.obj");
		ObjLoader objLoader3 = new ObjLoader();
		Scene debriModel = objLoader3.load("C:\\Eclipse\\workspace\\TankWarfareJ3D\\resources\\debri\\model.obj");
		
		Transform3D transform3D = new Transform3D();
		transform3D.setTranslation(new Vector3f(0, -1000, 0));
		
		explosionTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		smokeTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		explosionShape = (Shape3D) explosionModel.getSceneGroup().getChild(0);
		explosionShape.getAppearance().setTransparencyAttributes(explosionTransparencyAttributes);
		
		smokeShape = (Shape3D) smokeModel.getSceneGroup().getChild(0);
		smokeShape.getAppearance().setTransparencyAttributes(smokeTransparencyAttributes);
		
		debriShape = (Shape3D) debriModel.getSceneGroup().getChild(0);
		
		explosionShape.getAppearance().setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
		explosionShape.getAppearance().getTransparencyAttributes().setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
		
		smokeShape.getAppearance().setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
		smokeShape.getAppearance().getTransparencyAttributes().setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
		
		branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
	}
	
	public static Explosion instantiate(double initialScale, double explodeSpeed, int duration, Vector3f position) {
		Shape3D clonedExplosionShape = (Shape3D) explosionShape.cloneTree();
		Shape3D clonedSmokeShape = (Shape3D) smokeShape.cloneTree();
		Shape3D clonedDebriShape = (Shape3D) debriShape.cloneTree();
		
		return new Explosion(getCameraTransformGroup(), clonedExplosionShape, 
				clonedSmokeShape, clonedDebriShape, initialScale, explodeSpeed, duration, position);
	}
	
	public static void instantiateToWorld(double initialScale, double explodeSpeed, int duration, Vector3f position) {
		Shape3D clonedExplosionShape = (Shape3D) explosionShape.cloneTree();
		Shape3D clonedSmokeShape = (Shape3D) smokeShape.cloneTree();
		Shape3D clonedDebriShape = (Shape3D) debriShape.cloneTree();
		
		Explosion explosion = new Explosion(getCameraTransformGroup(), clonedExplosionShape, 
				clonedSmokeShape, clonedDebriShape, initialScale, explodeSpeed, duration, position);
		
		World.add(explosion);
	}

	public static World getWorld() {
		return world;
	}

	public static void setWorld(World world) {
		ExplosionMaker.world = world;
	}

	public static TransformGroup getCameraTransformGroup() {
		return cameraTransformGroup;
	}

	public static void setCameraTransformGroup(TransformGroup cameraTransformGroup) {
		ExplosionMaker.cameraTransformGroup = cameraTransformGroup;
	}
}
