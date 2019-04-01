package makers;

import com.bulletphysics.linearmath.Transform;

import abstracts.Maker;
import actors.Cannon;
import actors.Tank;
import main.World;

public class CannonMaker extends Maker {

	public CannonMaker() {
	}

	public static Cannon instantiate(Transform transform, Tank tank) {
		return new Cannon(transform, tank);
	}

	public static void instantiateToWorld(Transform transform, Tank tank) {
		Cannon cannon = new Cannon(transform, tank);
		World.add(cannon);
	}
}
