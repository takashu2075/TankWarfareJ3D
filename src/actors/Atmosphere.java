package actors;

import java.net.URL;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ExponentialFog;
import javax.media.j3d.Texture;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;

import abstracts.Actor;
import main.World;

public class Atmosphere extends Actor {
	private ExponentialFog atmosphericFog;
	private AmbientLight ambientLight;
	private Background background;

	String skyTexturePath = "sky.jpg";

	Color3f fogColor = new Color3f(0.6f, 0.86f, 1.0f);
	Color3f ambientColor = new Color3f(1f, 1f, 1.0f);
	float fogDensity = 0.005f;

	float atmosphereInfluenceRadius = 100000.0f;

	public Atmosphere() {
		atmosphericFog = new ExponentialFog(fogColor, fogDensity);
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

		URL url = getClass().getClassLoader().getResource(skyTexturePath);

		Texture tex = new TextureLoader(url, World.getSimpleUniverse().getCanvas()).getTexture();

		app.setTexture(tex);
		Sphere skybox = new Sphere(1.0f, Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS_INWARD, app);

		geometryBranchGroup.addChild(skybox);

		backGround.setGeometry(geometryBranchGroup);

		getBranchGroup().addChild(backGround);
	}
}
