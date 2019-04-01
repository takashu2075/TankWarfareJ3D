package makers;

import javax.media.j3d.BranchGroup;
import javax.vecmath.Vector3f;

import com.sun.j3d.loaders.Scene;

import abstracts.Maker;
import actors.Tank;
import main.World;
import tools.ObjLoader;

public class TankMaker extends Maker {
	private static World world;

	private static BranchGroup bodyShape;
	private static BranchGroup turretShape;
	private static BranchGroup musszleShape;

	String bodyModelPath = "/tank/body.obj";
	String turretModelPath = "/tank/turret.obj";
	String muzzleModelPath = "/tank/cannon.obj";

	public TankMaker() {
		ObjLoader objLoader1 = new ObjLoader();
		Scene bodyModel = objLoader1.load(bodyModelPath);
		bodyShape = bodyModel.getSceneGroup();

		ObjLoader objLoader2 = new ObjLoader();
		Scene turretModel = objLoader2.load(turretModelPath);
		turretShape = turretModel.getSceneGroup();

		ObjLoader objLoader3 = new ObjLoader();
		Scene cannonModel = objLoader3.load(muzzleModelPath);
		musszleShape = cannonModel.getSceneGroup();
	}

	public static Tank instantiate(Vector3f spawnPosition) {
		Tank tank = new Tank(world, spawnPosition, (BranchGroup) bodyShape.cloneTree(),
				(BranchGroup) turretShape.cloneTree(), (BranchGroup) musszleShape.cloneTree());
		return tank;
	}

	public static void instantiateToWorld(Vector3f spawnPosition) {
		Tank tank = new Tank(world, spawnPosition, (BranchGroup) bodyShape.cloneTree(),
				(BranchGroup) turretShape.cloneTree(), (BranchGroup) musszleShape.cloneTree());
		World.add(tank);
	}
}
