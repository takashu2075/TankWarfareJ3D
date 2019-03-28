package actors;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.universe.*;

import abstracts.Actor;
import main.World;
import tools.ObjLoader;
import utils.PerticleUtils;
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

public class Spark2 extends Actor {
	Shape3D explosionShape;
	Shape3D smokeShape;
	
	TransformGroup tansformGroup = new TransformGroup();
	
	double currentScale = 0;
	double initialScale = 0;
	double explodeSpeed = 0;
	double duration = 0;
	double stepCount = 0;
	
	boolean destroyFlg = false;
	
	public Spark2(Shape3D explosionShape, double initialScale, double explodeSpeed, int duration, Vector3f position) {
		this.explosionShape = explosionShape;
		this.smokeShape = smokeShape;
		
		this.initialScale = initialScale;
		this.explodeSpeed = explodeSpeed;
		this.duration = duration;
		
		tansformGroup.addChild(this.explosionShape);
		Transform3D transform3D = new Transform3D();
		transform3D.setTranslation(position);
		tansformGroup.setTransform(transform3D);
		tansformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		
		getBranchGroup().addChild(tansformGroup);
		
		updateScale(tansformGroup);
	}
	
	public void step() {
		currentScale = currentScale + explodeSpeed;
		
		updateScale(tansformGroup);
		
		updateTransparency(explosionShape);
		
		if (stepCount > duration) {
			destroy();
		}
		
		stepCount++;
	}
	
	public void destroy() {
		World.destroyActor(this);
	}
	
	private void updateScale(TransformGroup transformGroup) {
		Transform3D transform3D = new Transform3D();
		transformGroup.getTransform(transform3D);
		transform3D.setScale(currentScale);
		
		transformGroup.setTransform(transform3D);
	}
	
	private void updateTransparency(Shape3D shpae3D) {
		float transparency = ((float) stepCount / ((float) duration / 5));
		
		TransparencyAttributes transparencyAttributes = new TransparencyAttributes();
		transparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);
		transparencyAttributes.setTransparency(transparency);
		
		shpae3D.getAppearance().setTransparencyAttributes(transparencyAttributes);
	}
	
	public void setPosition(Vector3f position) {
		Transform3D transform3D = new Transform3D();
		transform3D.setTranslation(position);
		tansformGroup.setTransform(transform3D);
	}
}
