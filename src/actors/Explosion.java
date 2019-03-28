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

public class Explosion extends Actor {
	TransformGroup targetCameraTransformGroup;
	
	Shape3D explosionShape;
	Shape3D smokeShape;
	
	TransformGroup explosionTansformGroup = new TransformGroup();
	TransformGroup smokeTansformGroup = new TransformGroup();
	List<TransformGroup> debriTransformGroups = new ArrayList<TransformGroup>();
	
	double currentScale = 0;
	double initialScale = 0;
	double explodeSpeed = 0;
	double duration = 0;
	double stepCount = 0;
	
	boolean destroyFlg = false;
	
	BranchGroup debri;
	
	public Explosion(TransformGroup targetCameraTransformGroup, Shape3D explosionShape, 
			Shape3D smokeShape, Shape3D debriShape, double initialScale, double explodeSpeed, int duration, Vector3f position) {
		this.targetCameraTransformGroup = targetCameraTransformGroup;
		
		this.explosionShape = explosionShape;
		this.smokeShape = smokeShape;
		
		this.initialScale = initialScale;
		this.explodeSpeed = explodeSpeed;
		this.duration = duration;
		
		int NUM_OF_DEBRIS = 10;
		for (int i = 0; i < NUM_OF_DEBRIS; i++) {
			BoxShape boxShape = new BoxShape(new Vector3f(1, 1, 1));
			
			Transform3D debriTransform3D = new Transform3D();
			Vector3f debriPos = new Vector3f(position.x, position.y + 10, position.z);
			debriTransform3D.setTranslation(debriPos);
			
			int DEBRI_MASS = 1;
			ActorComponent actorComp = new ActorComponent(debriShape.cloneTree(), boxShape, debriTransform3D, DEBRI_MASS);
			
			addActorComp(actorComp);
		}
		
		explosionTansformGroup.addChild(this.explosionShape);
		smokeTansformGroup.addChild(this.smokeShape);
		
		Transform3D transform3D = new Transform3D();
		transform3D.setTranslation(position);
		
		explosionTansformGroup.setTransform(transform3D);
		smokeTansformGroup.setTransform(transform3D);
		
		explosionTansformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		smokeTansformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		
		getBranchGroup().addChild(explosionTansformGroup);
		getBranchGroup().addChild(smokeTansformGroup);
		
		currentScale = initialScale;
		
		updExplosionScale(explosionTansformGroup);
		updExplosionScale(smokeTansformGroup);
	}
	
	public void step() {
		Transform3D cameraTrans = new Transform3D();
		targetCameraTransformGroup.getTransform(cameraTrans);
		
		Vector3f cameraPos = new Vector3f();
		cameraTrans.get(cameraPos);
		
		PerticleUtils.loolAtCamera(smokeTansformGroup, targetCameraTransformGroup);
		
		currentScale = currentScale + explodeSpeed;
		
		updExplosionScale(explosionTansformGroup);
		updSmokeScale(smokeTansformGroup);
		
		updExplosionTransparency(explosionShape);
		updSmokeTransparency(smokeShape);
		
		if (stepCount > duration) {
			destroy();
		}
		
		stepCount++;
	}
	
	public void destroy() {
		World.destroyActor(this);
	}
	
	public void loolAtCamera(TransformGroup transformGroup) {
		Transform3D newTransform = new Transform3D();
		transformGroup.getTransform(newTransform);
		
		Vector3f currentPos = new Vector3f();
		newTransform.get(currentPos);
		Point3d currentPoint = new Point3d(currentPos.x, currentPos.y, currentPos.z);
		
		Transform3D transform3D = new Transform3D();
		targetCameraTransformGroup.getTransform(transform3D);
		
		Vector3f cameraPos = new Vector3f();
		transform3D.get(cameraPos);
		Point3d cameraPoint = new Point3d(cameraPos.x, cameraPos.y, cameraPos.z);
		
		newTransform.lookAt(new Point3d(currentPos.x, currentPoint.y, currentPos.z), cameraPoint, new Vector3d(0, 0, 1));
		newTransform.invert();
		
		transformGroup.setTransform(newTransform);
	}
	
	private void updExplosionScale(TransformGroup transformGroup) {
		Transform3D transform3D = new Transform3D();
		transformGroup.getTransform(transform3D);
		transform3D.setScale(currentScale);
		
		transformGroup.setTransform(transform3D);
	}
	
	private void updExplosionTransparency(Shape3D shpae3D) {
		float transparency = ((float) stepCount / ((float) duration / 5));
		
		TransparencyAttributes transparencyAttributes = new TransparencyAttributes();
		transparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);
		transparencyAttributes.setTransparency(transparency);
		
		shpae3D.getAppearance().setTransparencyAttributes(transparencyAttributes);
	}
	
	private void updSmokeScale(TransformGroup transformGroup) {
		Transform3D transform3D = new Transform3D();
		transformGroup.getTransform(transform3D);
		transform3D.setScale(currentScale / 3);
		
		transformGroup.setTransform(transform3D);
	}
	
	private void updSmokeTransparency(Shape3D shpae3D) {
		float transparency = ((float) stepCount / ((float) duration));
		
		TransparencyAttributes transparencyAttributes = new TransparencyAttributes();
		transparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);
		transparencyAttributes.setTransparency(transparency);
		
		shpae3D.getAppearance().setTransparencyAttributes(transparencyAttributes);
	}
}
