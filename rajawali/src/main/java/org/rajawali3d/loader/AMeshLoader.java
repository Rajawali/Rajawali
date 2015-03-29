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
package org.rajawali3d.loader;

import java.io.File;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.renderer.RajawaliRenderer;
import android.content.res.Resources;

public abstract class AMeshLoader extends ALoader implements IMeshLoader {

	protected TextureManager mTextureManager;

	protected Object3D mRootObject;

	public AMeshLoader(File file) {
		super(file);
		mRootObject = new Object3D();
	}

	public AMeshLoader(String fileOnSDCard) {
		super(fileOnSDCard);
		mRootObject = new Object3D();
	}

	public AMeshLoader(RajawaliRenderer renderer, String fileOnSDCard) {
		super(renderer, fileOnSDCard);
		mRootObject = new Object3D();
	}

	public AMeshLoader(Resources resources, TextureManager textureManager, int resourceId) {
		super(resources, resourceId);
		mTextureManager = textureManager;
		mRootObject = new Object3D();
	}

	public AMeshLoader(RajawaliRenderer renderer, File file) {
		super(renderer, file);
		mRootObject = new Object3D();
	}

	public AMeshLoader parse() throws ParsingException {
		super.parse();
		return this;
	}

	public Object3D getParsedObject() {
		return mRootObject;
	}

	protected class MaterialDef {

		public String name;
		public int ambientColor;
		public int diffuseColor;
		public int specularColor;
		public float specularCoefficient;
		public float alpha = 1f;
		public String ambientTexture;
		public String diffuseTexture;
		public String specularColorTexture;
		public String specularHighlightTexture;
		public String alphaTexture;
		public String bumpTexture;
	}
}
