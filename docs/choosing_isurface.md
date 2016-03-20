# Choosing an ISurface implementation

## About

`SurfaceView` and `TextureView` are the classes which replace `RajawaliFragment`, `RajawaliSupportFragment` and `RajawaliActivity`. They extend a common interface, `ISurface`. This was done to simplify development of the framework by reducing the duplication of life cycle related code as much as possible. While you may need to take a few extra steps to bring Rajawali into your Fragment/Activity, these steps have been simplified and made more consistent across the multiple use cases. What follows is an explanation of setting up these views in a `Fragment`, though it would be no different for an `Activity`.

## Which one to use?

You now have your choice between a `GLSurfaceView` implementation (`SurfaceView`) and a `TextureView` implementation (`TextureView`). Which to use depends on your desired application, but some general information follows:

* Both can be (and are) rendered from a dedicated GL drawing thread
* `SurfaceView` is a dedicated drawing surface embedded inside of its own view hierarchy. 
* You can control the format (transparent background, bits per pixel, etc) and size of a `SurfaceView`.
* `SurfaceView` can not be animated, transformed and scaled as "typical" Android views can.
* Two `SurfaceView` instances can not be overlapped.
* `TextureView` is treated as a general view in Android and can be animated, transformed, etc.
* `TextureView` can only be used in a hardware accelerated window. 
* `TextureView` can consume more memory than `SurfaceView`.
* `TextureView` may have a 1~3 frame latency due to the way the screen Choreographer works.

In the end, your choice may not matter one way or the other except for a few cases. Notably, older versions of Android seemed to have drawing bugs with `SurfaceView` and hardware accelerated windows, particularly on rotation or activity change. If you are going to have your application use hardware accelerated windows anyway, `TextureView` is likely a better choice. Additionally, if you need to treat the rendering surface as a normal Android view for any reason related to layout animation/modification, you will need to use `TextureView`.

To clarify:
* If you wish to use a `SurfaceView` implementation, use `SurfaceView`
* If you wish to use a `TextureView` implementation, use `TextureView`

## How To Use Them

### XML vs. Programatic Creation
The new implementations allow for you to create your rendering views in either Android XML Layouts or Java. If created in XML, you can specify certain configuration parameters, just as you would in Java, and just like configuring other Views in Android.

### XML Declaration
Below is a layout taken from the RajawaliExamples application. It creates a `FrameLayout` containing a `SurfaceView` which has been configured with XML attributes. 

```XML
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
             xmlns:surfaceview="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent" >

    <org.rajawali3d.view.SurfaceView
        android:id="@+id/rajwali_surface"
        android:layout_width="fill_parent"
        android:layout_height="fill_parent"
        surfaceview:frameRate="60.0"
        surfaceview:renderMode="RENDER_WHEN_DIRTY"/>

</FrameLayout>
```

The configurable attributes are:

* `frameRate` - A floating point number specifying the target frame rate. Default is 60.0.
* `renderMode` - A enum value specifying the desired rendering mode. Options are `RENDER_WHEN_DIRTY` and `RENDER_CONTINUOUS`. Default is `RENDER_WHEN_DIRTY`. Frame rate is irrelevant if this option is set to `RENDER_CONTINUOUS`.
* `multisamplingEnabled` - A boolean value specifying if multi sampling should be enabled or not. Default is `false`.
* `useCoverageAntiAliasing` - A boolean value specifying if coverage anti-aliasing should be enabled. Default is `false`.

`SurfaceView` has an addition option:

* `isTransparent` - A boolean value specifying if the surface should be rendered with a transparent background. Default is `false`.

Once your Activity/Fragment loads, you would simply find this view by its ID.

### Java Declaration
If you choose to create your rendering surface in Java, you create them just as you would any other Android view. The `SurfaceView` represented in XML can be created in a Fragment's `onCreateView` method as follows:

```java
final SurfaceView surface = new SurfaceView(this);
surface.setFrameRate(60.0);
surface.setRenderMode(ISurface.RENDERMODE_WHEN_DIRTY);

// Add mSurface to your root view
addContentView(surface, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT));
```

### Setting the renderer
Once you have your view created, either from XML inflation or Java, the only other step is to provide it with a `ISurfaceRenderer` instance. Typically this will be a class you wrote extending `Renderer`, though as long as you implement `ISurfaceRenderer`, it could be anything. Using the notation above for your surface (`mSurface`) and assuming the `ISurfaceRenderer` instance is `mRenderer`, the final step is:

```java
MyRenderer renderer = new MyRenderer(this);
surface.setSurfaceRenderer(renderer);
```