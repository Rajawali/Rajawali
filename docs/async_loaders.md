# Asynchronous Loaders

## About
Asynchronous loading allows user code to request the load of a model or scene to happen on a background thread, rather than the GL thread. This allows rendering to progress as models are loaded.

## Reasoning
`Scene` is most powerful when used to build scenes while rendering another scene is ongoing, and then swapping the scenes when the new one is ready. If you were to try to load models synchronously to accomplish this, all drawing would cease until the loading was completed. Similarly, if you only have one scene but it is very complex, loading models synchronously could take a long time to complete. Alternatively, you could manage the loading yourself in another thread. One of the goals of Rajawali is to make it easy to perform more complex tasks in graphics, so while you still have the option of managing the threading yourself, we have provided a simple implementation which will allow you to request that the engine asynchronously load a model and notify you when it is ready.

## Implementation
`Render` now has three new methods:

```java
public ALoader loadModel(ALoader loader, IAsyncLoaderCallback callback, int tag)
public ALoader loadModel(Class<? extends ALoader> loaderClass, IAsyncLoaderCallback callback, int resID)
public ALoader loadModel(Class<? extends ALoader> loaderClass, IAsyncLoaderCallback callback, int resID, int tag)
```

You may use any of these methods to request the automatic, asynchronous loading. The first will simply take a loader that you have configured. The other two will take the class of the loader and configure it for you. `resID` is the resource id of the file to load and `tag` is an identifier for this loader. By default `tag` is identical to the `resID` and in most (if not all) cases, this should be sufficient. The final parameter,`callback` is an implementation of the new interface `IAsyncLoaderCallback`. You must provide an implementation of this which will deal with the success or failure of loading, notified via `IAsyncLoaderCallback.onModelLoadComplete(ALoader loader)` and `IAsyncLoaderCallback.onModelLoadFailed(ALoader loader)`.

For an example implementation, see the [Async Model Load Example](https://github.com/Rajawali/RajawaliExamples/blob/master/examples/src/main/java/com/monyetmabuk/rajawali/tutorials/examples/loaders/AsyncLoadModelFragment.java) in the RajawaliExamples app. A snippet is provided below:

```java
private final class LoadModelRenderer extends AExampleRenderer implements IAsyncLoaderCallback {
    protected void initScene() {
        //Begin loading
        final LoaderOBJ loaderOBJ = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.multiobjects_obj);
        loadModel(loaderOBJ, this, R.raw.multiobjects_obj);
    }

    @Override
    public void onModelLoadComplete(ALoader aLoader) {
        RajLog.d(this, "Model load complete: " + aLoader);
        final LoaderOBJ obj = (LoaderOBJ) aLoader;
        final Object3D parsedObject = obj.getParsedObject();
        parsedObject.setPosition(Vector3.ZERO);
        getCurrentScene().addChild(parsedObject);
    }

    @Override
    public void onModelLoadFailed(ALoader aLoader) {
         RajLog.e(this, "Model load failed: " + aLoader);
    }
}
```