package actors;

import javax.media.j3d.Transform3D;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.BoxShape;
import com.sun.j3d.loaders.Scene;

import abstracts.Actor;
import tools.ObjLoader;

public class Tower extends Actor {
	public Tower() {
		ObjLoader objLoader = new ObjLoader();
		Scene tankModel = null;
		tankModel = objLoader.load("/tower/20950_Tower_v1_NEW.obj");

		BoxShape boxShape = new BoxShape(new Vector3f(5.0f, 5.0f, 10.0f));
		Transform3D transform3D = new Transform3D();

		transform3D.setRotation(new Quat4f(-1, 0, 0, 1));
		transform3D.setTranslation(new Vector3f(-10, -2, -10));

		ActorComponent actorComponent = new ActorComponent(tankModel.getSceneGroup(), boxShape, transform3D, 0);

		addActorComp(actorComponent);
	}
}
