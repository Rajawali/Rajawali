package org.rajawali3d.examples.examples.interactive.planes;

import android.graphics.Color;
import android.opengl.GLES20;

import org.rajawali3d.BufferInfo;
import org.rajawali3d.Geometry3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * This example shows how you can create a large number of textured planes efficiently.
 * The slow way is creating 2000 Plane objects and 16 separate textures. The optimized way
 * is to create one BaseObject3D with the vertex data for 2000 planes in one buffer (and
 * the same for tex coord data, normal data, etc). Each single plane is given the same position
 * at (0, 0, 0). Extra buffers are created for each plane's position and rotation.
 *
 * Only one texture is used. It's a 1024*1024 bitmap containing 16 256*256 images. This is
 * called a 'texture atlas'. Each plane is assigned a specific portion of this texture.
 *
 * This is much faster than creating separate object and textures because the shader program
 * needs to be created once, only one texture has to be uploaded, matrix transformations need
 * to be done only once on the cpu, etc.
 *
 * @author dennis.ippel
 *
 */
public class PlanesGalore extends Object3D {

	protected FloatBuffer mPlanePositions;
	protected FloatBuffer mRotationSpeeds;
	protected BufferInfo mPlanePositionsBufferInfo;
	protected BufferInfo mRotationSpeedsBufferInfo;
	protected Material mGaloreMat;
	protected PlanesGaloreMaterialPlugin mMaterialPlugin;

	public PlanesGalore() {
		super();
		mPlanePositionsBufferInfo = new BufferInfo();
		mRotationSpeedsBufferInfo = new BufferInfo();
		init();
	}

	public void init() {
		mGaloreMat = new Material();
		mGaloreMat.enableTime(true);

		mMaterialPlugin = new PlanesGaloreMaterialPlugin();
		mGaloreMat.addPlugin(mMaterialPlugin);

		setMaterial(mGaloreMat);
		final int numPlanes = 2000;
		final float planeSize = .3f;

		int numVertices = numPlanes * 4;
		float[] vertices = new float[numVertices * 3];
		float[] textureCoords = new float[numVertices * 2];
		float[] normals = new float[numVertices * 3];
		float[] planePositions = new float[numVertices * 3];
		float[] rotationSpeeds = new float[numVertices];
		float[] colors = new float[numVertices * 4];
		int[] indices = new int[numPlanes * 6];

		for (int i = 0; i < numPlanes; ++i) {
			Vector3 r = new Vector3(-10f + (Math.random() * 20f), -10 + (Math.random() * 20f), (Math.random() * 80f));
			int randColor = 0xff000000 + (int) (0xffffff * Math.random());

			int vIndex = i * 4 * 3;
			vertices[vIndex + 0] = -planeSize;
			vertices[vIndex + 1] = planeSize;
			vertices[vIndex + 2] = 0;
			vertices[vIndex + 3] = planeSize;
			vertices[vIndex + 4] = planeSize;
			vertices[vIndex + 5] = 0;
			vertices[vIndex + 6] = planeSize;
			vertices[vIndex + 7] = -planeSize;
			vertices[vIndex + 8] = 0;
			vertices[vIndex + 9] = -planeSize;
			vertices[vIndex + 10] = -planeSize;
			vertices[vIndex + 11] = 0;

			for (int j = 0; j < 12; j += 3) {
				normals[vIndex + j] = 0;
				normals[vIndex + j + 1] = 0;
				normals[vIndex + j + 2] = 1;

				planePositions[vIndex + j] = (float) r.x;
				planePositions[vIndex + j + 1] = (float) r.y;
				planePositions[vIndex + j + 2] = (float) r.z;
			}

			vIndex = i * 4 * 4;

			for (int j = 0; j < 16; j += 4) {
				colors[vIndex + j] = Color.red(randColor) / 255f;
				colors[vIndex + j + 1] = Color.green(randColor) / 255f;
				colors[vIndex + j + 2] = Color.blue(randColor) / 255f;
				colors[vIndex + j + 3] = 1.0f;
			}

			vIndex = i * 4 * 2;

			float u1 = .25f * (int) Math.floor(Math.random() * 4f);
			float v1 = .25f * (int) Math.floor(Math.random() * 4f);
			float u2 = u1 + .25f;
			float v2 = v1 + .25f;

			textureCoords[vIndex + 0] = u2;
			textureCoords[vIndex + 1] = v1;
			textureCoords[vIndex + 2] = u1;
			textureCoords[vIndex + 3] = v1;
			textureCoords[vIndex + 4] = u1;
			textureCoords[vIndex + 5] = v2;
			textureCoords[vIndex + 6] = u2;
			textureCoords[vIndex + 7] = v2;

			vIndex = i * 4;
			int iindex = i * 6;
			indices[iindex + 0] = (short) (vIndex + 0);
			indices[iindex + 1] = (short) (vIndex + 1);
			indices[iindex + 2] = (short) (vIndex + 3);
			indices[iindex + 3] = (short) (vIndex + 1);
			indices[iindex + 4] = (short) (vIndex + 2);
			indices[iindex + 5] = (short) (vIndex + 3);

			float rotationSpeed = -1f + (float) (Math.random() * 2f);
			rotationSpeeds[vIndex + 0] = rotationSpeed;
			rotationSpeeds[vIndex + 1] = rotationSpeed;
			rotationSpeeds[vIndex + 2] = rotationSpeed;
			rotationSpeeds[vIndex + 3] = rotationSpeed;
		}

		setData(vertices, normals, textureCoords, colors, indices, true);

		mPlanePositions = ByteBuffer.allocateDirect(planePositions.length * Geometry3D.FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mPlanePositions.put(planePositions);

		mRotationSpeeds = ByteBuffer.allocateDirect(rotationSpeeds.length * Geometry3D.FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mRotationSpeeds.put(rotationSpeeds);

		createBuffers();
	}

	private void createBuffers() {
		mPlanePositionsBufferInfo.buffer = mPlanePositions;
		mRotationSpeedsBufferInfo.buffer = mRotationSpeeds;
		mGeometry.addBuffer(mPlanePositionsBufferInfo, Geometry3D.BufferType.FLOAT_BUFFER, GLES20.GL_ARRAY_BUFFER);
		mGeometry.addBuffer(mRotationSpeedsBufferInfo, Geometry3D.BufferType.FLOAT_BUFFER, GLES20.GL_ARRAY_BUFFER);

		mMaterialPlugin.setPlanePositions(mPlanePositionsBufferInfo.bufferHandle);
		mMaterialPlugin.setRotationSpeeds(mRotationSpeedsBufferInfo.bufferHandle);
	}

	public void reload() {
		super.reload();
		createBuffers();
	}

	public PlanesGaloreMaterialPlugin getMaterialPlugin()
	{
		return mMaterialPlugin;
	}
}
