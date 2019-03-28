package actors;

import java.util.List;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;

import com.sun.j3d.utils.universe.*;

import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.geometry.*;
import com.bulletphysics.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.linearmath.*;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.dynamics.vehicle.*;

import abstracts.Actor;
import inter.OnCollisionEnter;
import main.World;
import makers.Spark2Maker;
import makers.SparkMaker;
import tools.ObjLoader;
import utils.MeshUtil;

public class Cannon extends Actor implements OnCollisionEnter {
	Tank tank;
	
	ActorComponent actorComp;
	
	int damage = 30;
	
	boolean isAlreadyBounded = false;
	
	public Cannon(Transform transform, Tank tank) {
		this.tank = tank;
		
		int mass = 10;
		
		//描画世界の外観を初期化
		Appearance appearance = new Appearance();
	    Material material = new Material();
	    material.setDiffuseColor(1.0f, 0.7f, 0.5f);
	    appearance.setMaterial(material);
	    
		Sphere bullet = new Sphere(1.5f, appearance);
		
		//力学世界の形状を初期化
		SphereShape bulletShape2 = new SphereShape(1.5f);
		
		//描画世界・力学世界それぞれに実態を持つ「パーツ」を作成
		actorComp = new ActorComponent(bullet, bulletShape2, transform, mass);
		
		//上記パーツをアクターに登録
		addActorComp(actorComp);
		
		actorComp.getRigidBody().setUserPointer(this);
	}
	
	public void destroy() {
		World.destroyActor(this);
	}
	
	public Tank getFiredBy() {
		return tank;
	}
	
	public int getDamage() {
		return damage;
	}
	
	public void applyImpulse(Vector3f impulse) {
		actorComp.getRigidBody().applyCentralImpulse(impulse);
	}
	
	public void step() {
		
	}

	@Override
	public void onCollisionEnter(ManifoldPoint collisionPoint, CollisionObject thisCollision, CollisionObject collided) {
		if (collisionPoint.appliedImpulse > 1000 || (isAlreadyBounded && collisionPoint.appliedImpulse > 1)) {
			destroy();
			
			Transform transform = new Transform();
			actorComp.getRigidBody().getMotionState().getWorldTransform(transform);
			
			Spark2Maker.instantiateToWorld(2, 0.05, 100, transform.origin);
			
			if (collided.getUserPointer() == null) {
				return;
			}
			
			if (collided.getUserPointer().getClass().equals(Tank.class)) {
				Tank hitTank = (Tank) collided.getUserPointer();
				hitTank.damage(30);
			}
			
		} else {
			isAlreadyBounded = true;
		}
	}
}
