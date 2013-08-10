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
package rajawali.materials.textures;

import android.graphics.Bitmap;

public class SphereMapTexture extends ASingleTexture {

	public SphereMapTexture(SphereMapTexture other)
	{
		super(other);
	}

	public SphereMapTexture(String textureName)
	{
		super(TextureType.SPHERE_MAP, textureName);
	}

	public SphereMapTexture(int resourceId)
	{
		super(TextureType.SPHERE_MAP, resourceId);
	}

	public SphereMapTexture(String textureName, Bitmap bitmap)
	{
		super(TextureType.SPHERE_MAP, textureName, bitmap);
	}
	
	public SphereMapTexture(String textureName, ACompressedTexture compressedTexture)
	{
		super(TextureType.SPHERE_MAP, textureName, compressedTexture);
	}

	@Override
	public SphereMapTexture clone() {
		return new SphereMapTexture(this);
	}
}
