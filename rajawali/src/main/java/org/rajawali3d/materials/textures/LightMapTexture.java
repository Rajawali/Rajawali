/**
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

public class LightMapTexture extends ASingleTexture {
	public LightMapTexture(LightMapTexture other)
	{
		super(other);
	}
	
	public LightMapTexture(String textureName)
	{
		super(TextureType.LIGHT, textureName);
	}
	
	public LightMapTexture(String textureName, int resourceId)
	{
		super(TextureType.LIGHT, textureName);
		setResourceId(resourceId);
	}
	
	public LightMapTexture(String textureName, Bitmap bitmap)
	{
		super(TextureType.LIGHT, textureName, bitmap);
	}
	
	public LightMapTexture(String textureName, ACompressedTexture compressedTexture)
	{
		super(TextureType.LIGHT, textureName, compressedTexture);
	}
	
	public LightMapTexture clone() {
		return new LightMapTexture(this);
	}
}
