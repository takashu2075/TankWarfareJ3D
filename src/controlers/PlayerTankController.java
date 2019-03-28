package controlers;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import abstracts.TankController;
import actors.Tank;
import utils.ConversionUtils;
import utils.DirectionUtils;

public class PlayerTankController extends TankController implements KeyListener {
	private float viewRotX;
	
	TransformGroup transformGroup;
	
	TransformGroup cameraTransformGroup;
	
	Tank tank;
	
	boolean isMoveForwardKeyPressed = false;
	boolean isMoveBackwardKeyPressed = false;
	boolean isTurnRightKeyPressed = false;
	boolean isTurnLeftKeyPressed = false;
	
	boolean isLookUpKeyPressed = false;
	boolean isLookDownKeyPressed = false;
	boolean isLookRightKeyPressed = false;
	boolean isLookLeftKeyPressed = false;
	
	boolean fireFlg = false;
	
	public Tank debugTank;
	
	public PlayerTankController(Tank tank, TransformGroup cameraTransformGroup) {
		super(tank);
		this.tank = tank;
		
		transformGroup = new TransformGroup();
		
		this.cameraTransformGroup = cameraTransformGroup;
	}
	
	public void setViewAngle() {
		if (!tank.isDestroyed()) {
			cameraTransformGroup.setTransform(tank.getCameraTransform3D());
		}
	}
	
	public void step() {
		moveTank();
		rotateTurret();
		rotateCannon();
		
		if (fireFlg) {
			tank.fire();
		}
		fireFlg = false;
	}
	
	private void moveTank() {
		if (isMoveForwardKeyPressed && isTurnRightKeyPressed) {
			tank.moveForwardRight();
		} else if (isMoveForwardKeyPressed && isTurnLeftKeyPressed) {
			tank.moveForwardLeft();
		} else if (isMoveBackwardKeyPressed && isTurnRightKeyPressed) {
			tank.moveBackwardRight();
		} else if (isMoveBackwardKeyPressed && isTurnLeftKeyPressed) {
			tank.moveBackwardLeft();
		} else if (isMoveForwardKeyPressed) {
			tank.moveForward();
		} else if (isTurnRightKeyPressed) {
			tank.turnRight();
		} else if (isTurnLeftKeyPressed) {
			tank.turnLeft();
		} else if (isMoveBackwardKeyPressed) {
			tank.moveBackward();
		} else {
			tank.brake();
		}
	}
	
	private void rotateTurret() {
		if (isLookRightKeyPressed) {
			tank.rotateTurretRight();
		} else if (isLookLeftKeyPressed) {
			tank.rotateTurretLeft();
		}
	}
	
	private void rotateCannon() {
		if (isLookUpKeyPressed) {
			tank.rotateMuzzleUp();
		} else if (isLookDownKeyPressed) {
			tank.rotateMuzzleDown();;
		}
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyText(e.getKeyCode()).equals("W")) {
			isMoveForwardKeyPressed = true;
		} else if (e.getKeyText(e.getKeyCode()).equals("S")) {
			isMoveBackwardKeyPressed = true;
		} else if (e.getKeyText(e.getKeyCode()).equals("D")) {
			isTurnRightKeyPressed = true;
		} else if (e.getKeyText(e.getKeyCode()).equals("A")) {
			isTurnLeftKeyPressed = true;
		} else if (e.getKeyCode() == 38) {
			isLookUpKeyPressed = true;
		} else if (e.getKeyCode() == 40) {
			isLookDownKeyPressed = true;
		} else if (e.getKeyCode() == 39) {
			isLookRightKeyPressed = true;
		} else if (e.getKeyCode() == 37) {
			isLookLeftKeyPressed = true;
		}
		
		if (e.getKeyCode() == 32) {
			fireFlg = true;
		}
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyText(e.getKeyCode()).equals("W")) {
			isMoveForwardKeyPressed = false;
		} else if (e.getKeyText(e.getKeyCode()).equals("S")) {
			isMoveBackwardKeyPressed = false;
		} else if (e.getKeyText(e.getKeyCode()).equals("D")) {
			isTurnRightKeyPressed = false;
		} else if (e.getKeyText(e.getKeyCode()).equals("A")) {
			isTurnLeftKeyPressed = false;
		} else if (e.getKeyCode() == 38) {
			isLookUpKeyPressed = false;
			tank.stopMuzzleRotation();
		} else if (e.getKeyCode() == 40) {
			isLookDownKeyPressed = false;
			tank.stopMuzzleRotation();
		} else if (e.getKeyCode() == 39) {
			isLookRightKeyPressed = false;
			tank.stopTurretRotation();
		} else if (e.getKeyCode() == 37) {
			isLookLeftKeyPressed = false;
			tank.stopTurretRotation();
		}
	}

	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}
}
