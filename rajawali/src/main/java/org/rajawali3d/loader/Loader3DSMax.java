/**
 * Copyright 2013 Dennis Ippel
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d.loader;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.RajLog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * 3DS object parser. This is a work in progress. Materials aren't parsed yet.
 *
 * @author dennis.ippel
 * @author lacasrac
 *
 */
public class Loader3DSMax extends AMeshLoader {

    private final int IDENTIFIER_3DS = 0x4D4D;
    private final int MESH_BLOCK = 0x3D3D;
    private final int OBJECT_BLOCK = 0x4000;
    private final int TRIMESH = 0x4100;
    private final int VERTICES = 0x4110;
    private final int FACES = 0x4120;
    private final int TEXCOORD = 0x4140;
    private final int TEX_MAP = 0xA200;
    private final int TRI_MATERIAL = 0x4130;
    private final int TEX_NAME = 0xA000;
    private final int TEX_FILENAME = 0xA300;
    private final int MATERIAL = 0xAFFF;

    private ArrayList<ArrayList<Vector3>> mVertices = new ArrayList<ArrayList<Vector3>>();
    private ArrayList<Vector3[]> mNormals = new ArrayList<Vector3[]>();
    private ArrayList<ArrayList<Vector3>> mVertNormals = new ArrayList<ArrayList<Vector3>>();
    private ArrayList<ArrayList<Vector3>> mTexCoords = new ArrayList<ArrayList<Vector3>>();
    private ArrayList<ArrayList<Integer>> mIndices = new ArrayList<ArrayList<Integer>>();
    private ArrayList<String> mObjNames = new ArrayList<String>();

    private int mChunkID;
    private int mChunkEndOffset;
    private boolean mEndReached = false;
    private int mObjects = -1;

    public Loader3DSMax(Renderer renderer, int resourceID) {
        super(renderer.getContext().getResources(), renderer.getTextureManager(), resourceID);
    }

    public Loader3DSMax(Renderer renderer, File file) {
        super(renderer, file);
    }

    @Override
    public AMeshLoader parse() throws ParsingException {
        RajLog.i("Start parsing 3DS");

        final InputStream stream;
        if (mFile == null) {
            stream = new BufferedInputStream(mResources.openRawResource(mResourceId));
        } else {
            try {
                stream = new BufferedInputStream(new FileInputStream(mFile));
            } catch (Exception e) {
                throw new ParsingException(e);
            }
        }

        try {
            readHeader(stream);
            if (mChunkID != IDENTIFIER_3DS) {
                RajLog.e("Not a valid 3DS file");
                return null;
            }

            while (!mEndReached) {
                readChunk(stream);
            }

            build();
            if (mRootObject.getNumChildren() == 1)
                mRootObject = mRootObject.getChildAt(0);

            stream.close();

            RajLog.i("End parsing 3DS");
        } catch (IOException e) {
            RajLog.e("Error parsing");
            throw new ParsingException(e);
        }

        return this;
    }

    void readChunk(InputStream stream) throws IOException {
        readHeader(stream);

        switch (mChunkID) {
            case MESH_BLOCK:
                break;
            case OBJECT_BLOCK:
                mObjects++;
                mObjNames.add(readString(stream));
                break;
            case TRIMESH:
                break;
            case VERTICES:
                readVertices(stream);
                break;
            case FACES:
                readFaces(stream);
                break;
            case TEXCOORD:
                readTexCoords(stream);
                break;
            case TEX_NAME:
                // mCurrentMaterialKey = readString(stream);
                skipRead(stream);
                break;
            case TEX_FILENAME:
                // String fileName = readString(stream);
                // StringBuffer texture = new StringBuffer(packageID);
                // texture.append(":drawable/");
                //
                // StringBuffer textureName = new StringBuffer(fileName.toLowerCase());
                // int dotIndex = textureName.lastIndexOf(".");
                // if (dotIndex > -1)
                // texture.append(textureName.substring(0, dotIndex));
                // else
                // texture.append(textureName);
                //
//                 textureAtlas.addBitmapAsset(new BitmapAsset(mCurrentMaterialKey, texture.toString()));
                skipRead(stream);
                break;
            case TRI_MATERIAL:
                // String materialName = readString(stream);
                // int numFaces = readShort(stream);
                //
                // for (int i = 0; i < numFaces; i++) {
                // int faceIndex = readShort(stream);
                // co.faces.get(faceIndex).materialKey = materialName;
                // }
                skipRead(stream);
                break;
            case MATERIAL:
                break;
            case TEX_MAP:
                break;
            default:
                skipRead(stream);
        }
    }

