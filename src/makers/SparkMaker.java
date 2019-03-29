package makers;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.universe.*;

import abstracts.Actor;
import abstracts.Maker;
import actors.ActorComponent;
import actors.Spark;
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

public class SparkMaker extends Maker {
	TransparencyAttributes transparencyAttributes; 
	
	TransformGroup transformGroup1 = new TransformGroup();
	TransformGroup transformGroup2 = new TransformGroup();
	TransformGroup transformGroup3 = new TransformGroup();
	TransformGroup transformGroup4 = new TransformGroup();
	
	BranchGroup explosion = new BranchGroup();
	
	static BranchGroup branchGroup = new BranchGroup();
	
	static TransformGroup cameraTransformGroup;
	
	boolean destroyFlg = false;
	
	private static Scene explosionModel;
	private static Scene smokeModel;
	
	static World world;
	
	public SparkMaker(TransformGroup cameraTransformGroup, World world) {
		SparkMaker.world = world;
		SparkMaker.cameraTransformGroup = cameraTransformGroup;
		
		TransparencyAttributes explosionTransparencyAttributes = new TransparencyAttributes();
		TransparencyAttributes smokeTransparencyAttributes = new TransparencyAttributes();
		
		explosionTransparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);
		smokeTransparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);
		
		ObjLoader objLoader1 = new ObjLoader();
		explosionModel = objLoader1.load(getClass().getClassLoader().getResource("explosion/model.obj").toString());
		ObjLoader objLoader2 = new ObjLoader();
		smokeModel = objLoader2.load(getClass().getClassLoader().getResource("smoke/model.obj").toString());
		
		Transform3D transform3D = new Transform3D();
		transform3D.setTranslation(new Vector3f(0, 10, 0));
		
		transformGroup1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		transformGroup2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		Shape3D explosionShape = (Shape3D) explosionModel.getSceneGroup().getChild(0);
		explosionShape.getAppearance().setTransparencyAttributes(explosionTransparencyAttributes);
		BranchGroup explosionScene = explosionModel.getSceneGroup();
		
		Shape3D smokeShape = (Shape3D) smokeModel.getSceneGroup().getChild(0);
		smokeShape.getAppearance().setTransparencyAttributes(smokeTransparencyAttributes);
		BranchGroup smokeScene = smokeModel.getSceneGroup();
		
		transformGroup1.addChild(explosionScene);
		transformGroup2.addChild(smokeScene);
		
		transformGroup1.setTransform(transform3D);
		transformGroup2.setTransform(transform3D);
		
		branchGroup.addChild(transformGroup1);
		branchGroup.addChild(transformGroup2);
		
		branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
	}
	
	
	public static Spark instantiate(double explodeSpeed, double maxSize, Vector3f position) {
		BranchGroup newBranchGroup = (BranchGroup) branchGroup.cloneTree();
		return new Spark(cameraTransformGroup, newBranchGroup, explodeSpeed, maxSize, position);
	}
	
	public static void instantiateToWorld(double explodeSpeed, double maxSize, Vector3f position) {
		BranchGroup newBranchGroup = (BranchGroup) branchGroup.cloneTree();
		Spark muzzle = new Spark(cameraTransformGroup, newBranchGroup, explodeSpeed, maxSize, position);
		World.add(muzzle);
	}
	
	public BranchGroup getBranchGroup() {
		return branchGroup;
	}
}
