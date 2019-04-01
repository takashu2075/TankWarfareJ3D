package makers;

import javax.media.j3d.BranchGroup;
import javax.vecmath.Vector3f;

import com.sun.j3d.loaders.Scene;

import abstracts.Maker;
import actors.Tree;
import main.World;
import tools.ObjLoader;

public class TreeMaker extends Maker {
	String treeShapePath = "/tree/model.obj";

	private static BranchGroup treeShape;

	public TreeMaker() {
		ObjLoader objLoader = new ObjLoader();
		Scene treeModel = objLoader.load(treeShapePath);
		treeShape = treeModel.getSceneGroup();
	}

	public static Tree instantiate(Vector3f position) {
		BranchGroup clonedExplosionShape = (BranchGroup) treeShape.cloneTree();

		return new Tree(clonedExplosionShape, position);
	}

	public static void instantiateToWorld(Vector3f position) {
		BranchGroup clonedExplosionShape = (BranchGroup) treeShape.cloneTree();

		Tree tree = new Tree(clonedExplosionShape, position);

		World.add(tree);
	}
}
