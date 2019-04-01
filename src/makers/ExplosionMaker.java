package makers;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Vector3f;

import com.sun.j3d.loaders.Scene;

import abstracts.Maker;
import actors.Explosion;
import main.World;
import tools.ObjLoader;

public class ExplosionMaker extends Maker {

	TransformGroup explosionTransformGroup = new TransformGroup();
	TransformGroup smokeTransformGroup = new TransformGroup();

	BranchGroup explosion = new BranchGroup();

	static BranchGroup branchGroup = new BranchGroup();

	private static TransformGroup cameraTransformGroup;

	String explosionShapePath = "/explosion/explosion_sphere.obj";
	String smokeShapePath = "/smoke/model.obj";
	String debriShapePath = "/debri/model.obj";

	private static Shape3D explosionShape;
	private static Shape3D smokeShape;
	private static Shape3D debriShape;

	static BranchGroup debriBranchGroup;

	private static World world;

	public ExplosionMaker(TransformGroup cameraTransformGroup, Canvas3D canvas3D, World world) {
		ExplosionMaker.world = world;
		ExplosionMaker.cameraTransformGroup = cameraTransformGroup;

		TransparencyAttributes explosionTransparencyAttributes = new TransparencyAttributes();
		TransparencyAttributes smokeTransparencyAttributes = new TransparencyAttributes();

		explosionTransparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);
		smokeTransparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);

		ObjLoader objLoader1 = new ObjLoader();
		Scene explosionModel = objLoader1.load(explosionShapePath);
		ObjLoader objLoader2 = new ObjLoader();
		Scene smokeModel = objLoader2.load(smokeShapePath);
		ObjLoader objLoader3 = new ObjLoader();
		Scene debriModel = objLoader3.load(debriShapePath);

		explosionTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		smokeTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		explosionShape = (Shape3D) explosionModel.getSceneGroup().getChild(0);
		explosionShape.getAppearance().setTransparencyAttributes(explosionTransparencyAttributes);

		System.out.println("çsÇØÇƒÇÒÇ∂Ç·ÇÀ");

		smokeShape = (Shape3D) smokeModel.getSceneGroup().getChild(0);
		smokeShape.getAppearance().setTransparencyAttributes(smokeTransparencyAttributes);

		debriShape = (Shape3D) debriModel.getSceneGroup().getChild(0);

		explosionShape.getAppearance().setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
		explosionShape.getAppearance().getTransparencyAttributes()
				.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);

		smokeShape.getAppearance().setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
		smokeShape.getAppearance().getTransparencyAttributes().setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);

		branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
	}

	public static Explosion instantiate(double initialScale, double explodeSpeed, int duration, Vector3f position) {
		return null;
	}

	public static void instantiateToWorld(double initialScale, double explodeSpeed, int duration, Vector3f position) {
		Appearance explosionAppearance = (Appearance) explosionShape.getAppearance().cloneNodeComponent(true);
		Shape3D clonedExplosionShape = (Shape3D) explosionShape.cloneTree();
		clonedExplosionShape.setAppearance(explosionAppearance);

		Appearance smokeAppearance = (Appearance) smokeShape.getAppearance().cloneNodeComponent(true);
		Shape3D clonedSmokeShape = (Shape3D) smokeShape.cloneTree();
		clonedSmokeShape.setAppearance(smokeAppearance);

		Shape3D clonedDebriShape = (Shape3D) debriShape.cloneTree();

		Explosion explosion = new Explosion(getCameraTransformGroup(), clonedExplosionShape, clonedSmokeShape,
				clonedDebriShape, initialScale, explodeSpeed, duration, position);

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
