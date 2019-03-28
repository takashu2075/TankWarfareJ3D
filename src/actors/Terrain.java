package actors;

import java.awt.Container;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.bulletphysics.util.ObjectArrayList;
import java.util.List;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Material;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4d;
import javax.vecmath.Vector4f;

import javax.vecmath.*;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;

import abstracts.Actor;
import tools.ObjLoader;

import com.bulletphysics.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.linearmath.*;

public class Terrain extends Actor{
	private static ByteBuffer gVertices;
	private static ByteBuffer gIndices;
//	private static int totalVerts = NUM_VERTS_X*NUM_VERTS_Y;
	
	public Terrain() {
		ObjLoader objLoader = new ObjLoader();
		Scene model = null;		
		model = objLoader.load("C:\\Eclipse\\workspace\\TankWarfareJ3D\\resources\\terrain\\model_2.obj");
		
		TriangleIndexVertexArray vertArray = new TriangleIndexVertexArray();
		float[] verticles = objLoader.getVerticles();
		List<int[]> indexes = objLoader.getIndexes();
		for (int[] index: indexes) {
			 verticesToPhysicsMesh(vertArray, verticles, index);
		}
		
		BvhTriangleMeshShape trimeshShape = new BvhTriangleMeshShape(vertArray, false);
		
		Transform3D transform3D = new Transform3D();
		
		transform3D.setTranslation(new Vector3f(0, 5, 0));
		
        ActorComponent actorComponent = new ActorComponent(model.getSceneGroup(), trimeshShape, transform3D, 0);
        actorComponent.getRigidBody().setUserPointer(this);
        addActorComp(actorComponent);
	}
	
	public static TriangleIndexVertexArray verticesToPhysicsMesh(TriangleIndexVertexArray vertArray, float[] vts, int[] ids) {
		IndexedMesh indexedMesh = new IndexedMesh();
		indexedMesh.numTriangles = ids.length / 3;
		indexedMesh.triangleIndexBase = ByteBuffer.allocateDirect(ids.length * 4).order(ByteOrder.nativeOrder());
		indexedMesh.triangleIndexBase.asIntBuffer().put(ids);
		indexedMesh.triangleIndexStride = 3 * 4;
		indexedMesh.numVertices = vts.length;
		indexedMesh.vertexBase = ByteBuffer.allocateDirect(vts.length * 4).order(ByteOrder.nativeOrder());
		indexedMesh.vertexBase.asFloatBuffer().put(vts);
		indexedMesh.vertexStride = 3 * 4;
		
//		TriangleIndexVertexArray vertArray = new TriangleIndexVertexArray();
		vertArray.addIndexedMesh(indexedMesh);
		return vertArray;
	}
	
	public static TriangleIndexVertexArray verticesToPhysicsMesh(TriangleIndexVertexArray vertArray, float[] vts){
		int[] ids = new int[vts.length / 3];
		for (int i = 0; i < ids.length; i++){
			ids[i] = i;
		}
		
		return verticesToPhysicsMesh(vertArray, vts, ids);
	}
}