	public void build() {
		int num = mVertices.size();
		for (int j = 0; j < num; ++j) {
			ArrayList<Integer> indices = mIndices.get(j);
			ArrayList<Vector3> vertices = mVertices.get(j);
			ArrayList<Vector3> texCoords = null;
			ArrayList<Vector3> vertNormals = mVertNormals.get(j);

            if (mTexCoords.size() > 0)
                texCoords = mTexCoords.get(j);

            int len = indices.size();
            float[] aVertices = new float[len * 3];
            float[] aNormals = new float[len * 3];
            float[] aTexCoords = new float[len * 2];
            int[] aIndices = new int[len];

            int ic = 0;
            int itn = 0;
            int itc = 0;
            int ivi = 0;

            Vector3 coord;
            Vector3 texcoord;
            Vector3 normal;

            for (int i = 0; i < len; i += 3) {
                int v1 = indices.get(i);
                int v2 = indices.get(i + 1);
                int v3 = indices.get(i + 2);

                coord = vertices.get(v1);

                aVertices[ic++] = (float) coord.x;
                aVertices[ic++] = (float) coord.y;
                aVertices[ic++] = (float) coord.z;

                aIndices[ivi] = ivi++;

                coord = vertices.get(v2);
                aVertices[ic++] = (float) coord.x;
                aVertices[ic++] = (float) coord.y;
                aVertices[ic++] = (float) coord.z;

                aIndices[ivi] = ivi++;

                coord = vertices.get(v3);
                aVertices[ic++] = (float) coord.x;
                aVertices[ic++] = (float) coord.y;
                aVertices[ic++] = (float) coord.z;

                aIndices[ivi] = ivi++;

                if (texCoords != null && texCoords.size() > 0) {
                    texcoord = texCoords.get(v1);

                    aTexCoords[itc++] = (float) texcoord.x;
                    aTexCoords[itc++] = (float) texcoord.y;

                    texcoord = texCoords.get(v2);

                    aTexCoords[itc++] = (float) texcoord.x;
                    aTexCoords[itc++] = (float) texcoord.y;

                    texcoord = texCoords.get(v3);

                    aTexCoords[itc++] = (float) texcoord.x;
                    aTexCoords[itc++] = (float) texcoord.y;
                }

                normal = vertNormals.get(v1);
                aNormals[itn++] = (float) normal.x;
                aNormals[itn++] = (float) normal.y;
                aNormals[itn++] = (float) normal.z;
                normal = vertNormals.get(v2);

                aNormals[itn++] = (float) normal.x;
                aNormals[itn++] = (float) normal.y;
                aNormals[itn++] = (float) normal.z;

                normal = vertNormals.get(v3);

                aNormals[itn++] = (float) normal.x;
                aNormals[itn++] = (float) normal.y;
                aNormals[itn++] = (float) normal.z;
            }

            Object3D targetObj = new Object3D(mObjNames.get(j));
            targetObj.setData(aVertices, aNormals, aTexCoords, null, aIndices, false);
            // -- diffuse material with random color. for now.
            Material material = new Material();
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            targetObj.setMaterial(material);
            targetObj.setColor(0xff000000 + (int) (Math.random() * 0xffffff));
            mRootObject.addChild(targetObj);
        }
    }

    public void clear() {
        for (int i = 0; i < mObjects; ++i) {
            mIndices.get(i).clear();
            mVertNormals.get(i).clear();
            mVertices.get(i).clear();
            mTexCoords.get(i).clear();
        }
        mIndices.clear();
        mVertNormals.clear();
        mVertices.clear();
        mTexCoords.clear();
    }

