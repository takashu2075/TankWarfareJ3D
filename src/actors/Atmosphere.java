package actors;

import javax.swing.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.geometry.Sphere;
import javax.vecmath.*;

import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ExponentialFog;
import javax.media.j3d.Fog;
import javax.media.j3d.LinearFog;

import java.awt.GraphicsConfiguration;
import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.TransformGroup;
import com.sun.j3d.loaders.*;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;

import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

import com.sun.j3d.utils.image.TextureLoader;

import actors.Atmosphere;
import actors.Sun;
import actors.Tank;
import actors.Terrain;
import actors.Tower;
import controlers.PlayerTankController;
import main.World;
import tools.ObjLoader;

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

import abstracts.Actor;

public class Atmosphere extends Actor {
	private ExponentialFog atmosphericFog;
	private AmbientLight ambientLight;
	private Background background;
	
	Color3f FOG_COLOR = new Color3f(0.995f, 0.999f, 1.0f);
	String SKY_TEXTURE_PATH = "C:\\Eclipse\\workspace\\TankWarfareJ3D\\resources\\skybox\\sky.jpg";
	String SKY_MESH_PATH = "C:\\Eclipse\\workspace\\TankWarfareJ3D\\resources\\skybox\\model.obj";
	
	Color3f fogColor = new Color3f(0.6f, 0.86f, 1.0f);
	Color3f ambientColor = new Color3f(1f, 1f, 1.0f);
	float fogDensity = 0.005f;
	
	float atmosphereInfluenceRadius = 100000.0f;
	
	public Atmosphere() {
		atmosphericFog= new ExponentialFog(fogColor, fogDensity);
		ambientLight = new AmbientLight(ambientColor);
		background = new Background();
		
		BoundingSphere boundingSphere = new BoundingSphere();
		boundingSphere.setRadius(atmosphereInfluenceRadius);
		
		background.setApplicationBounds(boundingSphere);
		atmosphericFog.setInfluencingBounds(boundingSphere);
		ambientLight.setInfluencingBounds(boundingSphere);
		
		getBranchGroup().addChild(atmosphericFog);
		getBranchGroup().addChild(ambientLight);
		
		Background backGround = new Background();
		backGround.setApplicationBounds(new BoundingSphere(new Point3d(), 1000000.0));

		BranchGroup geometryBranchGroup = new BranchGroup();
		
		Appearance app = new Appearance(); 
		Texture tex = new TextureLoader(SKY_TEXTURE_PATH, World.getSimpleUniverse().getCanvas()).getTexture();
				
		app.setTexture( tex );
		Sphere skybox = new Sphere(1.0f, Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS_INWARD, app);
		
		geometryBranchGroup.addChild(skybox);
		
		backGround.setGeometry( geometryBranchGroup );
		
		getBranchGroup().addChild(backGround);
	}
}
