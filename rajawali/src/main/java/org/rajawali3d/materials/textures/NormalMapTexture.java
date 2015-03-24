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


public class NormalMapTexture extends ASingleTexture {
	public NormalMapTexture(NormalMapTexture other)
	{
		super(other);
	}
	
	public NormalMapTexture(String textureName)
	{
		super(TextureType.NORMAL, textureName);
	}
	
	public NormalMapTexture(String textureName, int resourceId)
	{
		super(TextureType.NORMAL, textureName);
		setResourceId(resourceId);
	}
	
	public NormalMapTexture(String textureName, Bitmap bitmap)
	{
		super(TextureType.NORMAL, textureName, bitmap);
	}
	
	public NormalMapTexture(String textureName, ACompressedTexture compressedTexture)
	{
		super(TextureType.NORMAL, textureName, compressedTexture);
	}
	
	public NormalMapTexture clone() {
		return new NormalMapTexture(this);
	}
}
