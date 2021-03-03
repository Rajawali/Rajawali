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


public class Texture extends ASingleTexture {
	public Texture(Texture other)
	{
		super(other);
	}
	
	public Texture(String textureName)
	{
		super(TextureType.DIFFUSE, textureName);
	}
	
	public Texture(String textureName, int resourceId)
	{
		super(TextureType.DIFFUSE, textureName, resourceId);
	}
	
	public Texture(String textureName, InputStream stream)
	{
		super(TextureType.DIFFUSE, textureName, stream);
	}
	
	public Texture(String textureName, Bitmap bitmap)
	{
		super(TextureType.DIFFUSE, textureName, bitmap);
	}
	
	public Texture(String textureName, TextureAtlas atlas)
	{
		super(TextureType.DIFFUSE, textureName, atlas.getTileNamed(textureName).getPage());
	}
	public Texture(String textureName, ACompressedTexture compressedTexture)
	{
		super(TextureType.DIFFUSE, textureName, compressedTexture);
	}
	
	public Texture clone() {
		return new Texture(this);
	}
}
