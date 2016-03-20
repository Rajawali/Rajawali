# Materials

Rajawali has a material builder system which allows for the easy application of many common material types and attributes. The various combinations of all of these effects are too numerous to demonstrate, but below are several effects which can be accomplished using the material builder system. For more tailored effects, you can always load your own GLSL. This is demoed in an advanced tutorial.

## Creating a color material without using lighting

![Unlit Material](http://www.rozengain.com/files/RajawaliWiki/as_materials_001.jpg)

No lights, no textures, just a color.

```java
Material material = new Material();
material.setColor(Color.GREEN);
// or
material.setColor(0xff009900);
```

## Creating a color material with diffuse lighting

![Material with diffuse lighting](http://www.rozengain.com/files/RajawaliWiki/as_materials_002.jpg)

Using a light, a color and the Lambertian diffuse model.

```java
Material material = new Material();
material.setColor(0xff009900);
material.enableLighting(true);
material.setDiffuseMethod(new DiffuseMethod.Lambert());
```

## Creating a color material with diffuse lighting and specular highlights

![Material with diffuse lighting and specular highlights](http://www.rozengain.com/files/RajawaliWiki/as_materials_003.jpg)

Using a light source, a color, Lambertian diffuse model and Phong specular highlights.

```java
Material material = new Material();
material.setColor(0xff009900);
material.enableLighting(true);
material.setDiffuseMethod(new DiffuseMethod.Lambert());
material.setSpecularMethod(new SpecularMethod.Phong());
```

## Using a diffuse texture

![Diffuse texture](http://www.rozengain.com/files/RajawaliWiki/as_materials_004.jpg)

No lights, just a single diffuse texture.

```java
Material material = new Material();
// -- The first parameter is compulsory and needs to be unique and
//    has the same restrictions as any Java variable.
material.addTexture(new Texture("earth", R.drawable.earth_diffuse));
```

## Mixing diffuse texture with color

![Diffuse texture mixed with a color](http://www.rozengain.com/files/RajawaliWiki/as_materials_005.jpg)

50% diffuse texture, 50% blue color

```java
Material material = new Material();
material.setColor(Color.BLUE);
Texture texture = new Texture("earth", R.drawable.jettexture);
// -- Use 50% of the texture
texture.setInfluence(.5f);
// -- Use 50% blue
material.setColorInfluence(.5f);
material.addTexture(texture);
```

## Using a diffuse texture with diffuse lighting and specular highlights

![Texture with diffuse lighting and specular highlights](http://www.rozengain.com/files/RajawaliWiki/as_materials_006.jpg)

Texture, Lambertian diffuse method, Phong specular highlights.

```java
Material material = new Material();
material.addTexture(new Texture("earth", R.drawable.earth_diffuse));
material.enableLighting(true);
material.setDiffuseMethod(new DiffuseMethod.Lambert());
material.setSpecularMethod(new SpecularMethod.Phong());
```

## Mixing two diffuse textures

![Mixing two textures](http://www.rozengain.com/files/RajawaliWiki/as_materials_007.jpg)

Mixing two textures, 50% influence each.

```java
Material material = new Material();
Texture texture1 = new Texture("earth", R.drawable.earth_diffuse);
// -- Use 50% of this texure
texture1.setInfluence(.5f);
material.addTexture(texture1);
Texture texture2 = new Texture("manila", R.drawable.manila_sphere_map);
// -- Use 50% of this texture
texture2.setInfluence(.5f);
material.addTexture(texture2);
```

## Using a normal map

![Using a normal map](http://www.rozengain.com/files/RajawaliWiki/as_materials_008.jpg)

A diffuse texture, a normal map texture, Lambertian diffuse method and Phong specular highlights.

```java
Material material = new Material();
material.addTexture(new Texture("earth", R.drawable.earth_diffuse));
material.addTexture(new NormalMapTexture("earthNormal", R.drawable.earth_normal));
// -- Note that you can also set the normal map's strength through the setInfluence(float) setter
material.enableLighting(true);
material.setDiffuseMethod(new DiffuseMethod.Lambert());
material.setSpecularMethod(new SpecularMethod.Phong(0xeeeeee, 200));
```

## Repeating textures

![Texture repeating](http://www.rozengain.com/files/RajawaliWiki/as_materials_009.jpg)

Repeating a texture 4 times in the U/X direction and 4 times in the V/Y direction.

```java
Material material = new Material();
Texture texture = new Texture("earth", R.drawable.de_leckere);
texture.setWrapType(WrapType.REPEAT);
// -- Repeat 4 times in the U/X direction and 4 times in the V/Y direction
texture.setRepeat(4, 4);
material.addTexture(texture);
```

## Texture offset

![Texture offset](http://www.rozengain.com/files/RajawaliWiki/as_materials_010.jpg)

Set the texture offsets in the U/X directions and V/Y directions. These are normalized coordinates so they represent a 50% shift in the U/X direction and a 30% shift in the V/Y direction.

```java
Material material = new Material();
Texture texture = new Texture("earth", R.drawable.earth_diffuse);
// -- This has to be set explicitly because of shader optimization considerations
texture.enableOffset(true);
// -- Set the offsets in the U/X directions and V/Y directions. These are normalized coordinates so
//    they represent a 50% shift in the U/X direction and a 30% shift in the V/Y direction.
texture.setOffset(.5f, .3f);
// -- Note that the offset can be animated by changing the values on each frame in the onDrawFrame()
//    method in your renderer class.
```

## Environment Cube Map

![Environment cube map](http://www.rozengain.com/files/RajawaliWiki/as_materials_011.jpg)

This requires six textures. Use a sphere map if your resources are limited.

```java
Material material = new Material();
int[] cubemaps = new int[6];
cubemaps[0] = R.drawable.posx;
cubemaps[1] = R.drawable.negx;
cubemaps[2] = R.drawable.posy;
cubemaps[3] = R.drawable.negy;
cubemaps[4] = R.drawable.posz;
cubemaps[5] = R.drawable.negz;
CubeMapTexture texture = new CubeMapTexture("cubemaps", cubemaps);
texture.isEnvironmentTexture(true);
material.addTexture(texture);
```

## Environment Sphere Map

![Environment sphere map](http://www.rozengain.com/files/RajawaliWiki/as_materials_012.jpg)

```java
Material material = new Material();
SphereMapTexture texture = new SphereMapTexture("sphereMap", R.drawable.sphere_map);
texture.isEnvironmentTexture(true);
material.addTexture(texture);
```

## Environment Sphere Map Combined With Color and a Diffuse Texture

![Sphere map with color & texture](http://www.rozengain.com/files/RajawaliWiki/as_materials_013.jpg)

```java
Material material = new Material();
material.setUseColor(Color.ORANGE);
// -- use 30% plain orange color
material.setColorInfluence(.3f);

SphereMapTexture sphereMapTexture = new SphereMapTexture("sphereMap", R.drawable.sphere_map);
// -- use 30% of the environment sphere map
sphereMapTexture.isEnvironmentTexture(true);
sphereMapTexture.setInfluence(.3f);
material.addTexture(sphereMapTexture);

Texture texture = new Texture("myTexture", R.drawable.my_texture);
// -- use 40% of the diffuse texture
texture.setInfluence(.4f);
material.addTexture(texture);
```

### Diffuse Texture & Specular Map

![Diffuse Texture & Specular Map](http://www.rozengain.com/files/RajawaliWiki/as_materials_014.jpg)

```java
Material material = new Material();
// -- add a diffuse texture
material.addTexture(new Texture("earth", R.drawable.earth_diffuse));
// -- add a specular map
material.addTexture(new SpecularMapTexture("earthSpec", R.drawable.earth_specular));
```

### Diffuse Texture, Specular Map & Normal Map

![Diffuse Texture, Specular Map & Normal Map](http://www.rozengain.com/files/RajawaliWiki/as_materials_015.jpg)

```java
Material material = new Material();
// -- add a diffuse texture
material.addTexture(new Texture("earthDiff", R.drawable.earth_diffuse));
// -- add a specular map
material.addTexture(new SpecularMapTexture("earthSpec", R.drawable.earth_specular));
// -- add a normal map
material.addTexture(new NormalMapTexture("earthNorm", R.drawable.earth_normal));
```

### Diffuse Texture & Alpha Map

![Diffuse Texture, Specular Map & Normal Map](http://www.rozengain.com/files/RajawaliWiki/as_materials_017.jpg)

```java
Material material = new Material();
Texture texture = new Texture("earth", R.drawable.earth_diffuse);
material.addTexture(texture);

AlphaMapTexture alphaMap = new AlphaMapTexture("alphaMap", R.drawable.camden_town_alpha);
material.addTexture(alphaMap);
```

### Toon Material

![Toon Material](http://www.rozengain.com/files/RajawaliWiki/as_materials_016.jpg)

```java
Material material = new Material();
material.enableLighting(true);
material.setDiffuseMethod(new DiffuseMethod.Toon());
// or new DiffuseMethod.Toon(int color1, int color2, int color3, int color4);
```