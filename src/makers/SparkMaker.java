package makers;

import javax.media.j3d.Appearance;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Vector3f;

import com.sun.j3d.loaders.Scene;

import abstracts.Maker;
import actors.Spark;
import main.World;
import tools.ObjLoader;

public class SparkMaker extends Maker {
	private static Shape3D sparkShape;

	static String explosionMedelPath = "/explosion/explosion_sphere.obj";

	public SparkMaker() {
		TransparencyAttributes transparencyAttr = new TransparencyAttributes();
		transparencyAttr.setTransparencyMode(TransparencyAttributes.BLENDED);

		ObjLoader objLoader = new ObjLoader();
		Scene sparkModel = objLoader.load(explosionMedelPath);
		sparkShape = (Shape3D) sparkModel.getSceneGroup().getChild(0);
		sparkShape.getAppearance().setTransparencyAttributes(transparencyAttr);
		sparkShape.getAppearance().setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
		sparkShape.getAppearance().getTransparencyAttributes().setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
	}

	public static Spark instantiate(double initialScale, double explodeSpeed, int duration, Vector3f position) {
		return null;
	}

	public static void instantiateToWorld(double initialScale, double explodeSpeed, int duration, Vector3f position) {
		Appearance clonedSparkAppearance = (Appearance) sparkShape.getAppearance().cloneNodeComponent(true);
		Shape3D clonedSparkShape = (Shape3D) sparkShape.cloneTree();
		clonedSparkShape.setAppearance(clonedSparkAppearance);

		Spark spark = new Spark(clonedSparkShape, initialScale, explodeSpeed, duration, position);
		World.add(spark);
	}
}
