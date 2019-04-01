package makers;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Vector3f;

import com.sun.j3d.loaders.Scene;

import abstracts.Maker;
import actors.Flash;
import main.World;
import tools.ObjLoader;

public class FlashMaker extends Maker {
	String flashShapePath = "/explosion/model.obj";
	TransparencyAttributes transparencyAttributes;

	TransformGroup transformGroup = new TransformGroup();

	static BranchGroup branchGroup = new BranchGroup();

	static TransformGroup cameraTransformGroup;

	boolean destroyFlg = false;

	private static Scene flashModel;

	static World world;

	public FlashMaker(TransformGroup cameraTransformGroup, World world) {
		FlashMaker.world = world;
		FlashMaker.cameraTransformGroup = cameraTransformGroup;

		TransparencyAttributes explosionTransparencyAttributes = new TransparencyAttributes();
		TransparencyAttributes smokeTransparencyAttributes = new TransparencyAttributes();

		explosionTransparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);
		smokeTransparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);

		ObjLoader flashLoader = new ObjLoader();
		flashModel = flashLoader.load(flashShapePath);

		Transform3D transform3D = new Transform3D();
		transform3D.setTranslation(new Vector3f(0, 10, 0));

		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		Shape3D explosionShape = (Shape3D) flashModel.getSceneGroup().getChild(0);
		explosionShape.getAppearance().setTransparencyAttributes(explosionTransparencyAttributes);
		BranchGroup explosionScene = flashModel.getSceneGroup();

		transformGroup.addChild(explosionScene);

		transformGroup.setTransform(transform3D);

		branchGroup.addChild(transformGroup);

		branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
	}

	public static Flash instantiate(double explodeSpeed, double maxSize, Vector3f position) {
		BranchGroup newBranchGroup = (BranchGroup) branchGroup.cloneTree();
		return new Flash(cameraTransformGroup, newBranchGroup, explodeSpeed, maxSize, position);
	}

	public static void instantiateToWorld(double explodeSpeed, double maxSize, Vector3f position) {
		BranchGroup newBranchGroup = (BranchGroup) branchGroup.cloneTree();
		Flash muzzle = new Flash(cameraTransformGroup, newBranchGroup, explodeSpeed, maxSize, position);
		World.add(muzzle);
	}

	public BranchGroup getBranchGroup() {
		return branchGroup;
	}
}
