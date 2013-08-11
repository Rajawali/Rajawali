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
package rajawali.parser;

import java.io.File;

import rajawali.BaseObject3D;
import rajawali.materials.textures.TextureManager;
import rajawali.renderer.RajawaliRenderer;
import android.content.res.Resources;

public abstract class AMeshParser extends AParser implements IMeshParser {
	protected TextureManager mTextureManager;
	
	protected BaseObject3D mRootObject;

	public AMeshParser(RajawaliRenderer renderer, String fileOnSDCard) {
		super(renderer, fileOnSDCard);
		mRootObject = new BaseObject3D();
	}
	
	public AMeshParser(Resources resources, TextureManager textureManager, int resourceId) {
		super(resources, resourceId);
		mTextureManager = textureManager;
		mRootObject = new BaseObject3D();
	}
	
	
	public AMeshParser(RajawaliRenderer renderer, File file) {
		super(renderer, file);
		mRootObject = new BaseObject3D();
	}
	
	public AMeshParser parse() throws ParsingException {
		super.parse();
		return this;
	}

	public BaseObject3D getParsedObject() {
		return mRootObject;
	}
	
	protected class MaterialDef {
		public String name;
		public int ambientColor;
		public int diffuseColor;
		public int specularColor;
		public float specularCoefficient;
		public float alpha;
		public String ambientTexture;
		public String diffuseTexture;
		public String specularColorTexture;
		public String specularHighlightTexture;
		public String alphaTexture;
		public String bumpTexture;
	}
}
