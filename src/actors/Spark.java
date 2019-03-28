package actors;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.universe.*;

import abstracts.Actor;
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

public class Spark extends Actor {
	TransformGroup transformGroup = new TransformGroup();
	
	TransformGroup cameraTransformGroup;
	
	double scale = 0f;
	double explodeSpeed = 0;
	double maxSize;
	
	boolean destroyFlg = false;
	
	public Spark(TransformGroup cameraTransformGroup, BranchGroup branchGroup, 
			double explodeSpeed, double maxSize, Vector3f position) {
		this.cameraTransformGroup = cameraTransformGroup;
		
		this.explodeSpeed = explodeSpeed;
		this.maxSize = maxSize;
		
		this.transformGroup.addChild(branchGroup);
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		BranchGroup branchGroupTemp = new BranchGroup();
		branchGroupTemp.setCapability(BranchGroup.ALLOW_DETACH);
		branchGroupTemp.addChild(transformGroup);
		setBranchGroup(branchGroupTemp);

		Transform3D transform3D = new Transform3D();
		transform3D.setTranslation(position);
		transformGroup.setTransform(transform3D);
	}
	
	public void step() {
		Transform3D cameraTrans = new Transform3D();
		cameraTransformGroup.getTransform(cameraTrans);
		
		Vector3f cameraPos = new Vector3f();
		cameraTrans.get(cameraPos);
		Point3d cameraPoint = new Point3d(cameraPos.x, cameraPos.y, cameraPos.z);
		
		Transform3D currentTransform = new Transform3D();
		transformGroup.getTransform(currentTransform);
		
		Vector3f currentPos = new Vector3f();
		currentTransform.get(currentPos);
		Point3d currentPoint = new Point3d(currentPos.x, currentPos.y, currentPos.z);
		
		currentTransform.lookAt(new Point3d(currentPos.x, currentPoint.y, currentPos.z), cameraPoint, new Vector3d(0, 0, 1));
		currentTransform.invert();
		
		currentTransform.setScale(scale);
		if (destroyFlg) {
			currentTransform.setScale(maxSize);
		}
		
		transformGroup.setTransform(currentTransform);
		
		scale = scale + explodeSpeed;
		
		if (scale > maxSize) {
			destroy();
			destroyFlg = true;
		}
	}
	
	public void destroy() {
		destroyFlg = true;
		
		Transform3D transform3D = new Transform3D();
		transform3D.setTranslation(new Vector3f(0, -1000, 0));
		transformGroup.setTransform(transform3D);
		
		World.destroyActor(this);
	}
}
