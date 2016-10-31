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
package org.rajawali3d.textures;


import android.content.Context;
import android.support.annotation.NonNull;
import org.rajawali3d.textures.annotation.Type;

public class SpecularMapTexture extends SingleTexture {
	public SpecularMapTexture(SpecularMapTexture other)
	{
		super(other);
	}

	public SpecularMapTexture(String textureName)
	{
		super(Type.SPECULAR, textureName);
	}

	public SpecularMapTexture(String textureName, @NonNull Context context, int resourceId)
	{
		super(Type.SPECULAR, textureName);
		setTextureDataFromResourceId(context, resourceId);
	}

	public SpecularMapTexture(String textureName, TextureDataReference textureData)
	{
		super(Type.SPECULAR, textureName, textureData);
	}

	public SpecularMapTexture(String textureName, CompressedTexture compressedTexture)
	{
		super(Type.SPECULAR, textureName, compressedTexture);
	}

	public SpecularMapTexture clone() {
		return new SpecularMapTexture(this);
	}
}
