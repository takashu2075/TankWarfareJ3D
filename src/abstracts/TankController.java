package abstracts;

import actors.Tank;

public abstract class TankController {
	Tank tank;
	
	public TankController(Tank tank) {
		this.tank = tank;
	}
	
	public abstract void step();
}
