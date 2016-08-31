Rajawali v1.1.x "Bombshell"
-------------------------------------------------

"Bombshell", the next Rajawali version contains some minor changes to the API.

### Anti-Aliasing

FXAA and Super-sample anti aliasing have been added as post processing options. See the FXAA Post Processing example to see how.

### Examples and Support Modules

The examples application and all support modules such as Android Wear, Vuforia and Cardboard support have been moved to this repository. 

### Color Picking

* `Scene.java`
 * Added `doColorPicking()` to isolate color-picking render from normal render() control flow
 * Renamed `requestColorPickingTexture()` to `requestColorPicking()` 
* `Object3D.java`
 * Introduced `UNPICKABLE` color/index, made it a default value
 * Updated `setPickingColor()`, removed `getPickingColor()`
 * Eliminated `mIsPickingEnabled`, now check for `UNPICKABLE`
 * Added `renderColorPicking()` for optimized/independent color-picking rendering of the `Object3D` and its children (per #1387)
* `ObjectColorPicker.java`
 * Updated `unregisterObject()` to reset picking color to `UNPICKABLE`
 * Renamed `createColorPickingTexture()` to `pickObject()`
 * In `pickObject()` call to `GLES20.glReadPixels()`, replaced `getDefaultViewportHieght()` with `getViewportHeight()`

### Textures

Stores texture handles in a hashmap keyed by texture name. This allows reusing textures between materials if the same names are used. An initial attempt to find the locations is made on first compiling a material but if the texture name is not known there (such as for post processing) then it will be cached on the first use.

### Desmurfing

The old class names that were prefixed with "Rajawali" have been removed. For example, `RajawaliRenderer` is now just `Renderer`.

### Textures
 
`ATexture.setUniformHandle(int)` and `ATexture.getUniformHandle()` have been removed. They were an incomplete implementation and the ownership of these handles has been moved to the `Material` level, allowing reused of identically named textures across materials as well as helping cache for vertex textures, if available on the device.

Rajawali v1.0.x "Anchor Steam"
-------------------------------------------------

"Anchor Steam", the next Rajawali version contains significant changes to the API.
Here's what's new:

### Android Studio and Continuous Integration

The project has been migrated to Android Studio and the Gradle build system. Along with this, we have setup builds with Travis CI. Build status badges have been added to the [Readme](https://github.com/Rajawali/Rajawali/blob/master/README.md) to indicate the status. We are currently in the process of deploying the project to Maven.

### Package Name

In preparation for deployment into Maven, the package name of the Rajawali library has changed. The old packages `rajawali` and `rajawali.framework` are now `org.rajawali3d`, which is the Maven group id.

### No more `RajawaliFragment` or `RajawaliActivity`

`RajawaliSurfaceView` and `RajawaliTextureView` are the classes which replace `RajawaliFragment`, `RajawaliSupportFragment` and `RajawaliActivity`. They extend a common interface, `IRajawaliSurface`. This was done to simplify development of the framework by reducing the duplication of life cycle related code as much as possible. While you may need to take a few extra steps to bring Rajawali into your Fragment/Activity, these steps have been simplified and made more consistent across the multiple use cases. What follows is an explanation of setting up these views in a `Fragment`, though it would be no different for an `Activity`. For more details, see [Using RajawaliSurfaceView and RajawaliTextureView](https://github.com/Rajawali/Rajawali/wiki/Using-RajawaliSurfaceView-and-RajawaliTextureView)

### Debugging

A new renderer class, `RajawaliDebugRenderer` has been added. It has an additional constructor parameter for a `RajawaliGLDebug.Builder` instance which will configure the debug behavior. You can enable automatic glError() calls after every GL call, enforcment of all GL calls coming from the same thread and argument name printing for GL calls. With the addition of this class, `RajawaliRenderer` no longer checks for GL errors at the end of each frame. This is for performance reasons. Of course in your own implementation you could still check at the end of each frame if you so chose. 

Additionally, the `org.rajawali3d.util.debugvisualizer` package has been added. It contains several classes which allow you to easily add visuals to your scene for things like camera frustrum, lights, bounding volumes, etc. 

### OpenGL ES 3.0

Rajawali will automatically determine if a device supports OpenGL ES 3.x. If it does, Rajawali will automatically request a GLES 3 context rather than 2. If the device does not support GLES 3, Rajawali will request a GLES 2 surface. The GLES 3 feature set supported Rajawali is currently minimal, but the following has been implemented:

- ETC2 Texture compression. All formats of ETC2 compression except for the 3 sRGB formats are currently supported. At present, we have been unable to find information relating to the internal format codes of the PKM file header for the sRGB formats. When that information is available, a very small modification will be all that is required to add support for these files. The `Etc2Texture` class is the Rajawali native wrapper for these textures. It has the added benefit of accepting ETC1 textures.

### Garbage

Across the library we have tried to reduce the ammount of garbage that is generated. Animations now generate little to
no garbage, even when run for very long periods of time.

### Thread Safety

To eliminate a number of issues which stemmed from trying to change scene contents in the middle of a render cycle,
a task queue system has been added to Rajawali. You no longer have direct access to lists such as `mChildren`. Helper
methods such as `addChild()` exist and will automatically queue everything for you.

### Asyncronous Loading

To fully take advantage of `RajawaliScene` (see the Scenes section below), it is necessary to be able to load models on a background thread while rendering is active. To fascilitate this, asyncronous loading options have been added to `RajawaliRenderer`. Your code can now request an asyncronous load of any `ALoader` implementation (your own included) and receive a notification of success or failure via the `IAsyncLoaderCallback` interface. See [The wiki explanation](https://github.com/Rajawali/Rajawali/wiki/Async-Loaders) or Rajawali Examples for more information.

### Scene Frame Callbacks

Scene frame callbacks were added to provide a easy way for user code to tie into the render cycle while receiving timing information about the scene. These callbacks receive the typical frame delta time (measured in seconds), used by the animation system, as well as an additional parameter - the rendering elapsed time (measured in nanoseconds). For more information, see [The Wiki](https://github.com/Rajawali/Rajawali/wiki/Scene-Frame-Callbacks)

### Lazy VBO creation

In the past, all VBOs were created immediately in the `Object3D` constructor. This is still the default behavior, however a new constructor has been added with a `boolean` parameter which allows for the creation of these VBOs to be deferred until the first render pass. If they are deferred, the initial frame may incur a slight delay, but in most cases this will not be noticable. Deferred creation is useful if you would like to build a complete `RajawaliScene` before having a running `RajawaliRenderer`.

### Conversion to double precision

Rajawali has been converted to double precision internally. Some of the public API has changed as a result of this switch,
however it is not significant and likely affects only advanced users. Most notably the method signature of the `render()` methods.
This was done to eliminate some bugs being caused by floating point roundoff errors since current and emerging devices have little
to no performance loss. There will be a slight increase in memory consumption but it should be negligible compared to texture consumption.
For more information see issue #988.

Since the `android.opengl.Matrix` class only supports float arrays, the class has been copied as `rajawali.math.Matrix` and converted
to use doubles. To avoid needless casting, you should utilize this class instead. The only change to the class is to utilize double precision
floating point numbers instead of single precision, however a few native methods had to be implemented in Java. Similarly, `android.opengl.GLU`
only supports float math, so the class has been copied as `rajawali.util.GLU` and convert to use doubles.

Position and orientation information are now handled as double precision, however the object geometry and any colors are not. This
is done primarily to reduce the overhead of casting a lot of data from double to float on each frame, but there is also no need for it,
and it doesn't come free. Promoting these to double will happen automatically anytime they are used in math with a double and because they
are provided to the library in float form, we do not loose any precision this way. The one exception to this is if you are dynamically
modifying the geometry data at run time which is an advanced process with a lot of other implications anyway.

### Number3D

The `Number3D` class has been refactored into `Vector3` which is way more appropriate.
This will most likely affect a lot of your code. Furthermore, the class has been entirely
refactored to be more consistent and reduce the amount of garbage it generates. The public
API has been modified considerably but should be much more robust, clean and useful.
It has also been moved to the `rajawali.math.vector` package.
 - Operations which set their result on the calling object are named/documented accordingly.
 - Operations which create new objects are named/documented accordingly.

### Vector2D

The `Vector2D` has been refactored into `Vector2` which falls in line with the new `Vector3` class.
It has also been moved to the `rajawali.math.vector` package.

### Vector3

The `Number3D` class has been refactored to `Vector3`. This name is much more appropriate.

### Quaternion

The `Quaternion` class has been overhauled and refactored similarly to the `Vector3` class. This may affect a lot of your code,
depending on your use of quaternions. It has been made more consistent and efficient, reducing garbage. Other noteworthy changes include:
 - The method `Quaternion#fromRotationMatrix(float[])` has become `Quaternion#fromMatrix(Matrix4)`.
 - The method `Quaternion#unitInverse()` has been removed as it was the same as `Quaternion#conjugate()`
 - Operations which set their result on the calling object are named/documented accordingly.
 - Operations which create new objects are named/documented accordingly.

### AngleAxis

The `AngleAxis` class has been removed. It was essentially an incomplete `Quaternion` class and was not being
used anywhere in the library or in the examples project.

### Matrix4

Previously, the `Matrix4` class was incomplete and never used internal to the library. The class has been filled out
and the library has been switched over to using it internally. This was done for clarity, concise code and as the ground
work for being able to add more complex features to the library. An exception to this is skeletal animation, which still uses
double arrays. This with absolute certainty will require you to change some of your code. At a bare minimum, common method
signatures have changed, but only in their data types.

While you can still use float or double arrays for matrices if you prefer, `Matrix4` has been implemented in an efficient manner
which should not produce extra garbage and will dramatically simplify code which performs lots of matrix operations.

### ATransformable3D 

The orientation, rotation and look at functionality of `ATransformable3D` have been modified. Look at tracking has the ability to operate automatically now. To enable, set your look at point via `ATransformable3D.setLookAt(Vector3 look)` and call `ATransformable3D.enableLookAt()`. At this point, anytime your object moves, it will automatically re-orient itself to look at its look at point. 

The model matrix is now the responsibility of `ATransformable3D`. Calculation is handled on an as needed basis now, saving processing time. When an object is moved/rotated, its model matrix is marked as dirty. The next time it is requested in a render loop, the matrix will then be re-calculated. By marking as dirty, we save multiple recalculations when an object has several transformations applied between frames. 

Finally, the orientation of all `ATransformable3D` objects is defined solely as a quaternion. This eliminates the ambiguity of the old system, provides no chance of gimble lock and generally increases efficiency. The previous model matrix calculation required 100 floating point operations and 4 loops. The new method requires only 27 floating point operations. View matrix calculation has seen similar improvements.

### BaseObject3D

`BaseObject3D` has been renamed to `Object3D`.

### FPSUpdateListener

`FPSUpdateListener` has been renamed to `OnFPSUpdateLister`.

### Parsers

All parser classes which were previously called `xxParser` are now called `Loaderxx`. For example, `OBJParser` is now `LoaderOBJ`. They have also been moved from the `parser` package to `loader`.

### GL State

To help increase ultimate frame rate, a default GL state has been implemented and set once at GL surface creation. `BaseObject3D`
instances which are transparent, double-sided or otherwise differ in their culling automatically change the state for their render
and return the state when they are done. This means the GL state is not managed as efficiently as it could be, however it is a dramatic
improvement over the previous method of explicitly declaring the state on each render for each object and has in some testing shown a
6 FPS improvement for ~12%.

### Scenes

A new class, `RajawaliScene` has been added which fully encompasses everything to render a scene. Essentially everything you
would have previously done in `RajawaliRenderer#initScene()` now fits in a `RajawaliScene` and you can have multiple instances
of `RajawaliScene` and feely switch between them, allowing you to do all sorts of cool things such as loading a new scene in the
background, showing different areas, etc.
For more info please see [Tutorial 31](https://github.com/MasDennis/Rajawali/wiki/Tutorial-31-Using-RajawaliScene).

### Multiple Cameras

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

### Texture Management & Materials

Materials & textures have become much more flexible. Please check this wiki pages for all the changes: https://github.com/MasDennis/Rajawali/wiki/Materials


### Post Processing

The old filter system has been replaced with a new modular post processing framework. This allows for complex effects based on frame buffer objects and multi-pass rendering. The documentation of details of this will be a work in progress. In the meantime, the examples app is the best place to start.

### Lights

Lights aren't added directly to objects anymore. In Anchor Steam they have to be added to the scene:

```java
getCurrentScene().addLight(myLight);
```

### Paths/Curves

"Paths" now have the more appropriate name "Curves". The reason for this is that curves aren't necessarily paths.
The have also been moved from `rajawali.animation` to `rajawali.curves`.
These existing classes have been renamed:
- `CatmullRomPath3D`: `CatmullRomCurve3D`
- `ISpline3D`: `ICurve3D`
- `BezierPath3D`: `CubicBezier3D`

Some new classes have been added:
- `CompoundCurve3D`: This is a container for an n number of curves of any type.
- `LinearBezierCurve3D`: A linear bezier curve. Basically just a straight line. This is useful for compound curves.
- `QuadraticBezierCurve3D`: A quadratic bezier curve. This type of Bezier curve take only one control point instead of two.
- `SVGPath`: takes an SVG-style path string and creates a `CompoundCurve3D`. Still a work in progress.
- `LogarithmicSpiral3D` : A spiral curve, often refered to as a "Golden Spiral" or "Nautalus Spiral"
- `ArchimedeanSpiral3D` : A spiral curve, with several variants based on a constant exponent.


