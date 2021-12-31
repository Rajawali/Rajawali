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
package org.rajawali3d.materials.textures;

import android.graphics.Bitmap;

import java.io.InputStream;

public class SpecularMapTexture extends ASingleTexture {
	public SpecularMapTexture(SpecularMapTexture other)
	{
		super(other);
	}
	
	public SpecularMapTexture(String textureName)
	{
		super(TextureType.SPECULAR, textureName);
	}
	
	public SpecularMapTexture(String textureName, InputStream stream)
	{
		super(TextureType.SPECULAR, textureName, stream);
	}
	
	public SpecularMapTexture(String textureName, int resourceId)
	{
		super(TextureType.SPECULAR, textureName, resourceId);
	}
	
	public SpecularMapTexture(String textureName, Bitmap bitmap)
	{
		super(TextureType.SPECULAR, textureName, bitmap);
	}
	
	public SpecularMapTexture(String textureName, ACompressedTexture compressedTexture)
	{
		super(TextureType.SPECULAR, textureName, compressedTexture);
	}
	
	public SpecularMapTexture clone() {
		return new SpecularMapTexture(this);
	}
}
