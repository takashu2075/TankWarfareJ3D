package makers;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.universe.*;

import abstracts.Actor;
import abstracts.Maker;
import actors.ActorComponent;
import actors.Explosion;
import actors.Spark2;
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

public class Spark2Maker extends Maker {
	private static Shape3D explosionShape;
	
	String EXPLOSION_TEXTURE_PATH = "explosion\\explosion_sphere.jpg";
	
	public Spark2Maker() {
		TransparencyAttributes explosionTransparencyAttributes = new TransparencyAttributes();
		explosionTransparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);
		
		ObjLoader objLoader1 = new ObjLoader();
		Scene explosionModel = objLoader1.load(getClass().getClassLoader().getResource("explosion/explosion_sphere.obj").toString());
		explosionShape = (Shape3D) explosionModel.getSceneGroup().getChild(0);
		explosionShape.getAppearance().setTransparencyAttributes(explosionTransparencyAttributes);
		explosionShape.getAppearance().setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
		explosionShape.getAppearance().getTransparencyAttributes().setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
	}
	
	public static Spark2 instantiate(double initialScale, double explodeSpeed, int duration, Vector3f position) {
		Shape3D clonedExplosionShape = (Shape3D) explosionShape.cloneTree();
		
		TransparencyAttributes explosionTransparencyAttributes = new TransparencyAttributes();
		explosionTransparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);
		
		Appearance appearance = new Appearance();
		appearance.setTransparencyAttributes(explosionTransparencyAttributes);
		appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
		appearance.getTransparencyAttributes().setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
		
		clonedExplosionShape.setAppearance(appearance);
		
		return new Spark2(clonedExplosionShape, initialScale, explodeSpeed, duration, position);
	}
	
	public static void instantiateToWorld(double initialScale, double explodeSpeed, int duration, Vector3f position) {
		Shape3D clonedExplosionShape = (Shape3D) explosionShape.cloneTree();
		
		Appearance appearance = (Appearance) explosionShape.getAppearance().cloneNodeComponent(true);
		
		clonedExplosionShape.setAppearance(appearance);
		
		Spark2 explosion = new Spark2(clonedExplosionShape, initialScale, explodeSpeed, duration, position);
		
		World.add(explosion);
	}
}
