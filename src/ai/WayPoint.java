package ai;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

public class WayPoint {
	Vector3f position;
	ArrayList<WayPoint> connectedWaypoints = new ArrayList<WayPoint>();
	WayPoint nextWayPoint;

	public WayPoint(Vector3f position) {
		this.position = position;
	}

	public void addConnectedPoint(WayPoint wayPoint) {
		connectedWaypoints.add(wayPoint);
	}

	public ArrayList<WayPoint> getConnectedWayPoints() {
		return connectedWaypoints;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setNextWayPoint(WayPoint nextWayPoint) {
		this.nextWayPoint = nextWayPoint;
	}

	public WayPoint getNextWayPoint() {
		return nextWayPoint;
	}
}
