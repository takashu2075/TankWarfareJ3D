package utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.bulletphysics.collision.shapes.IndexedMesh;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;

public class MeshUtil {
	public static TriangleIndexVertexArray verticesToPhysicsMesh(TriangleIndexVertexArray vertArray, float[] vts) {
		int[] ids = new int[vts.length / 3];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = i;
		}

		return verticesToPhysicsMesh(vertArray, vts, ids);
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
}
