# Basic Scene

**_Credit Note:_** Most of this tutorial is taken from Rajawali user Clinton Medbery's tutorial which you can find [on his blog here](http://www.clintonmedbery.com/?p=59). We have altered content to suit our desires for our documentation pages, but all credit for the initial effort of laying out these steps and in particular the images, goes to him.

This tutorial will help you setup a basic 3D scene in Android using the Rajawali 3D library. It is assumed that you are familiar with configuring a project to use Rajawali. If you are not, please see the above mentioned tutorial or the [Installation Guide](./installation_guide.md).

## Setting Up Our Scene

We are going to make a simple scene of the Earth rotating. First, we need to extend the `Renderer` class. Let's create a new class in the same package as our `MainActivity` and name it **Renderer**.

![](http://i1.wp.com/www.clintonmedbery.com/wp-content/uploads/2015/04/Screen-Shot-2015-04-06-at-12.41.33-PM.png)

First, after `public class CustomRenderer` we add `extends Renderer` to make our class a subclass of `Renderer`. Android Studio is going to yell at you a bit to tell you that you also need to implement some methods that the interface demands. It also might yell at you a bit about importing `Renderer`. Make sure you import the Rajawali version not the Android version. If you don't have auto importing turned on, then you will need to add the import statements. Let's fix these issues.

```java
package com.clintonmedbery.rajawalibasicproject;

import android.view.MotionEvent;

import org.rajawali3d.renderer.Renderer;

public class CustomRenderer extends Renderer {

    @Override
    public void initScene() {

    }

    @Override
    public void onTouchEvent(MotionEvent event){
    }

    @Override
    public void onOffsetsChanged(float x, float y, float z, float w, int i, int j){
    }
}
```

A quick note: `onTouchEvent` and `onOffsetsChanged` are both optional use methods (you are required to implement them, but you can leave them empty with no harm) used when your renderer is attached to a wallpaper.

Now it is probably yelling at you about a default constructor. Let's start off by adding our constructor. We will eventually need a variable to hold our model and light as well, so we will add that.

```java
private Sphere mEarthSphere;
private DirectionalLight mDirectionalLight;

public CustomRenderer(Context context) {
    super(context);
    setFrameRate(60);
}
```

At this point let's go ahead and add our texture to the project. You may use any texture you like, but for simplicity you may find the earth texture from the Rajawali Examples project [here](https://github.com/Rajawali/RajawaliExamples/blob/master/examples/src/main/res/drawable-nodpi/earthtruecolor_nasa_big.jpg). **Right click on the res** folder and click on **New > Android Resource Directory** and put in **drawable-nodpi** like the image below.

![](http://i1.wp.com/www.clintonmedbery.com/wp-content/uploads/2015/04/Screen-Shot-2015-04-06-at-1.10.16-PM.png)

You can now drag the image to your folder. It should show up in Android studio when you do. Now we are ready to build our scene. We will do this in a the method called `initScene()`. Let's start with getting our lighting setup:

```java
@Override
public void initScene() {
    mDirectionalLight = new DirectionalLight(1f, .2f, -1.0f);
    mDirectionalLight.setColor(1.0f, 1.0f, 1.0f);
    mDirectionalLight.setPower(2);
    getCurrentScene().addLight(mDirectionalLight);
}
```

This will be called when when Rajawali starts the renderer. We set `mDirectionalLight` to a new `DirectionalLight` and we adjust the color and the power. Then we add it to our scene. Next, we want to set up the material for our sphere. Add these lines after the code for adding our `mDirectionalLight`.

```java
        Material material = new Material();
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        material.setColorInfluence(0);
        Texture earthTexture = new Texture("Earth", R.drawable.earthtruecolor_nasa_big);
        try{
            material.addTexture(earthTexture);

        } catch (ATexture.TextureException error){
            Log.d("DEBUG", "TEXTURE ERROR");
        }
```

First we create a new `Material` called `material`. We enable lighting on it and give it a material type of Diffuse (material types are outside the scope of this tutorial). Finally we set its color influence to 0. This simply means that vertex/object colors will not be blended with this material. A value of `1.0f` would result in the object color completely overriding the texture color.

Then we create a new `Texture` called `texture` and give it our earth picture in the constructor. Then we add `texture` to `material` in a `try-catch` block to catch any errors. Now we have our material ready to go.

Finally, we instantiate our Sphere and give it the material we just created. We add it to the scene, and set the camera back so we can view the scene.

```java
        mEarthSphere = new Sphere(1, 24, 24);
        earthSphere.setMaterial(material);
        getCurrentScene().addChild(mEarthSphere);
        getCurrentCamera().setZ(4.2f);
```

## Animating the Sphere

While Rajawali has an extensible animation system, the details of that are beyond this tutorial and we will limit ourselves to a very basic frame by frame step by overriding the `onRender` method. This will make our Earth rotate.

```java
@Override
public void onRender(final long elapsedTime, final double deltaTime) {
    super.onRender(elapsedTime, deltaTime);
    mEarthSphere.rotate(Vector3.Axis.Y, 1.0);
}
```

That should be it for our simple scene. Now we need to go to `MainActivity` and set up the Rajawali to use our renderer.

## Applying our Renderer

Now we need to go to `MainActivity` and add a renderer to the activity. Then, we need to add some code to `onCreate`. Here is what you should have at the top of `MainActivity`.

```java
Renderer renderer;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final SurfaceView surface = new SurfaceView(this);
    surface.setFrameRate(60.0);
    surface.setRenderMode(ISurface.RENDERMODE_WHEN_DIRTY);

    // Add mSurface to your root view
    addContentView(surface, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT));

    renderer = new Renderer(this);
    surface.setSurfaceRenderer(renderer);
}
```

The main thing to focus on here is the `SurfaceView`. This class extends `GLSurfaceView` and let's you add our `Renderer` to it, making it appear in our activity. We are able to set up some variables for our surface, and instantiate our `Renderer` scene and add it to the surface.

That's it! Let's run the program and see what we get.

![](http://i1.wp.com/www.clintonmedbery.com/wp-content/uploads/2015/04/Screenshot_2015-04-06-14-03-561.png)

The Earth should rotate and you should be good to go! If not, please feel free to ask any questions on the [Rajawali Google+ community](https://plus.google.com/communities/116529974266844528013).
