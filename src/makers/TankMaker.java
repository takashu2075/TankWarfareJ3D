package makers;

import java.util.Timer;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Material;
import javax.media.j3d.Transform3D;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.dynamics.vehicle.DefaultVehicleRaycaster;
import com.bulletphysics.dynamics.vehicle.RaycastVehicle;
import com.bulletphysics.dynamics.vehicle.VehicleRaycaster;
import com.bulletphysics.dynamics.vehicle.VehicleTuning;
import com.bulletphysics.dynamics.vehicle.WheelInfo;
import com.bulletphysics.linearmath.Transform;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.geometry.Box;

import abstracts.Actor;
import abstracts.Maker;
import abstracts.TankController;
import actors.ActorComponent;
import actors.Tank;
import main.World;
import tools.ObjLoader;

public class TankMaker extends Maker {
	private static World world;
	
	private static BranchGroup bodyShape;
	private static BranchGroup turretShape;
	private static BranchGroup cannonShape;
	
	public TankMaker() {
		ObjLoader objLoader1 = new ObjLoader();
		Scene bodyModel = objLoader1.load("C:\\Eclipse\\workspace\\TankWarfareJ3D\\resources\\tank\\body.obj");
		bodyShape = bodyModel.getSceneGroup();
		
		ObjLoader objLoader2 = new ObjLoader();
		Scene turretModel = objLoader2.load("C:\\Eclipse\\workspace\\TankWarfareJ3D\\resources\\tank\\turret.obj");
		turretShape = turretModel.getSceneGroup();
		
		ObjLoader objLoader3 = new ObjLoader();
		Scene cannonModel = objLoader3.load("C:\\Eclipse\\workspace\\TankWarfareJ3D\\resources\\tank\\cannon.obj");
		cannonShape = cannonModel.getSceneGroup();
	}
	
	public static Tank instantiate(Vector3f spawnPosition) {
		Tank tank = new Tank(world, spawnPosition, (BranchGroup) bodyShape.cloneTree(), 
				(BranchGroup) turretShape.cloneTree(), (BranchGroup) cannonShape.cloneTree());
		return tank;
	}
	
	public static void instantiateToWorld(Vector3f spawnPosition) {
		Tank tank = new Tank(world, spawnPosition, (BranchGroup) bodyShape.cloneTree(), 
				(BranchGroup) turretShape.cloneTree(), (BranchGroup) cannonShape.cloneTree());
		World.add(tank);
	}
}
