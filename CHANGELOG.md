Rajawali "Anchor Steam" Development Branch
---------------------------

"Anchor Steam", the next Rajawali version contains significant changes to the API.
Here's what's new:

# Thread Safety

To eliminate a number of issues which stemmed from trying to change scene contents in the middle of a render cycle, a task queue system has been added to Rajawali. You no longer have direct access to lists such as `mChildren`. Helper methods such as `addChild()` exist and will automatically queue everything for you.

# Number3D

The `Number3D` class has been refactored into `Vector3` which is way more appropriate.
This will most likely affect a lot of your code.

# Vector2D

The `Vector2D` has been refactored into `Vector2` which falls in line with the new `Vector3` class.

# Scenes

A new class, `RajawaliScene` has been added which fully encompasses everything to render a scene. Essentially everything you would have previously done in `RajawaliRenderer#initScene()` now fits in a `RajawaliScene` and you can have multiple instances of `RajawaliScene` and feely switch between them, allowing you to do all sorts of cool things such as loading a new scene in the background, showing different areas, etc. For more info please see [Tutorial 31](https://github.com/MasDennis/Rajawali/wiki/Tutorial-31-Using-RajawaliScene)

# Multiple Cameras

You can now use multiple cameras in Rajawali and freely switch between them in a thread safe manner.

```java
public void nextCamera() {
	if (getCurrentCamera().equals(mCamera1)) {
		getCurrentScene().switchCamera(mCamera2);
	} else {
		getCurrentScene().switchCamera(mCamera1);
	}
}
```

# Texture Management

Texture management has been simplified. Here's a basic `DiffuseMaterial` example:

``` java
DiffuseMaterial material = new DiffuseMaterial();
// -- Add the texture to the material
material.addTexture(new Texture(R.drawable.earthtruecolor_nasa_big));
// -- No need to add the texture to the object. This method has been removed.
myObject3D.setMaterial(material);
```

Here's a `CubeMapMaterial` example:

``` java
int[] resourceIds = new int[] { R.drawable.posx, R.drawable.negx, R.drawable.posy, R.drawable.negy, R.drawable.posz, R.drawable.negz};

CubeMapMaterial material = new CubeMapMaterial();
// -- Errors are thrown so you'll get more information when things go wrong
try {
	material.addTexture(new CubeMapTexture("environmentMap", resourceIds));
	myObject3D.setMaterial(material);
} catch (TextureException e) {
	e.printStackTrace();
}
```

A `SphereMapMaterial` example:

``` java
Texture jetTexture = new Texture(R.drawable.jettexture);
SphereMapTexture sphereMapTexture = new SphereMapTexture(R.drawable.manila_sphere_map);

BaseObject3D jet1 = null;
// -- sphere map with texture

try {
	SphereMapMaterial material1 = new SphereMapMaterial();
	material1.setSphereMapStrength(.5f);
	material1.addTexture(jetTexture);
	material1.addTexture(sphereMapTexture);

	ObjectInputStream ois;
	ois = new ObjectInputStream(mContext.getResources().openRawResource(R.raw.jet));
	jet1 = new BaseObject3D((SerializedObject3D)ois.readObject());
	jet1.setMaterial(material1);
	jet1.addLight(light);
	jet1.setY(2.5f);
	addChild(jet1);
} catch(Exception e) {
	e.printStackTrace();
}
```

A `NormalMapMaterial` example:

``` java
NormalMapMaterial material1 = new NormalMapMaterial();
material1.addTexture(new Texture(R.drawable.sphere_texture));
material1.addTexture(new NormalMapTexture(R.drawable.sphere_normal));
mHalfSphere1.setMaterial(material1);
```

# Vector3

The `Number3D` class has been refactored to `Vector3`. This name is much more appropriate.

# Object color

`AMaterial`'s method `setUseColor(boolean useColor)` has been removed. There are two new methods that replace it:
- `setUseSingleColor(boolean value)`: When the object uses a single color for the whole mesh use this. This way no color buffer will be created which reduces the memory footprint and increases performance, especially in big scenes.
- `setUseVertexColors(boolean value)`: Use this when your mesh has multiple colors. This isn't applicable to textures, just vertex colors.
