/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package rajawali.primitives;

import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.materials.AParticleMaterial;
import rajawali.util.RajLog;
import android.opengl.GLES20;

public class Particle extends BaseObject3D {
	protected float mPointSize = 10.0f;
	protected AParticleMaterial mParticleShader;
	
	public Particle() {
		super();
		init();
	}
	
	public void setPointSize(float pointSize) {
		mPointSize = pointSize;
	}
	
	public float getPointSize() {
		return mPointSize;
	}
	
	protected void init() {
		setDrawingMode(GLES20.GL_POINTS);
		setTransparent(true);
		
		float[] vertices = new float[] {
			0, 0, 0	        
		};
		float[] textureCoords = new float[] {
			0, 0, 0
		};
		float[] normals = new float[] {
			0.0f, 0.0f, 1.0f
		};
		float[] colors = new float[] {
			1.0f, 1.0f, 1.0f, 1.0f	
		};
		int[] indices = new int[] {
			0
		};
		
		setData(vertices, normals, textureCoords, colors, indices);
	}
	
	public void setMaterial(AParticleMaterial material) {
		super.setMaterial(material);
		mParticleShader = material;
	}

	@Override
	protected void setShaderParams(Camera camera) {
		super.setShaderParams(camera);
		
		if(mParticleShader == null) {
			RajLog.e("[" +getClass().getName()+ "] You need to set a particle material first.");
			throw new RuntimeException("You need to set a particle material first.");
		}
		mParticleShader.setCamera(camera);
		mParticleShader.setCameraPosition(camera.getPosition());
		mParticleShader.setPointSize(mPointSize);
	}
}
