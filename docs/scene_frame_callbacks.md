# Scene Frame Callbacks

## About
Scene frame callbacks were added to provide a easy way for user code to tie into the render cycle while receiving timing information about the scene. These callbacks receive the typical frame delta time (measured in seconds), used by the animation system, as well as an additional parameter - the rendering elapsed time (measured in nanoseconds).

## Reasoning
Complex animations which depend on either total elapsed time (not animation elapsed time) or the position of other objects in the scene, are not easily handled by the current animation system. This is ok, as that system was not intended for such complex tasks. Additionally, other tasks such as physics simulation, or game AI, will need to be updated each frame, and depend on more than the time passed since the last frame. Rather than having an assortment of classes tracking elapsed time, the `Renderer` was tasked with tracking the elapsed time, allowing it to be passed to any number of potential callbacks.

## Implementation
The scene frame callbacks are a simple implementation. The abstract class `rajawali.scene.ASceneFrameCallback` has added. User code simply needs to extend this class, implementing the following two methods:

```java
public abstract void onPreFrame(long sceneTime, double deltaTime);

public abstract void onPostFrame(long sceneTime, double deltaTime);
```

As their name implies, these methods are called before the frame, and after the frame. More specifically, `onPreFrame(...)` is called prior to any updates to the scene, including camera frustum, model-view-projection matrix, and animations. It is however called after any pending frame tasks are executed (things such as adding/removing objects, changing cameras, registering callbacks/animations, etc). `onPostFrame(...)` is called after all rendering tasks have completed. This includes the usual drawing, multiple render passes, plugins and debugging tools such as bounding box, scene graph and view frustum drawing.

A user's implementation must implement both of these by contract, however, undesired callbacks can simply be left with empty method bodies. To prevent inefficiently calling empty functions, two other methods exist in `ASceneFrameCallback`:

```java
public boolean callPreFrame() {
    return false;
}

public boolean callPostFrame() {
    return false;
}
```

The `Scene` class keeps two lists of callbacks, one to process as pre- callbacks, and the other to process as post- callbacks. When registering a callback (more on that later), these methods are called to determine which of these lists to place the callback in. By default, both methods return `false` - this means that the registration will simply drop the callback and not place it in either list. Overriding `callPreFrame()` to return `true` will cause the registration to add the callback to the pre- list. Similarly, overriding `callPostFrame()` to return `true` will add it to the post- list. You may choose one, both or none (proguard entropy class anyone?).

Registering a frame callback is as simple as handling an animation. Three new methods exist in `Scene`:

```java
public boolean registerFrameCallback(ASceneFrameCallback callback);

public boolean unregisterFrameCallback(ASceneFrameCallback callback);

public boolean clearFrameCallbacks();
```

The function of each one should be obvious, and they are documented accordingly. One key point to keep in mind however - `clearFrameCallbacks()` removes all pre- *AND* post- callbacks. There is currently no way to remove only a pre- callback or only a post- callback. There are a few reasons for this, the biggest being that it would be cumbersome to handle the case of some dual callbacks combined with single callback implementations.

## Example

See the "Frame Callbacks" example under "Scenes" in the [RajawaliExamples](https://github.com/Rajawali/RajawaliExamples/blob/master/examples/src/main/java/com/monyetmabuk/rajawali/tutorials/examples/scene/SceneFrameCallbackFragment.java) repo for a sample implementation.