    protected void skipRead(InputStream stream) throws IOException {
        for (int i = 0; (i < mChunkEndOffset - 6) && !mEndReached; i++) {
            mEndReached = stream.read() < 0;
        }
    }

    protected void readVertices(InputStream buffer) throws IOException {
        float x, y, z;
        int numVertices = readShort(buffer);
        ArrayList<Vector3> vertices = new ArrayList<Vector3>();

        for (int i = 0; i < numVertices; i++) {
            x = readFloat(buffer);
            y = readFloat(buffer);
            z = readFloat(buffer);

            vertices.add(new Vector3(x, y, z));
        }

        mVertices.add(vertices);
    }

    protected void readTexCoords(InputStream buffer) throws IOException {
        int numVertices = readShort(buffer);
        ArrayList<Vector3> texCoords = new ArrayList<Vector3>();

        for (int i = 0; i < numVertices; i++) {
            float x = readFloat(buffer);
            float y = 1 - readFloat(buffer);

            texCoords.add(new Vector3(x, y, 0));
        }

        mTexCoords.add(texCoords);
    }

    protected void readFaces(InputStream buffer) throws IOException {
        int triangles = readShort(buffer);
        Vector3[] normals = new Vector3[triangles];
        ArrayList<Integer> indices = new ArrayList<Integer>();

        for (int i = 0; i < triangles; i++) {
            int[] vertexIDs = new int[3];
            vertexIDs[0] = readShort(buffer);
            vertexIDs[1] = readShort(buffer);
            vertexIDs[2] = readShort(buffer);
            readShort(buffer);

            indices.add(vertexIDs[0]);
            indices.add(vertexIDs[1]);
            indices.add(vertexIDs[2]);

            Vector3 normal = calculateFaceNormal(vertexIDs);
            normals[i] = normal;
        }

        mNormals.add(new Vector3[triangles]);
        mIndices.add(indices);

        int numVertices = mVertices.get(mObjects).size();
        int numIndices = indices.size();

        ArrayList<Vector3> vertNormals = new ArrayList<Vector3>();

        for (int i = 0; i < numVertices; i++) {

            Vector3 vertexNormal = new Vector3();

            for (int j = 0; j < numIndices; j += 3) {
                int id1 = indices.get(j);
                int id2 = indices.get(j + 1);
                int id3 = indices.get(j + 2);

                if (id1 == i || id2 == i || id3 == i) {
                    vertexNormal.add(normals[j / 3]);
                }
            }
            vertexNormal.normalize();
            vertNormals.add(vertexNormal);
        }

        mVertNormals.add(vertNormals);
    }

    private Vector3 calculateFaceNormal(int[] vertexIDs) {
        ArrayList<Vector3> vertices = mVertices.get(mObjects);
        Vector3 v1 = vertices.get(vertexIDs[0]);
        Vector3 v2 = vertices.get(vertexIDs[2]);
        Vector3 v3 = vertices.get(vertexIDs[1]);

        Vector3 vector1 = Vector3.subtractAndCreate(v2, v1);
        Vector3 vector2 = Vector3.subtractAndCreate(v3, v1);

        Vector3 normal = Vector3.crossAndCreate(vector1, vector2);
        normal.normalize();
        return normal;
    }

    protected void readHeader(InputStream stream) throws IOException {
        mChunkID = readShort(stream);
        mChunkEndOffset = readInt(stream);
        mEndReached = mChunkID < 0;
    }

    protected String readString(InputStream stream) throws IOException {
        String result = new String();
        byte inByte;
        while ((inByte = (byte) stream.read()) != 0)
            result += (char) inByte;
        return result;
    }

    protected int readInt(InputStream stream) throws IOException {
        return stream.read() | (stream.read() << 8) | (stream.read() << 16) | (stream.read() << 24);
    }

    protected int readShort(InputStream stream) throws IOException {
        return (stream.read() | (stream.read() << 8));
    }

    protected float readFloat(InputStream stream) throws IOException {
        return Float.intBitsToFloat(readInt(stream));
    }

}
