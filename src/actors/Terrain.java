package actors;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import javax.media.j3d.Transform3D;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.IndexedMesh;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.sun.j3d.loaders.Scene;

import abstracts.Actor;
import tools.ObjLoader;

public class Terrain extends Actor {
	private static ByteBuffer gVertices;
	private static ByteBuffer gIndices;
//	private static int totalVerts = NUM_VERTS_X*NUM_VERTS_Y;

	String terrainModelPath = "/terrain/model_2.obj";

	public Terrain() {
		ObjLoader objLoader = new ObjLoader();
		Scene model = null;
		model = objLoader.load(terrainModelPath);

		TriangleIndexVertexArray vertArray = new TriangleIndexVertexArray();
		float[] verticles = objLoader.getVerticles();
		List<int[]> indexes = objLoader.getIndexes();
		for (int[] index : indexes) {
			verticesToPhysicsMesh(vertArray, verticles, index);
		}

		BvhTriangleMeshShape trimeshShape = new BvhTriangleMeshShape(vertArray, false);

		Transform3D transform3D = new Transform3D();

		transform3D.setTranslation(new Vector3f(0, 5, 0));

		ActorComponent actorComponent = new ActorComponent(model.getSceneGroup(), trimeshShape, transform3D, 0);
		actorComponent.getRigidBody().setUserPointer(this);
		addActorComp(actorComponent);
	}

	public static TriangleIndexVertexArray verticesToPhysicsMesh(TriangleIndexVertexArray vertArray, float[] vts,
			int[] ids) {
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

	public static TriangleIndexVertexArray verticesToPhysicsMesh(TriangleIndexVertexArray vertArray, float[] vts) {
		int[] ids = new int[vts.length / 3];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = i;
		}

		return verticesToPhysicsMesh(vertArray, vts, ids);
	}
}
