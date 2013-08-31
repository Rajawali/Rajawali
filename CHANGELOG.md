Rajawali "Anchor Steam" Development Branch
---------------------------

"Anchor Steam", the next Rajawali version contains significant changes to the API.
Here's what's new:

### Garbage

Across the library we have tried to reduce the ammount of garbage that is generated. Animations now generate little to
no garbage, even when run for very long periods of time.

### Thread Safety

To eliminate a number of issues which stemmed from trying to change scene contents in the middle of a render cycle,
a task queue system has been added to Rajawali. You no longer have direct access to lists such as `mChildren`. Helper
methods such as `addChild()` exist and will automatically queue everything for you.

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

### BaseObject3D

`BaseObject3D` has been renamed to `Object3D`.

### FPSUpdateListener

`FPSUpdateListener` has been renamed to `OnFPSUpdateLister`.

### Parsers

All parser classes which were previously called `xxParser` are now called `Loaderxx`. For example, `OBJParser` is now `LoaderOBJ`.

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

