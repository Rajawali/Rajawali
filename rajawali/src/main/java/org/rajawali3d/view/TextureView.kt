package org.rajawali3d.view

import android.annotation.TargetApi
import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import org.rajawali3d.R
import org.rajawali3d.renderer.ISurfaceRenderer
import org.rajawali3d.util.Capabilities
import org.rajawali3d.util.egl.RajawaliEGLConfigChooser
import java.lang.ref.WeakReference
import java.util.*
import javax.microedition.khronos.egl.*
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10

/**
 * Rajawali version of a [TextureView]. If you plan on using Rajawali with a [TextureView],
 * it is imperative that you extend this class or life cycle events may not function as you expect.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
class TextureView : android.view.TextureView, ISurface {

    private val thisWeakRef = WeakReference(this)

    protected var frameRateTexture = 60.0
    protected var mRenderMode = ISurface.RENDERMODE_WHEN_DIRTY
    protected var antiAliasingConfig: ISurface.ANTI_ALIASING_CONFIG = ISurface.ANTI_ALIASING_CONFIG.NONE
    protected var bitsRed = 5
    protected var bitsGreen = 6
    protected var bitsBlue = 5
    protected var bitsAlpha = 0
    protected var bitsDepth = 16
    protected var multiSampleCount = 0

    private var glThread: GLThread? = null
    private var detached: Boolean = false
    private var eglConfigChooser: GLSurfaceView.EGLConfigChooser? = null
    private var eglContextFactory: GLSurfaceView.EGLContextFactory? = null
    private var eglWindowSurfaceFactory: GLSurfaceView.EGLWindowSurfaceFactory? = null
    private var eglContextClientVersion: Int = 0

    /**
     * @return true if the EGL context will be preserved when paused
     */
    /**
     * Control whether the EGL context is preserved when the TextureView is paused and
     * resumed.
     *
     *
     * If set to true, then the EGL context may be preserved when the TextureView is paused.
     * Whether the EGL context is actually preserved or not depends upon whether the
     * Android device that the program is running on can support an arbitrary number of EGL
     * contexts or not. Devices that can only support a limited number of EGL contexts must
     * release the  EGL context in order to allow multiple applications to share the GPU.
     *
     *
     * If set to false, the EGL context will be released when the TextureView is paused,
     * and recreated when the TextureView is resumed.
     *
     * The default is false.
     */
    var preserveEGLContextOnPause: Boolean = false

    protected var rendererDelegate: RendererDelegate? = null

    /**
     * Get the current rendering mode. May be called
     * from any thread. Must not be called before a renderer has been set.
     *
     * @return the current rendering mode.
     * @see .RENDERMODE_CONTINUOUSLY
     *
     * @see .RENDERMODE_WHEN_DIRTY
     */
    /**
     * Set the rendering mode. When renderMode is
     * RENDERMODE_CONTINUOUSLY, the renderer is called
     * repeatedly to re-render the scene. When renderMode
     * is RENDERMODE_WHEN_DIRTY, the renderer only rendered when the surface
     * is created, or when [.requestRenderUpdate] is called. Defaults to RENDERMODE_CONTINUOUSLY.
     *
     *
     * Using RENDERMODE_WHEN_DIRTY can improve battery life and overall system performance
     * by allowing the GPU and CPU to idle when the view does not need to be updated.
     *
     *
     * This method can only be called after [.setSurfaceRenderer]
     *
     * @param renderMode one of the RENDERMODE_X constants
     *
     * @see .RENDERMODE_CONTINUOUSLY
     *
     * @see .RENDERMODE_WHEN_DIRTY
     */
    private var renderModeInternal: Int
        get() = glThread!!.renderMode
        set(renderMode) {
            glThread!!.renderMode = renderMode
        }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        applyAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        applyAttributes(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        applyAttributes(context, attrs)
    }

    private fun applyAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs == null) return
        val array = context.obtainStyledAttributes(attrs, R.styleable.TextureView)
        val count = array.indexCount
        for (i in 0 until count) {
            val attr = array.getIndex(i)
            if (attr == R.styleable.TextureView_frameRate) {
                frameRateTexture = array.getFloat(attr, 60.0f).toDouble()
            } else if (attr == R.styleable.TextureView_renderMode) {
                mRenderMode = array.getInt(attr, ISurface.RENDERMODE_WHEN_DIRTY)
            } else if (attr == R.styleable.TextureView_antiAliasingType) {
                antiAliasingConfig = ISurface.ANTI_ALIASING_CONFIG.fromInteger(array.getInteger(attr, ISurface.ANTI_ALIASING_CONFIG.NONE.ordinal))
            } else if (attr == R.styleable.TextureView_bitsRed) {
                bitsRed = array.getInteger(attr, 5)
            } else if (attr == R.styleable.TextureView_bitsGreen) {
                bitsGreen = array.getInteger(attr, 6)
            } else if (attr == R.styleable.TextureView_bitsBlue) {
                bitsBlue = array.getInteger(attr, 5)
            } else if (attr == R.styleable.TextureView_bitsAlpha) {
                bitsAlpha = array.getInteger(attr, 0)
            } else if (attr == R.styleable.TextureView_bitsDepth) {
                bitsDepth = array.getInteger(attr, 16)
            }
        }
        array.recycle()
    }

    private fun initialize() {
        val glesMajorVersion = Capabilities.getGLESMajorVersion()
        setEGLContextClientVersion(glesMajorVersion)

        setEGLConfigChooser(RajawaliEGLConfigChooser(glesMajorVersion, antiAliasingConfig, multiSampleCount,
                bitsRed, bitsGreen, bitsBlue, bitsAlpha, bitsDepth))
    }

    private fun checkRenderThreadState() {
        if (glThread != null) {
            throw IllegalStateException("setRenderer has already been called for this instance.")
        }
    }

    /**
     * This method is part of the SurfaceTexture.Callback interface, and is
     * not normally called or subclassed by clients of TextureView.
     */
    private fun surfaceCreated(width: Int, height: Int) {
        glThread!!.surfaceCreated(width, height)
    }

    /**
     * This method is part of the SurfaceTexture.Callback interface, and is
     * not normally called or subclassed by clients of TextureView.
     */
    private fun surfaceDestroyed() {
        // Surface will be destroyed when we return
        glThread!!.surfaceDestroyed()
    }

    /**
     * This method is part of the SurfaceTexture.Callback interface, and is
     * not normally called or subclassed by clients of TextureView.
     */
    private fun surfaceChanged(w: Int, h: Int) {
        glThread!!.onWindowResize(w, h)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        if (!isInEditMode) {
            if (visibility == View.GONE || visibility == View.INVISIBLE) {
                onPause()
            } else {
                onResume()
            }
        }
        super.onVisibilityChanged(changedView, visibility)
    }

    /**
     * This method is used as part of the View class and is not normally
     * called or subclassed by clients of TextureView.
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onAttachedToWindow reattach =$detached")
        }
        if (detached && rendererDelegate != null) {
            var renderMode = ISurface.RENDERMODE_CONTINUOUSLY
            if (glThread != null) {
                renderMode = glThread!!.renderMode
            }
            glThread = GLThread(thisWeakRef)
            if (renderMode != ISurface.RENDERMODE_CONTINUOUSLY) {
                glThread!!.renderMode = renderMode
            }
            glThread!!.start()
        }
        detached = false
    }

    override fun onDetachedFromWindow() {
        if (LOG_ATTACH_DETACH) {
            Log.v(TAG, "onDetachedFromWindow")
        }
        rendererDelegate!!.mRenderer.onRenderSurfaceDestroyed(null)
        if (glThread != null) {
            glThread!!.requestExitAndWait()
        }
        detached = true
        super.onDetachedFromWindow()
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        if (glThread != null) {
            // GLThread may still be running if this view was never
            // attached to a window.
            glThread!!.requestExitAndWait()
        }
    }

    override fun setFrameRate(rate: Double) {
        frameRateTexture = rate
        if (rendererDelegate != null) {
            rendererDelegate!!.mRenderer.frameRate = rate
        }
    }

    override fun getRenderMode(): Int {
        return if (rendererDelegate != null) {
            renderModeInternal
        } else {
            mRenderMode
        }
    }

    override fun setRenderMode(mode: Int) {
        mRenderMode = mode
        if (rendererDelegate != null) {
            renderModeInternal = mRenderMode
        }
    }

    override fun setAntiAliasingMode(config: ISurface.ANTI_ALIASING_CONFIG) {
        antiAliasingConfig = config
    }

    override fun setSampleCount(count: Int) {
        multiSampleCount = count
    }

    @Throws(IllegalStateException::class)
    override fun setSurfaceRenderer(renderer: ISurfaceRenderer) {
        if (rendererDelegate != null) throw IllegalStateException("A renderer has already been set for this view.")
        initialize()

        // Configure the EGL stuff
        checkRenderThreadState()
        if (eglConfigChooser == null) {
            throw IllegalStateException("You must set an EGL config before attempting to set a surface renderer.")
        }
        if (eglContextFactory == null) {
            eglContextFactory = DefaultContextFactory()
        }
        if (eglWindowSurfaceFactory == null) {
            eglWindowSurfaceFactory = DefaultWindowSurfaceFactory()
        }
        // Create our delegate
        val delegate = TextureView.RendererDelegate(renderer, this)
        // Create the GL thread
        glThread = GLThread(thisWeakRef)
        glThread!!.start()
        // Render mode cant be set until the GL thread exists
        renderModeInternal = mRenderMode
        // Register the delegate for callbacks
        rendererDelegate = delegate // Done to make sure we dont publish a reference before its safe.
        setSurfaceTextureListener(rendererDelegate)
    }

    override fun requestRenderUpdate() {
        glThread!!.requestRender()
    }

    /**
     * Install a custom EGLContextFactory.
     *
     * If this method is
     * called, it must be called before [.setSurfaceRenderer]
     * is called.
     *
     *
     * If this method is not called, then by default
     * a context will be created with no shared context and
     * with a null attribute list.
     */
    fun setEGLContextFactory(factory: GLSurfaceView.EGLContextFactory) {
        checkRenderThreadState()
        eglContextFactory = factory
    }

    /**
     * Install a custom EGLWindowSurfaceFactory.
     *
     * If this method is
     * called, it must be called before [.setSurfaceRenderer]
     * is called.
     *
     *
     * If this method is not called, then by default
     * a window surface will be created with a null attribute list.
     */
    fun setEGLWindowSurfaceFactory(factory: GLSurfaceView.EGLWindowSurfaceFactory) {
        checkRenderThreadState()
        eglWindowSurfaceFactory = factory
    }

    /**
     * Install a custom EGLConfigChooser.
     *
     * If this method is
     * called, it must be called before [.setSurfaceRenderer]
     * is called.
     *
     *
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose an EGLConfig that is compatible with the current
     * android.view.Surface, with a depth buffer depth of
     * at least 16 bits.
     *
     * @param configChooser [GLSurfaceView.EGLConfigChooser] The EGL Configuration chooser.
     */
    fun setEGLConfigChooser(configChooser: GLSurfaceView.EGLConfigChooser) {
        checkRenderThreadState()
        eglConfigChooser = configChooser
    }

    /**
     * Install a config chooser which will choose a config
     * with at least the specified depthSize and stencilSize,
     * and exactly the specified redSize, greenSize, blueSize and alphaSize.
     *
     * If this method is
     * called, it must be called before [.setSurfaceRenderer]
     * is called.
     *
     *
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose an RGB_888 surface with a depth buffer depth of
     * at least 16 bits.
     */
    fun setEGLConfigChooser(redSize: Int, greenSize: Int, blueSize: Int,
                            alphaSize: Int, depthSize: Int, stencilSize: Int) {
        setEGLConfigChooser(ComponentSizeChooser(redSize, greenSize,
                blueSize, alphaSize, depthSize, stencilSize))
    }

    /**
     * Inform the default EGLContextFactory and default EGLConfigChooser
     * which EGLContext client version to pick.
     *
     * Use this method to create an OpenGL ES 2.0-compatible context.
     * Example:
     * <pre class="prettyprint">
     * public MyView(Context context) {
     * super(context);
     * setEGLContextClientVersion(2); // Pick an OpenGL ES 2.0 context.
     * setRenderer(new MyRenderer());
     * }
    </pre> *
     *
     * Note: Activities which require OpenGL ES 2.0 should indicate this by
     * setting @lt;uses-feature android:glEsVersion="0x00020000" /> in the activity's
     * AndroidManifest.xml file.
     *
     * If this method is called, it must be called before [.setSurfaceRenderer]
     * is called.
     *
     * This method only affects the behavior of the default EGLContexFactory and the
     * default EGLConfigChooser. If
     * [.setEGLContextFactory] has been called, then the supplied
     * EGLContextFactory is responsible for creating an OpenGL ES 2.0-compatible context.
     * If
     * [.setEGLConfigChooser] has been called, then the supplied
     * EGLConfigChooser is responsible for choosing an OpenGL ES 2.0-compatible config.
     *
     * @param version The EGLContext client version to choose. Use 2 for OpenGL ES 2.0
     */
    fun setEGLContextClientVersion(version: Int) {
        checkRenderThreadState()
        eglContextClientVersion = version
    }

    /**
     * Inform the view that the activity is paused. The owner of this view must
     * call this method when the activity is paused. Calling this method will
     * pause the rendering thread.
     * Must not be called before a renderer has been set.
     */
    fun onPause() {
        if (rendererDelegate != null) {
            rendererDelegate!!.mRenderer.onPause()
        }
        if (glThread != null) {
            glThread!!.onPause()
        }
    }

    /**
     * Inform the view that the activity is resumed. The owner of this view must
     * call this method when the activity is resumed. Calling this method will
     * recreate the OpenGL display and resume the rendering
     * thread.
     * Must not be called before a renderer has been set.
     */
    fun onResume() {
        if (rendererDelegate != null) {
            rendererDelegate!!.mRenderer.onResume()
        }
        glThread!!.onResume()
    }

    /**
     * Queue a runnable to be run on the GL rendering thread. This can be used
     * to communicate with the Renderer on the rendering thread.
     * Must not be called before a renderer has been set.
     *
     * @param r the runnable to be run on the GL rendering thread.
     */
    fun queueEvent(r: Runnable) {
        glThread!!.queueEvent(r)
    }

    protected class RendererDelegate(internal val mRenderer: ISurfaceRenderer, internal val mRajawaliTextureView: TextureView) : SurfaceTextureListener {

        init {
            mRenderer.frameRate = if (mRajawaliTextureView.mRenderMode == ISurface.RENDERMODE_WHEN_DIRTY)
                mRajawaliTextureView.frameRateTexture
            else
                0.0
            mRenderer.setAntiAliasingMode(mRajawaliTextureView.antiAliasingConfig)
            mRenderer.setRenderSurface(mRajawaliTextureView)
            mRajawaliTextureView.setSurfaceTextureListener(this)
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            mRajawaliTextureView.surfaceCreated(width, height)
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            mRajawaliTextureView.surfaceChanged(width, height)
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            surface.release()
            mRajawaliTextureView.surfaceDestroyed()
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            // Do nothing
        }
    }

    private inner class DefaultContextFactory : GLSurfaceView.EGLContextFactory {
        private val EGL_CONTEXT_CLIENT_VERSION = 0x3098

        override fun createContext(egl: EGL10, display: EGLDisplay, config: EGLConfig): EGLContext {
            val attrib_list = intArrayOf(EGL_CONTEXT_CLIENT_VERSION, eglContextClientVersion, EGL10.EGL_NONE)

            return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT,
                    if (eglContextClientVersion != 0) attrib_list else null)
        }

        override fun destroyContext(egl: EGL10, display: EGLDisplay, context: EGLContext) {
            if (!egl.eglDestroyContext(display, context)) {
                Log.e("DefaultContextFactory", "display:$display context: $context")
                if (LOG_THREADS) {
                    Log.i("DefaultContextFactory", "tid=" + Thread.currentThread().id)
                }
                EglHelper.throwEglException("eglDestroyContex", egl.eglGetError())
            }
        }
    }

    private class DefaultWindowSurfaceFactory : GLSurfaceView.EGLWindowSurfaceFactory {

        override fun createWindowSurface(egl: EGL10, display: EGLDisplay,
                                         config: EGLConfig, nativeWindow: Any): EGLSurface? {
            var result: EGLSurface? = null
            try {
                result = egl.eglCreateWindowSurface(display, config, nativeWindow, null)
            } catch (e: IllegalArgumentException) {
                // This exception indicates that the surface flinger surface
                // is not valid. This can happen if the surface flinger surface has
                // been torn down, but the application has not yet been
                // notified via SurfaceTexture.Callback.surfaceDestroyed.
                // In theory the application should be notified first,
                // but in practice sometimes it is not. See b/4588890
                Log.e(TAG, "eglCreateWindowSurface", e)
            }

            return result
        }

        override fun destroySurface(egl: EGL10, display: EGLDisplay,
                                    surface: EGLSurface) {
            egl.eglDestroySurface(display, surface)
        }
    }

    abstract inner class BaseConfigChooser(configSpec: IntArray) : GLSurfaceView.EGLConfigChooser {

        protected var mConfigSpec: IntArray

        init {
            mConfigSpec = filterConfigSpec(configSpec)
        }

        override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig {
            val num_config = IntArray(1)
            if (!egl.eglChooseConfig(display, mConfigSpec, null, 0, num_config)) {
                throw IllegalArgumentException("eglChooseConfig failed")
            }

            val numConfigs = num_config[0]

            if (numConfigs <= 0) {
                throw IllegalArgumentException("No configs match configSpec")
            }

            val configs = arrayOfNulls<EGLConfig>(numConfigs)
            if (!egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs, num_config)) {
                throw IllegalArgumentException("eglChooseConfig#2 failed")
            }
            return chooseConfig(egl, display, configs.requireNoNulls())
                    ?: throw IllegalArgumentException("No config chosen")
        }

        internal abstract fun chooseConfig(egl: EGL10, display: EGLDisplay, configs: Array<EGLConfig>): EGLConfig?

        private fun filterConfigSpec(configSpec: IntArray): IntArray {
            if (eglContextClientVersion != 2 && eglContextClientVersion != 3) {
                return configSpec
            }
            /* We know none of the subclasses define EGL_RENDERABLE_TYPE.
             * And we know the configSpec is well formed.
             */
            val len = configSpec.size
            val newConfigSpec = IntArray(len + 2)
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1)
            newConfigSpec[len - 1] = EGL10.EGL_RENDERABLE_TYPE
            if (eglContextClientVersion == 2) {
                newConfigSpec[len] = RajawaliEGLConfigChooser.EGL_OPENGL_ES2_BIT  /* EGL_OPENGL_ES2_BIT */
            } else {
                newConfigSpec[len] = RajawaliEGLConfigChooser.EGL_OPENGL_ES3_BIT_KHR /* EGL_OPENGL_ES3_BIT_KHR */
            }
            newConfigSpec[len + 1] = EGL10.EGL_NONE
            return newConfigSpec
        }
    }

    /**
     * Choose a configuration with exactly the specified r,g,b,a sizes,
     * and at least the specified depth and stencil sizes.
     */
    inner class ComponentSizeChooser(// Subclasses can adjust these values:
            protected var mRedSize: Int, protected var mGreenSize: Int, protected var mBlueSize: Int,
            protected var mAlphaSize: Int, protected var mDepthSize: Int, protected var mStencilSize: Int) : BaseConfigChooser(intArrayOf(EGL10.EGL_RED_SIZE, mRedSize, EGL10.EGL_GREEN_SIZE, mGreenSize, EGL10.EGL_BLUE_SIZE, mBlueSize, EGL10.EGL_ALPHA_SIZE, mAlphaSize, EGL10.EGL_DEPTH_SIZE, mDepthSize, EGL10.EGL_STENCIL_SIZE, mStencilSize, EGL10.EGL_NONE)) {

        private val mValue: IntArray

        init {
            mValue = IntArray(1)
        }

        public override fun chooseConfig(egl: EGL10, display: EGLDisplay, configs: Array<EGLConfig>): EGLConfig? {
            for (config in configs) {
                val d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0)
                val s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0)
                if (d >= mDepthSize && s >= mStencilSize) {
                    val r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0)
                    val g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0)
                    val b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0)
                    val a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0)
                    if (r == mRedSize && g == mGreenSize && b == mBlueSize && a == mAlphaSize) {
                        return config
                    }
                }
            }
            return null
        }

        private fun findConfigAttrib(egl: EGL10, display: EGLDisplay, config: EGLConfig, attribute: Int, defaultValue: Int): Int {
            return if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                mValue[0]
            } else defaultValue
        }
    }

    /**
     * An EGL helper class.
     */
    private class EglHelper(private val mRajawaliTextureViewWeakRef: WeakReference<TextureView>) {
        internal var mEgl: EGL10? = null
        internal var mEglDisplay: EGLDisplay? = null
        internal var mEglSurface: EGLSurface? = null
        internal var mEglConfig: EGLConfig? = null
        internal var mEglContext: EGLContext? = null

        /**
         * Initialize EGL for a given configuration spec.
         */
        fun start() {
            if (LOG_EGL) {
                Log.w("EglHelper", "start() tid=" + Thread.currentThread().id)
            }
            /*
             * Get an EGL instance
             */
            mEgl = EGLContext.getEGL() as EGL10

            /*
             * Get to the default display.
             */
            mEglDisplay = mEgl!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

            if (mEglDisplay === EGL10.EGL_NO_DISPLAY) {
                throw RuntimeException("eglGetDisplay failed")
            }

            /*
             * We can now initialize EGL for that display
             */
            val version = IntArray(2)
            if (!mEgl!!.eglInitialize(mEglDisplay, version)) {
                throw RuntimeException("eglInitialize failed")
            }
            val view = mRajawaliTextureViewWeakRef.get()
            if (view == null) {
                mEglConfig = null
                mEglContext = null
            } else {
                mEglConfig = view.eglConfigChooser!!.chooseConfig(mEgl, mEglDisplay)

                /*
                * Create an EGL context. We want to do this as rarely as we can, because an
                * EGL context is a somewhat heavy object.
                */
                mEglContext = view.eglContextFactory!!.createContext(mEgl, mEglDisplay, mEglConfig)
            }
            if (mEglContext == null || mEglContext === EGL10.EGL_NO_CONTEXT) {
                mEglContext = null
                throwEglException("createContext")
            }
            if (LOG_EGL) {
                Log.w("EglHelper", "createContext " + mEglContext + " tid=" + Thread.currentThread().id)
            }

            mEglSurface = null
        }

        /**
         * Create an egl surface for the current SurfaceTexture surface. If a surface
         * already exists, destroy it before creating the new surface.
         *
         * @return true if the surface was created successfully.
         */
        fun createSurface(): Boolean {
            if (LOG_EGL) {
                Log.w("EglHelper", "createSurface()  tid=" + Thread.currentThread().id)
            }
            /*
             * Check preconditions.
             */
            if (mEgl == null) {
                throw RuntimeException("egl not initialized")
            }
            if (mEglDisplay == null) {
                throw RuntimeException("eglDisplay not initialized")
            }
            if (mEglConfig == null) {
                throw RuntimeException("mEglConfig not initialized")
            }

            /*
             *  The window size has changed, so we need to create a new
             *  surface.
             */
            destroySurfaceImp()

            /*
             * Create an EGL surface we can render into.
             */
            val view = mRajawaliTextureViewWeakRef.get()
            if (view != null) {
                mEglSurface = view.eglWindowSurfaceFactory!!.createWindowSurface(mEgl,
                        mEglDisplay, mEglConfig, view.surfaceTexture)
            } else {
                mEglSurface = null
            }

            if (mEglSurface == null || mEglSurface === EGL10.EGL_NO_SURFACE) {
                val error = mEgl!!.eglGetError()
                if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                    Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.")
                }
                return false
            }

            /*
             * Before we can issue GL commands, we need to make sure
             * the context is current and bound to a surface.
             */
            if (!mEgl!!.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
                /*
                 * Could not make the context current, probably because the underlying
                 * SurfaceView surface has been destroyed.
                 */
                logEglErrorAsWarning("EGLHelper", "eglMakeCurrent", mEgl!!.eglGetError())
                return false
            }

            return true
        }

        /**
         * Create a GL object for the current EGL context.
         *
         * @return [GL] The GL interface for the current context.
         */
        internal fun createGL(): GL {
            return mEglContext!!.gl
        }

        /**
         * Display the current render surface.
         *
         * @return the EGL error code from eglSwapBuffers.
         */
        fun swap(): Int {
            return if (!mEgl!!.eglSwapBuffers(mEglDisplay, mEglSurface)) {
                mEgl!!.eglGetError()
            } else EGL10.EGL_SUCCESS
        }

        fun destroySurface() {
            if (LOG_EGL) {
                Log.w("EglHelper", "destroySurface()  tid=" + Thread.currentThread().id)
            }
            destroySurfaceImp()
        }

        private fun destroySurfaceImp() {
            if (mEglSurface != null && mEglSurface !== EGL10.EGL_NO_SURFACE) {
                mEgl!!.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_CONTEXT)
                val view = mRajawaliTextureViewWeakRef.get()
                if (view != null) {
                    view.eglWindowSurfaceFactory!!.destroySurface(mEgl, mEglDisplay, mEglSurface)
                }
                mEglSurface = null
            }
        }

        fun finish() {
            if (LOG_EGL) {
                Log.w("EglHelper", "finish() tid=" + Thread.currentThread().id)
            }
            if (mEglContext != null) {
                val view = mRajawaliTextureViewWeakRef.get()
                if (view != null) {
                    view.eglContextFactory!!.destroyContext(mEgl, mEglDisplay, mEglContext)
                }
                mEglContext = null
            }
            if (mEglDisplay != null) {
                mEgl!!.eglTerminate(mEglDisplay)
                mEglDisplay = null
            }
        }

        private fun throwEglException(function: String) {
            throwEglException(function, mEgl!!.eglGetError())
        }

        companion object {

            fun throwEglException(function: String, error: Int) {
                val message = formatEglError(function, error)
                if (LOG_THREADS) {
                    Log.e("EglHelper", "throwEglException tid=" + Thread.currentThread().id + " " + message)
                }
                throw RuntimeException(message)
            }

            fun logEglErrorAsWarning(tag: String, function: String, error: Int) {
                Log.w(tag, formatEglError(function, error))
            }

            fun formatEglError(function: String, error: Int): String {
                return function + " failed: " + getErrorString(error)
            }

            fun getErrorString(error: Int): String {
                when (error) {
                    EGL10.EGL_SUCCESS -> return "EGL_SUCCESS"
                    EGL10.EGL_NOT_INITIALIZED -> return "EGL_NOT_INITIALIZED"
                    EGL10.EGL_BAD_ACCESS -> return "EGL_BAD_ACCESS"
                    EGL10.EGL_BAD_ALLOC -> return "EGL_BAD_ALLOC"
                    EGL10.EGL_BAD_ATTRIBUTE -> return "EGL_BAD_ATTRIBUTE"
                    EGL10.EGL_BAD_CONFIG -> return "EGL_BAD_CONFIG"
                    EGL10.EGL_BAD_CONTEXT -> return "EGL_BAD_CONTEXT"
                    EGL10.EGL_BAD_CURRENT_SURFACE -> return "EGL_BAD_CURRENT_SURFACE"
                    EGL10.EGL_BAD_DISPLAY -> return "EGL_BAD_DISPLAY"
                    EGL10.EGL_BAD_MATCH -> return "EGL_BAD_MATCH"
                    EGL10.EGL_BAD_NATIVE_PIXMAP -> return "EGL_BAD_NATIVE_PIXMAP"
                    EGL10.EGL_BAD_NATIVE_WINDOW -> return "EGL_BAD_NATIVE_WINDOW"
                    EGL10.EGL_BAD_PARAMETER -> return "EGL_BAD_PARAMETER"
                    EGL10.EGL_BAD_SURFACE -> return "EGL_BAD_SURFACE"
                    EGL11.EGL_CONTEXT_LOST -> return "EGL_CONTEXT_LOST"
                    else -> return "0x" + Integer.toHexString(error).toUpperCase(Locale.US)
                }
            }
        }
    }

    /**
     * A generic GL Thread. Takes care of initializing EGL and GL. Delegates
     * to a Renderer instance to do the actual drawing. Can be configured to
     * render continuously or on request.
     *
     *
     * All potentially blocking synchronization is done through the
     * sGLThreadManager object. This avoids multiple-lock ordering issues.
     */
    internal class GLThread(
            /**
             * Set once at thread construction time, nulled out when the parent view is garbage
             * called. This weak reference allows the TextureView to be garbage collected while
             * the RajawaliGLThread is still alive.
             */
            private val mRajawaliTextureViewWeakRef: WeakReference<TextureView>) : Thread() {

        // Once the thread is started, all accesses to the following member
        // variables are protected by the sGLThreadManager monitor
        private var mShouldExit: Boolean = false
        var mExited: Boolean = false
        private var mRequestPaused: Boolean = false
        private var mPaused: Boolean = false
        private var mHasSurface: Boolean = false
        private var mSurfaceIsBad: Boolean = false
        private var mWaitingForSurface: Boolean = false
        private var haveEglContext: Boolean = false
        private var haveEglSurface: Boolean = false
        private var mFinishedCreatingEglSurface: Boolean = false
        private var mShouldReleaseEglContext: Boolean = false
        private var mWidth: Int = 0
        private var mHeight: Int = 0
        private var mRenderMode: Int = 0
        private var mRequestRender: Boolean = false
        private var mRenderComplete: Boolean = false
        private val mEventQueue = ArrayList<Runnable>()
        private var mSizeChanged = true

        // End of member variables protected by the sGLThreadManager monitor.

        private var eglHelper: EglHelper? = null

        var renderMode: Int
            get() = synchronized(sGLThreadManager) {
                return mRenderMode
            }
            set(renderMode) {
                if (!(ISurface.RENDERMODE_WHEN_DIRTY <= renderMode && renderMode <= ISurface.RENDERMODE_CONTINUOUSLY)) {
                    throw IllegalArgumentException("renderMode")
                }
                synchronized(sGLThreadManager) {
                    mRenderMode = renderMode
                    sGLThreadManager.notifyAll()
                }
            }

        init {
            mWidth = 0
            mHeight = 0
            mRequestRender = true
            mRenderMode = ISurface.RENDERMODE_CONTINUOUSLY
        }

        override fun run() {
            name = "RajawaliGLThread $id"
            if (LOG_THREADS) {
                Log.i("RajawaliGLThread", "starting tid=$id")
            }

            try {
                guardedRun()
            } catch (e: InterruptedException) {
                // fall thru and exit normally
            } finally {
                sGLThreadManager.threadExiting(this)
            }
        }

        /*
         * This private method should only be called inside a
         * synchronized(sGLThreadManager) block.
         */
        private fun stopEglSurfaceLocked() {
            if (haveEglSurface) {
                haveEglSurface = false
                eglHelper!!.destroySurface()
            }
        }

        /*
         * This private method should only be called inside a
         * synchronized(sGLThreadManager) block.
         */
        private fun stopEglContextLocked() {
            if (haveEglContext) {
                eglHelper!!.finish()
                haveEglContext = false
                sGLThreadManager.releaseEglContextLocked(this)
            }
        }

        @Throws(InterruptedException::class)
        private fun guardedRun() {
            eglHelper = EglHelper(mRajawaliTextureViewWeakRef)
            haveEglContext = false
            haveEglSurface = false
            try {
                var gl: GL10? = null
                var createEglContext = false
                var createEglSurface = false
                var createGlInterface = false
                var lostEglContext = false
                var sizeChanged = false
                var wantRenderNotification = false
                var doRenderNotification = false
                var askedToReleaseEglContext = false
                var w = 0
                var h = 0
                var event: Runnable? = null

                while (true) {
                    synchronized(sGLThreadManager) {
                        while (true) {
                            if (mShouldExit) {
                                return
                            }

                            if (!mEventQueue.isEmpty()) {
                                event = mEventQueue.removeAt(0)
                                break
                            }

                            // Update the pause state.
                            var pausing = false
                            if (mPaused != mRequestPaused) {
                                pausing = mRequestPaused
                                mPaused = mRequestPaused
                                sGLThreadManager.notifyAll()
                                if (LOG_PAUSE_RESUME) {
                                    Log.i("RajawaliGLThread", "mPaused is now $mPaused tid=$id")
                                }
                            }

                            // Do we need to give up the EGL context?
                            if (mShouldReleaseEglContext) {
                                if (LOG_SURFACE) {
                                    Log.i("RajawaliGLThread", "releasing EGL context because asked to tid=$id")
                                }
                                stopEglSurfaceLocked()
                                stopEglContextLocked()
                                mShouldReleaseEglContext = false
                                askedToReleaseEglContext = true
                            }

                            // Have we lost the EGL context?
                            if (lostEglContext) {
                                stopEglSurfaceLocked()
                                stopEglContextLocked()
                                lostEglContext = false
                            }

                            // When pausing, release the EGL surface:
                            if (pausing && haveEglSurface) {
                                if (LOG_SURFACE) {
                                    Log.i("RajawaliGLThread", "releasing EGL surface because paused tid=$id")
                                }
                                stopEglSurfaceLocked()
                            }

                            // When pausing, optionally release the EGL Context:
                            if (pausing && haveEglContext) {
                                val view = mRajawaliTextureViewWeakRef.get()
                                val preserveEglContextOnPause = view != null && view.preserveEGLContextOnPause
                                if (!preserveEglContextOnPause || sGLThreadManager.shouldReleaseEGLContextWhenPausing()) {
                                    stopEglContextLocked()
                                    if (LOG_SURFACE) {
                                        Log.i("RajawaliGLThread", "releasing EGL context because paused tid=$id")
                                    }
                                }
                            }

                            // When pausing, optionally terminate EGL:
                            if (pausing) {
                                if (sGLThreadManager.shouldTerminateEGLWhenPausing()) {
                                    eglHelper!!.finish()
                                    if (LOG_SURFACE) {
                                        Log.i("RajawaliGLThread", "terminating EGL because paused tid=$id")
                                    }
                                }
                            }

                            // Have we lost the SurfaceView surface?
                            if (!mHasSurface && !mWaitingForSurface) {
                                if (LOG_SURFACE) {
                                    Log.i("RajawaliGLThread", "noticed surfaceView surface lost tid=$id")
                                }
                                if (haveEglSurface) {
                                    stopEglSurfaceLocked()
                                }
                                mWaitingForSurface = true
                                mSurfaceIsBad = false
                                sGLThreadManager.notifyAll()
                            }

                            // Have we acquired the surface view surface?
                            if (mHasSurface && mWaitingForSurface) {
                                if (LOG_SURFACE) {
                                    Log.i("RajawaliGLThread", "noticed surfaceView surface acquired tid=$id")
                                }
                                mWaitingForSurface = false
                                sGLThreadManager.notifyAll()
                            }

                            if (doRenderNotification) {
                                if (LOG_SURFACE) {
                                    Log.i("RajawaliGLThread", "sending render notification tid=$id")
                                }
                                wantRenderNotification = false
                                doRenderNotification = false
                                mRenderComplete = true
                                sGLThreadManager.notifyAll()
                            }

                            // Ready to draw?
                            if (readyToDraw()) {
                                // If we don't have an EGL context, try to acquire one.
                                if (!haveEglContext) {
                                    if (askedToReleaseEglContext) {
                                        askedToReleaseEglContext = false
                                    } else if (sGLThreadManager.tryAcquireEglContextLocked(this)) {
                                        try {
                                            eglHelper!!.start()
                                        } catch (t: RuntimeException) {
                                            sGLThreadManager.releaseEglContextLocked(this)
                                            throw t
                                        }

                                        haveEglContext = true
                                        createEglContext = true

                                        sGLThreadManager.notifyAll()
                                    }
                                }

                                if (haveEglContext && !haveEglSurface) {
                                    haveEglSurface = true
                                    createEglSurface = true
                                    createGlInterface = true
                                    sizeChanged = true
                                }

                                if (haveEglSurface) {
                                    if (mSizeChanged) {
                                        sizeChanged = true
                                        w = mWidth
                                        h = mHeight
                                        wantRenderNotification = true
                                        if (LOG_SURFACE) {
                                            Log.i("RajawaliGLThread", "noticing that we want render notification tid=$id")
                                        }

                                        // Destroy and recreate the EGL surface.
                                        createEglSurface = true

                                        mSizeChanged = false
                                    }
                                    mRequestRender = false
                                    sGLThreadManager.notifyAll()
                                    break
                                }
                            }

                            // By design, this is the only place in a RajawaliGLThread thread where we wait().
                            if (LOG_THREADS) {
                                Log.i("RajawaliGLThread", "waiting tid=" + id
                                        + " haveEglContext: " + haveEglContext
                                        + " haveEglSurface: " + haveEglSurface
                                        + " mFinishedCreatingEglSurface: " + mFinishedCreatingEglSurface
                                        + " mPaused: " + mPaused
                                        + " mHasSurface: " + mHasSurface
                                        + " mSurfaceIsBad: " + mSurfaceIsBad
                                        + " mWaitingForSurface: " + mWaitingForSurface
                                        + " mWidth: " + mWidth
                                        + " mHeight: " + mHeight
                                        + " mRequestRender: " + mRequestRender
                                        + " mRenderMode: " + mRenderMode)
                            }
                            sGLThreadManager.wait()
                        }
                    } // end of synchronized(sGLThreadManager)

                    if (event != null) {
                        event!!.run()
                        event = null
                        continue
                    }

                    if (createEglSurface) {
                        if (LOG_SURFACE) {
                            Log.w("RajawaliGLThread", "egl createSurface")
                        }
                        if (eglHelper!!.createSurface()) {
                            synchronized(sGLThreadManager) {
                                mFinishedCreatingEglSurface = true
                                sGLThreadManager.notifyAll()
                            }
                        } else {
                            synchronized(sGLThreadManager) {
                                mFinishedCreatingEglSurface = true
                                mSurfaceIsBad = true
                                sGLThreadManager.notifyAll()
                            }
                            continue
                        }
                        createEglSurface = false
                    }

                    if (createGlInterface) {
                        gl = eglHelper!!.createGL() as GL10

                        sGLThreadManager.checkGLDriver(gl)
                        createGlInterface = false
                    }

                    if (createEglContext) {
                        if (LOG_RENDERER) {
                            Log.w("RajawaliGLThread", "onSurfaceCreated")
                        }
                        val view = mRajawaliTextureViewWeakRef.get()
                        if (view != null) {
                            view.rendererDelegate!!.mRenderer.onRenderSurfaceCreated(eglHelper!!.mEglConfig, gl, -1, -1)
                        }
                        createEglContext = false
                    }

                    if (sizeChanged) {
                        if (LOG_RENDERER) {
                            Log.w("RajawaliGLThread", "onSurfaceChanged($w, $h)")
                        }
                        val view = mRajawaliTextureViewWeakRef.get()
                        if (view != null) {
                            view.rendererDelegate!!.mRenderer.onRenderSurfaceSizeChanged(gl, w, h)
                        }
                        sizeChanged = false
                    }

                    if (LOG_RENDERER_DRAW_FRAME) {
                        Log.w("RajawaliGLThread", "onDrawFrame tid=$id")
                    }
                    run {
                        val view = mRajawaliTextureViewWeakRef.get()
                        if (view != null) {
                            view.rendererDelegate!!.mRenderer.onRenderFrame(gl)
                        }
                    }
                    val swapError = eglHelper!!.swap()
                    when (swapError) {
                        EGL10.EGL_SUCCESS -> {
                        }
                        EGL11.EGL_CONTEXT_LOST -> {
                            if (LOG_SURFACE) {
                                Log.i("RajawaliGLThread", "egl context lost tid=$id")
                            }
                            lostEglContext = true
                        }
                        else -> {
                            // Other errors typically mean that the current surface is bad,
                            // probably because the SurfaceView surface has been destroyed,
                            // but we haven't been notified yet.
                            // Log the error to help developers understand why rendering stopped.
                            EglHelper.logEglErrorAsWarning("RajawaliGLThread", "eglSwapBuffers", swapError)

                            synchronized(sGLThreadManager) {
                                mSurfaceIsBad = true
                                sGLThreadManager.notifyAll()
                            }
                        }
                    }

                    if (wantRenderNotification) {
                        doRenderNotification = true
                    }
                }

            } finally {
                /*
                 * clean-up everything...
                 */
                synchronized(sGLThreadManager) {
                    stopEglSurfaceLocked()
                    stopEglContextLocked()
                }
            }
        }

        fun ableToDraw(): Boolean {
            return haveEglContext && haveEglSurface && readyToDraw()
        }

        private fun readyToDraw(): Boolean {
            return (!mPaused && mHasSurface && !mSurfaceIsBad
                    && mWidth > 0 && mHeight > 0
                    && (mRequestRender || mRenderMode == ISurface.RENDERMODE_CONTINUOUSLY))
        }

        fun requestRender() {
            synchronized(sGLThreadManager) {
                mRequestRender = true
                sGLThreadManager.notifyAll()
            }
        }

        fun surfaceCreated(w: Int, h: Int) {
            synchronized(sGLThreadManager) {
                if (LOG_THREADS) {
                    Log.i("RajawaliGLThread", "surfaceCreated tid=$id")
                }
                mHasSurface = true
                mWidth = w
                mHeight = h
                mFinishedCreatingEglSurface = false
                sGLThreadManager.notifyAll()
                while (mWaitingForSurface
                        && !mFinishedCreatingEglSurface
                        && !mExited) {
                    try {
                        sGLThreadManager.wait()
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }

                }
            }
        }

        fun surfaceDestroyed() {
            synchronized(sGLThreadManager) {
                if (LOG_THREADS) {
                    Log.i("RajawaliGLThread", "surfaceDestroyed tid=$id")
                }
                mHasSurface = false
                sGLThreadManager.notifyAll()
                while (!mWaitingForSurface && !mExited) {
                    try {
                        sGLThreadManager.wait()
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }

                }
            }
        }

        fun onPause() {
            synchronized(sGLThreadManager) {
                if (LOG_PAUSE_RESUME) {
                    Log.i("RajawaliGLThread", "onPause tid=$id")
                }
                mRequestPaused = true
                sGLThreadManager.notifyAll()
                while (!mExited && !mPaused) {
                    if (LOG_PAUSE_RESUME) {
                        Log.i("Main thread", "onPause waiting for mPaused.")
                    }
                    try {
                        sGLThreadManager.wait()
                    } catch (ex: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }

                }
            }
        }

        fun onResume() {
            synchronized(sGLThreadManager) {
                if (LOG_PAUSE_RESUME) {
                    Log.i("RajawaliGLThread", "onResume tid=$id")
                }
                mRequestPaused = false
                mRequestRender = true
                mRenderComplete = false
                sGLThreadManager.notifyAll()
                while (!mExited && mPaused && !mRenderComplete) {
                    if (LOG_PAUSE_RESUME) {
                        Log.i("Main thread", "onResume waiting for !mPaused.")
                    }
                    try {
                        sGLThreadManager.wait()
                    } catch (ex: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }

                }
            }
        }

        fun onWindowResize(w: Int, h: Int) {
            synchronized(sGLThreadManager) {
                mWidth = w
                mHeight = h
                mSizeChanged = true
                mRequestRender = true
                mRenderComplete = false
                sGLThreadManager.notifyAll()

                // Wait for thread to react to resize and render a frame
                while (!mExited && !mPaused && !mRenderComplete
                        && ableToDraw()) {
                    if (LOG_SURFACE) {
                        Log.i("Main thread", "onWindowResize waiting for render complete from tid=$id")
                    }
                    try {
                        sGLThreadManager.wait()
                    } catch (ex: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }

                }
            }
        }

        fun requestExitAndWait() {
            // don't call this from RajawaliGLThread thread or it is a guaranteed
            // deadlock!
            synchronized(sGLThreadManager) {
                mShouldExit = true
                sGLThreadManager.notifyAll()
                while (!mExited) {
                    try {
                        sGLThreadManager.wait()
                    } catch (ex: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }

                }
            }
        }

        fun requestReleaseEglContextLocked() {
            mShouldReleaseEglContext = true
            sGLThreadManager.notifyAll()
        }

        /**
         * Queue an "event" to be run on the GL rendering thread.
         *
         * @param r the runnable to be run on the GL rendering thread.
         */
        fun queueEvent(r: Runnable?) {
            if (r == null) {
                throw IllegalArgumentException("r must not be null")
            }
            synchronized(sGLThreadManager) {
                mEventQueue.add(r)
                sGLThreadManager.notifyAll()
            }
        }
    }

    // Every class in Kotlin inherits from Any, but Any doesn't declare wait(), notify() and notifyAll(),
    // meaning that these methods can't be called on a Kotlin class.
    // It's hacky to extend from Object and concurrency mechanisms can done in a better way, eg Coroutines
    private class GLThreadManager : Object() {

        private var mGLESVersionCheckComplete: Boolean = false
        private var mGLESVersion: Int = 0
        private var mGLESDriverCheckComplete: Boolean = false
        private var mMultipleGLESContextsAllowed: Boolean = false
        private var mLimitedGLESContexts: Boolean = false
        private var mEglOwner: GLThread? = null

        @Synchronized
        fun threadExiting(thread: GLThread) {
            if (LOG_THREADS) {
                Log.i("RajawaliGLThread", "exiting tid=" + thread.id)
            }
            thread.mExited = true
            if (mEglOwner === thread) {
                mEglOwner = null
            }
            notifyAll()
        }

        /*
         * Tries once to acquire the right to use an EGL
         * context. Does not block. Requires that we are already
         * in the sGLThreadManager monitor when this is called.
         *
         * @return true if the right to use an EGL context was acquired.
         */
        fun tryAcquireEglContextLocked(thread: GLThread): Boolean {
            if (mEglOwner === thread || mEglOwner == null) {
                mEglOwner = thread
                notifyAll()
                return true
            }
            checkGLESVersion()
            if (mMultipleGLESContextsAllowed) {
                return true
            }
            // Notify the owning thread that it should release the context.
            // TODO: implement a fairness policy. Currently
            // if the owning thread is drawing continuously it will just
            // reacquire the EGL context.
            if (mEglOwner != null) {
                mEglOwner!!.requestReleaseEglContextLocked()
            }
            return false
        }

        /*
         * Releases the EGL context. Requires that we are already in the
         * sGLThreadManager monitor when this is called.
         */
        fun releaseEglContextLocked(thread: GLThread) {
            if (mEglOwner === thread) {
                mEglOwner = null
            }
            notifyAll()
        }

        @Synchronized
        fun shouldReleaseEGLContextWhenPausing(): Boolean {
            // Release the EGL context when pausing even if
            // the hardware supports multiple EGL contexts.
            // Otherwise the device could run out of EGL contexts.
            return mLimitedGLESContexts
        }

        @Synchronized
        fun shouldTerminateEGLWhenPausing(): Boolean {
            checkGLESVersion()
            return !mMultipleGLESContextsAllowed
        }

        private fun checkGLESVersion() {
            if (!mGLESVersionCheckComplete) {
                mGLESVersion = Capabilities.getGLESMajorVersion()
                if (mGLESVersion >= kGLES_20) {
                    mMultipleGLESContextsAllowed = true
                }
                if (LOG_SURFACE) {
                    Log.w(TAG, "checkGLESVersion mGLESVersion =" +
                            " " + mGLESVersion + " mMultipleGLESContextsAllowed = " + mMultipleGLESContextsAllowed)
                }
                mGLESVersionCheckComplete = true
            }
        }

        @Synchronized
        fun checkGLDriver(gl: GL10?) {
            if (!mGLESDriverCheckComplete) {
                checkGLESVersion()
                val renderer = gl!!.glGetString(GL10.GL_RENDERER)
                if (mGLESVersion < kGLES_20) {
                    mMultipleGLESContextsAllowed = !renderer.startsWith(kMSM7K_RENDERER_PREFIX)
                    notifyAll()
                }
                mLimitedGLESContexts = !mMultipleGLESContextsAllowed
                if (LOG_SURFACE) {
                    Log.w(TAG, "checkGLDriver renderer = \"" + renderer + "\" multipleContextsAllowed = "
                            + mMultipleGLESContextsAllowed
                            + " mLimitedGLESContexts = " + mLimitedGLESContexts)
                }
                mGLESDriverCheckComplete = true
            }
        }

        companion object {
            private const val TAG = "RajawaliGLThreadManager"
            private const val kGLES_20 = 0x20000
            private const val kMSM7K_RENDERER_PREFIX = "Q3Dimension MSM7500 "
        }
    }

    companion object {
        private const val TAG = "TextureView"
        private const val LOG_ATTACH_DETACH = false
        private const val LOG_THREADS = false
        private const val LOG_PAUSE_RESUME = false
        private const val LOG_SURFACE = true
        private const val LOG_RENDERER = false
        private const val LOG_RENDERER_DRAW_FRAME = false
        private const val LOG_EGL = false

        private val sGLThreadManager = GLThreadManager()
    }
}
