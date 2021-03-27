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

public class OcclusionMapTexture extends ASingleTexture {
	public OcclusionMapTexture(OcclusionMapTexture other)
	{
		super(other);
	}
	
	public OcclusionMapTexture(String textureName)
	{
		super(TextureType.OCCLUSION, textureName);
	}
	
	public OcclusionMapTexture(String textureName, int resourceId)
	{
		super(TextureType.OCCLUSION, textureName);
		setResourceId(resourceId);
	}
	
	public OcclusionMapTexture(String textureName, Bitmap bitmap)
	{
		super(TextureType.OCCLUSION, textureName, bitmap);
	}
	
	public OcclusionMapTexture(String textureName, ACompressedTexture compressedTexture)
	{
		super(TextureType.OCCLUSION, textureName, compressedTexture);
	}
	
	public OcclusionMapTexture clone() {
		return new OcclusionMapTexture(this);
	}
}
