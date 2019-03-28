package actors;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.universe.*;

import abstracts.Actor;

import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.geometry.Box;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
import java.util.Timer;

public class Sun extends Actor {
	TransformGroup transformGroup = new TransformGroup();
	DirectionalLight directionalLight;
	
	public Sun() {
		directionalLight = new DirectionalLight(
				true, new Color3f(1.0f, 1.0f, 1.0f), new Vector3f(-0.5f, -0.5f, -1.0f)); 
		transformGroup.addChild(directionalLight);
		
		Transform3D sunTransform3D = new Transform3D();
		sunTransform3D.setRotation(new AxisAngle4f(0.0f, 1.0f, 0.0f, 1.0f));
		transformGroup.setTransform(sunTransform3D);
		
		directionalLight.setInfluencingBounds(new BoundingSphere(new Point3d(), 1000.0));
		
		getBranchGroup().addChild(transformGroup);
	}
}
