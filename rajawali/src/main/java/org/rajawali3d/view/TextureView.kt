@file:Suppress("ConstantConditionIf")

package org.rajawali3d.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import org.rajawali3d.R
import org.rajawali3d.renderer.ISurfaceRenderer
import org.rajawali3d.util.Capabilities
import org.rajawali3d.util.egl.RajawaliEGLConfigChooser
import org.rajawali3d.util.egl.ResultConfigChooser
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
@Suppress("MemberVisibilityCanBePrivate")
open class TextureView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : android.view.TextureView(context, attrs, defStyleAttr), ISurface {

    init {
        applyAttributes(context, attrs)
    }

    @Suppress("LeakingThis")
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
    private var eglConfigChooser: IRajawaliEglConfigChooser? = null
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
     * The rendering mode.
     *
     * When renderMode is [ISurface.RENDERMODE_CONTINUOUSLY], the renderer is called repeatedly to re-render the scene.
     * When renderMode is [ISurface.RENDERMODE_WHEN_DIRTY], the renderer only rendered when the surface is created, or
     * when [.requestRenderUpdate] is called. Defaults to [ISurface.RENDERMODE_CONTINUOUSLY].
     *
     * Using [ISurface.RENDERMODE_WHEN_DIRTY] can improve battery life and overall system performance by allowing the
     * GPU and CPU to idle when the view does not need to be updated.
     *
     * This method can only be called after [setSurfaceRenderer]
     *
     * @see [ISurface.RENDERMODE_CONTINUOUSLY]
     * @see [ISurface.RENDERMODE_WHEN_DIRTY]
     */
    protected var renderModeInternal: Int
        get() = glThread?.renderMode ?: throw IllegalStateException("GLThread not initialized")
        private set(renderMode) {
            glThread?.apply { this.renderMode = renderMode }
                    ?: throw IllegalStateException("GLThread not initialized")
        }

    private fun applyAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs == null) return
        val array = context.obtainStyledAttributes(attrs, R.styleable.TextureView)
        for (i in 0 until array.indexCount) {
            val attr = array.getIndex(i)
            when (attr) {
                R.styleable.TextureView_frameRate -> frameRateTexture = array.getFloat(attr, 60.0f).toDouble()
                R.styleable.TextureView_renderMode -> mRenderMode = array.getInt(attr, ISurface.RENDERMODE_WHEN_DIRTY)
                R.styleable.TextureView_antiAliasingType ->
                    antiAliasingConfig = ISurface.ANTI_ALIASING_CONFIG
                            .fromInteger(array.getInteger(attr, ISurface.ANTI_ALIASING_CONFIG.NONE.ordinal))
                R.styleable.TextureView_bitsRed -> bitsRed = array.getInteger(attr, 5)
                R.styleable.TextureView_bitsGreen -> bitsGreen = array.getInteger(attr, 6)
                R.styleable.TextureView_bitsBlue -> bitsBlue = array.getInteger(attr, 5)
                R.styleable.TextureView_bitsAlpha -> bitsAlpha = array.getInteger(attr, 0)
                R.styleable.TextureView_bitsDepth -> bitsDepth = array.getInteger(attr, 16)
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
        glThread?.surfaceCreated(width, height)
    }

    /**
     * This method is part of the SurfaceTexture.Callback interface, and is
     * not normally called or subclassed by clients of TextureView.
     */
    private fun surfaceDestroyed() {
        // Surface will be destroyed when we return
        glThread?.surfaceDestroyed()
    }

    /**
     * This method is part of the SurfaceTexture.Callback interface, and is
     * not normally called or subclassed by clients of TextureView.
     */
    private fun surfaceChanged(w: Int, h: Int) {
        glThread?.onWindowResize(w, h)
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
            val renderMode = glThread?.renderMode!!
            glThread = GLThread(thisWeakRef, context)
            if (renderMode != ISurface.RENDERMODE_CONTINUOUSLY) {
                glThread?.renderMode = renderMode
            }
            glThread?.apply { start() }
        }
        detached = false
    }

    override fun onDetachedFromWindow() {
        if (LOG_ATTACH_DETACH) {
            Log.v(TAG, "onDetachedFromWindow")
        }
        rendererDelegate?.renderer?.onRenderSurfaceDestroyed(null)
        glThread?.requestExitAndWait()
        detached = true
        super.onDetachedFromWindow()
    }

    @Suppress("unused")
    protected open fun finalize() {
        // GLThread may still be running if this view was never
        // attached to a window.
        glThread?.requestExitAndWait()
    }

    override fun setFrameRate(rate: Double) {
        frameRateTexture = rate
        rendererDelegate?.renderer?.frameRate = rate
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
        rendererDelegate?.let { renderModeInternal = mRenderMode }
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
            eglContextFactory = DefaultContextFactory(this)
        }
        if (eglWindowSurfaceFactory == null) {
            eglWindowSurfaceFactory = DefaultWindowSurfaceFactory()
        }
        // Create our delegate
        val delegate = TextureView.RendererDelegate(renderer, this)
        // Create the GL thread
        glThread = GLThread(thisWeakRef, context)
                .apply { start() }
        // Render mode cant be set until the GL thread exists
        renderModeInternal = mRenderMode
        // Register the delegate for callbacks
        // Done to make sure we don't publish a reference before its safe.
        rendererDelegate = delegate
        surfaceTextureListener = rendererDelegate
    }

    override fun requestRenderUpdate() {
        glThread?.requestRender()
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
    @Suppress("unused")
    open fun setEGLContextFactory(factory: GLSurfaceView.EGLContextFactory) {
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
    @Suppress("unused")
    open fun setEGLWindowSurfaceFactory(factory: GLSurfaceView.EGLWindowSurfaceFactory) {
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
    open fun setEGLConfigChooser(configChooser: IRajawaliEglConfigChooser) {
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
    @Suppress("unused")
    open fun setEGLConfigChooser(
            redSize: Int,
            greenSize: Int,
            blueSize: Int,
            alphaSize: Int,
            depthSize: Int,
            stencilSize: Int) {
        setEGLConfigChooser(ComponentSizeChooser(
                redSize = redSize,
                greenSize = greenSize,
                blueSize = blueSize,
                alphaSize = alphaSize,
                depthSize = depthSize,
                stencilSize = stencilSize,
                textureView = this
        ))
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
    open fun setEGLContextClientVersion(version: Int) {
        checkRenderThreadState()
        eglContextClientVersion = version
    }

    /**
     * Inform the view that the activity is paused. The owner of this view must
     * call this method when the activity is paused. Calling this method will
     * pause the rendering thread.
     * Must not be called before a renderer has been set.
     */
    open fun onPause() {
        rendererDelegate?.renderer?.onPause()
        glThread?.onPause()
    }

    /**
     * Inform the view that the activity is resumed. The owner of this view must
     * call this method when the activity is resumed. Calling this method will
     * recreate the OpenGL display and resume the rendering
     * thread.
     * Must not be called before a renderer has been set.
     */
    open fun onResume() {
        rendererDelegate?.renderer?.onResume()
        glThread?.onResume()
    }

    /**
     * Queue a runnable to be run on the GL rendering thread. This can be used
     * to communicate with the Renderer on the rendering thread.
     * Must not be called before a renderer has been set.
     *
     * @param runnable the runnable to be run on the GL rendering thread.
     */
    @Suppress("unused")
    open fun queueEvent(runnable: Runnable) {
        glThread?.queueEvent(runnable)
    }

    protected class RendererDelegate(
            internal val renderer: ISurfaceRenderer,
            internal val rajawaliTextureView: TextureView
    ) : SurfaceTextureListener {

        init {
            renderer.frameRate = when {
                rajawaliTextureView.mRenderMode == ISurface.RENDERMODE_WHEN_DIRTY -> rajawaliTextureView.frameRateTexture
                else -> 0.0
            }
            renderer.setAntiAliasingMode(rajawaliTextureView.antiAliasingConfig)
            renderer.setRenderSurface(rajawaliTextureView)
            rajawaliTextureView.surfaceTextureListener = this
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) =
                rajawaliTextureView.surfaceCreated(width, height)

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) =
                rajawaliTextureView.surfaceChanged(width, height)

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            surface.release()
            rajawaliTextureView.surfaceDestroyed()
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
    }

    private class DefaultContextFactory(
            private val textureView: TextureView
    ) : GLSurfaceView.EGLContextFactory {

        override fun createContext(egl: EGL10, display: EGLDisplay, config: EGLConfig): EGLContext {
            return intArrayOf(
                    EGL_CONTEXT_CLIENT_VERSION,
                    textureView.eglContextClientVersion,
                    EGL10.EGL_NONE
            ).let { attributeList ->
                egl.eglCreateContext(
                        display,
                        config,
                        EGL10.EGL_NO_CONTEXT,
                        if (textureView.eglContextClientVersion != 0) attributeList else null
                )
            }
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

        companion object {
            private const val EGL_CONTEXT_CLIENT_VERSION = 0x3098
        }
    }

    private class DefaultWindowSurfaceFactory : GLSurfaceView.EGLWindowSurfaceFactory {

        override fun createWindowSurface(
                egl: EGL10,
                display: EGLDisplay,
                config: EGLConfig,
                nativeWindow: Any
        ): EGLSurface? {
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

    abstract class BaseConfigChooser(
            protected val textureView: TextureView,
            requestedConfigSpec: IntArray
    ) : IRajawaliEglConfigChooser {

        private var configSpec: IntArray = filterConfigSpec(requestedConfigSpec)

        override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig {
            val numConfig = IntArray(1)
            if (!egl.eglChooseConfig(display, configSpec, null, 0, numConfig)) {
                throw IllegalArgumentException("eglChooseConfig failed")
            }

            val numConfigs = numConfig[0]

            if (numConfigs <= 0) {
                throw IllegalArgumentException("No configs match configSpec")
            }

            val configs = arrayOfNulls<EGLConfig>(numConfigs)
            if (!egl.eglChooseConfig(display, configSpec, configs, numConfigs, numConfig)) {
                throw IllegalArgumentException("eglChooseConfig#2 failed")
            }
            return chooseConfig(egl, display, configs.requireNoNulls())
                    ?: throw IllegalArgumentException("No config chosen")
        }

        internal abstract fun chooseConfig(egl: EGL10, display: EGLDisplay, configs: Array<EGLConfig>): EGLConfig?

        private fun filterConfigSpec(configSpec: IntArray): IntArray {
            if (textureView.eglContextClientVersion != 2 && textureView.eglContextClientVersion != 3) {
                return configSpec
            }
            /* We know none of the subclasses define EGL_RENDERABLE_TYPE.
             * And we know the configSpec is well formed.
             */
            val len = configSpec.size
            val newConfigSpec = IntArray(len + 2)
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1)
            newConfigSpec[len - 1] = EGL10.EGL_RENDERABLE_TYPE
            newConfigSpec[len] = when {
                textureView.eglContextClientVersion == 2 -> RajawaliEGLConfigChooser.EGL_OPENGL_ES2_BIT
                else -> RajawaliEGLConfigChooser.EGL_OPENGL_ES3_BIT_KHR
            }
            newConfigSpec[len + 1] = EGL10.EGL_NONE
            return newConfigSpec
        }
    }

    /**
     * Choose a configuration with exactly the specified r,g,b,a sizes,
     * and at least the specified depth and stencil sizes.
     */
    open class ComponentSizeChooser(// Subclasses can adjust these values:
            protected var redSize: Int,
            protected var greenSize: Int,
            protected var blueSize: Int,
            protected var alphaSize: Int,
            protected var depthSize: Int,
            protected var stencilSize: Int,
            textureView: TextureView
    ) : BaseConfigChooser(
            textureView,
            intArrayOf(
                    EGL10.EGL_RED_SIZE, redSize,
                    EGL10.EGL_GREEN_SIZE, greenSize,
                    EGL10.EGL_BLUE_SIZE, blueSize,
                    EGL10.EGL_ALPHA_SIZE, alphaSize,
                    EGL10.EGL_DEPTH_SIZE, depthSize,
                    EGL10.EGL_STENCIL_SIZE, stencilSize,
                    EGL10.EGL_NONE
            )
    ) {
        override fun chooseConfigWithReason(egl: EGL10, display: EGLDisplay): ResultConfigChooser {
            return ResultConfigChooser(chooseConfig(egl, display), null)
        }

        private val valueArray = IntArray(1)

        public override fun chooseConfig(egl: EGL10, display: EGLDisplay, configs: Array<EGLConfig>): EGLConfig? {
            for (config in configs) {
                val d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0)
                val s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0)
                if (d >= depthSize && s >= stencilSize) {
                    val r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0)
                    val g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0)
                    val b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0)
                    val a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0)
                    if (r == redSize && g == greenSize && b == blueSize && a == alphaSize) {
                        return config
                    }
                }
            }
            return null
        }

        private fun findConfigAttrib(
                egl: EGL10,
                display: EGLDisplay,
                config: EGLConfig,
                attribute: Int,
                defaultValue: Int
        ): Int =
                if (egl.eglGetConfigAttrib(display, config, attribute, valueArray)) valueArray[0]
                else defaultValue
    }

    /**
     * An EGL helper class.
     */
    private class EglHelper(private val rajawaliTextureViewWeakRef: WeakReference<TextureView>) {
        internal var egl: EGL10? = null
        internal var eglDisplay: EGLDisplay? = null
        internal var eglSurface: EGLSurface? = null
        internal var mEglConfig: EGLConfig? = null
        internal var eglContext: EGLContext? = null

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
            egl = EGLContext.getEGL() as EGL10

            /*
             * Get to the default display.
             */
            eglDisplay = egl?.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

            if (eglDisplay === EGL10.EGL_NO_DISPLAY) {
                throwEglException("eglGetDisplay failed " + EGL10.EGL_NO_DISPLAY)
                return
            }

            /*
             * We can now initialize EGL for that display
             */
            val version = IntArray(2)
            if (!egl!!.eglInitialize(eglDisplay, version)) {
                throwEglException("eglInitialize failed")
                return
            }
            rajawaliTextureViewWeakRef.get()?.let {

                val result = it.eglConfigChooser?.chooseConfigWithReason(egl!!, eglDisplay!!)
                result?.configGL?.let { config ->
                    mEglConfig = config

                    /*
                    * Create an EGL context. We want to do this as rarely as we can, because an
                    * EGL context is a somewhat heavy object.
                    */
                    eglContext = it.eglContextFactory?.createContext(egl, eglDisplay, mEglConfig)
                } ?: run {
                    result?.error?.let { error -> throwEglException(error) }
                }
            } ?: run {
                mEglConfig = null
                eglContext = null
            }
            if (eglContext === EGL10.EGL_NO_CONTEXT) {
                eglContext = null
                throwEglException("createContext " + EGL10.EGL_NO_CONTEXT)
            }
            if (LOG_EGL) {
                Log.w("EglHelper", "createContext " + eglContext + " tid=" + Thread.currentThread().id)
            }

            eglSurface = null
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
            if (egl == null) {
                throw RuntimeException("egl not initialized")
            }
            if (eglDisplay == null) {
                throw RuntimeException("eglDisplay not initialized")
            }
            if (mEglConfig == null) {
                return false
            }

            /*
             *  The window size has changed, so we need to create a new
             *  surface.
             */
            destroySurfaceImp()

            /*
             * Create an EGL surface we can render into.
             */
            eglSurface = rajawaliTextureViewWeakRef.get()
                    ?.let { textureView ->
                        textureView.eglWindowSurfaceFactory
                                ?.createWindowSurface(egl, eglDisplay, mEglConfig, textureView.surfaceTexture)
                    }

            if (eglSurface == null || eglSurface === EGL10.EGL_NO_SURFACE) {
                if (egl?.eglGetError() == EGL10.EGL_BAD_NATIVE_WINDOW) {
                    Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.")
                }
                return false
            }

            /*
             * Before we can issue GL commands, we need to make sure
             * the context is current and bound to a surface.
             */
            if (!egl!!.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                /*
                 * Could not make the context current, probably because the underlying
                 * SurfaceView surface has been destroyed.
                 */
                logEglErrorAsWarning("EGLHelper", "eglMakeCurrent", egl!!.eglGetError())
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
            return eglContext!!.gl
        }

        /**
         * Display the current render surface.
         *
         * @return the EGL error code from eglSwapBuffers.
         */
        fun swap(): Int {
            return if (!egl!!.eglSwapBuffers(eglDisplay, eglSurface)) {
                egl!!.eglGetError()
            } else EGL10.EGL_SUCCESS
        }

        fun destroySurface() {
            if (LOG_EGL) {
                Log.w("EglHelper", "destroySurface()  tid=" + Thread.currentThread().id)
            }
            destroySurfaceImp()
        }

        private fun destroySurfaceImp() {
            if (eglSurface != null && eglSurface !== EGL10.EGL_NO_SURFACE) {
                egl?.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_CONTEXT)
                rajawaliTextureViewWeakRef.get()?.let {
                    it.eglWindowSurfaceFactory?.destroySurface(egl, eglDisplay, eglSurface)
                }
                eglSurface = null
            }
        }

        fun finish() {
            if (LOG_EGL) {
                Log.w("EglHelper", "finish() tid=" + Thread.currentThread().id)
            }
            eglContext?.let {
                rajawaliTextureViewWeakRef.get()
                        ?.let { textureView ->
                            textureView.eglContextFactory
                                    ?.destroyContext(egl, eglDisplay, eglContext)
                        }
                eglContext = null
            }
            eglContext?.let {
                egl?.eglTerminate(eglDisplay)
                eglDisplay = null
            }
        }

        private fun throwEglException(function: String) {
            throwEglException(function, egl!!.eglGetError())
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
     * glThreadManager object. This avoids multiple-lock ordering issues.
     */
    internal class GLThread(
            /**
             * Set once at thread construction time, nulled out when the parent view is garbage
             * called. This weak reference allows the TextureView to be garbage collected while
             * the RajawaliGLThread is still alive.
             */
            private val rajawaliTextureViewWeakRef: WeakReference<TextureView>, val context: Context) : Thread() {

        // Once the thread is started, all accesses to the following member
        // variables are protected by the glThreadManager monitor
        private var shouldExit: Boolean = false
        var exited: Boolean = false
        private var requestPaused: Boolean = false
        private var paused: Boolean = false
        private var hasSurface: Boolean = false
        private var surfaceIsBad: Boolean = false
        private var waitingForSurface: Boolean = false
        private var haveEglContext: Boolean = false
        private var haveEglSurface: Boolean = false
        private var finishedCreatingEglSurface: Boolean = false
        private var shouldReleaseEglContext: Boolean = false
        private var width: Int = 0
        private var height: Int = 0
        private var renderMode1: Int = 0
        private var requestRender: Boolean = false
        private var renderComplete: Boolean = false
        private val eventQueue = ArrayList<Runnable>()
        private var sizeChanged = true

        // End of member variables protected by the glThreadManager monitor.

        private var eglHelper: EglHelper? = null

        var renderMode: Int
            get() = synchronized(glThreadManager) {
                return renderMode1
            }
            set(renderMode) {
                if (!(ISurface.RENDERMODE_WHEN_DIRTY <= renderMode && renderMode <= ISurface.RENDERMODE_CONTINUOUSLY)) {
                    throw IllegalArgumentException("renderMode")
                }
                synchronized(glThreadManager) {
                    renderMode1 = renderMode
                    glThreadManager.notifyAll()
                }
            }

        init {
            width = 0
            height = 0
            requestRender = true
            renderMode1 = ISurface.RENDERMODE_CONTINUOUSLY
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
            } catch (e: IllegalStateException) {
                Log.e("RajawaliGLThread", e.message ?: "")
                showToast(e.message)
            } catch (e: Exception) {
                Log.e("RajawaliGLThread", e.message ?: "")
            } finally {
                glThreadManager.threadExiting(this)
            }
        }

        fun showToast(text: String?) {
            Handler(Looper.getMainLooper()).post { Toast.makeText(context, text, Toast.LENGTH_LONG).show() }
        }

        /*
         * This private method should only be called inside a
         * synchronized(glThreadManager) block.
         */
        private fun stopEglSurfaceLocked() {
            if (haveEglSurface) {
                haveEglSurface = false
                eglHelper?.destroySurface()
            }
        }

        /*
         * This private method should only be called inside a
         * synchronized(glThreadManager) block.
         */
        private fun stopEglContextLocked() {
            if (haveEglContext) {
                eglHelper?.finish()
                haveEglContext = false
                glThreadManager.releaseEglContextLocked(this)
            }
        }

        @Throws(InterruptedException::class)
        private fun guardedRun() {
            eglHelper = EglHelper(rajawaliTextureViewWeakRef)
            haveEglContext = false
            haveEglSurface = false
            try {
                var gl: GL10? = null
                var createEglContext = false
                var createEglSurface = false
                var createGlInterface = false
                var lostEglContext = false
                var sizeChangedLocal = false
                var wantRenderNotification = false
                var doRenderNotification = false
                var askedToReleaseEglContext = false
                var w = 0
                var h = 0
                var event: Runnable? = null

                while (true) {
                    synchronized(glThreadManager) {
                        while (true) {
                            if (shouldExit) {
                                return
                            }

                            if (!eventQueue.isEmpty()) {
                                event = eventQueue.removeAt(0)
                                break
                            }

                            // Update the pause state.
                            var pausing = false
                            if (paused != requestPaused) {
                                pausing = requestPaused
                                paused = requestPaused
                                glThreadManager.notifyAll()
                                if (LOG_PAUSE_RESUME) {
                                    Log.i("RajawaliGLThread", "paused is now $paused tid=$id")
                                }
                            }

                            // Do we need to give up the EGL context?
                            if (shouldReleaseEglContext) {
                                if (LOG_SURFACE) {
                                    Log.i("RajawaliGLThread", "releasing EGL context because asked to tid=$id")
                                }
                                stopEglSurfaceLocked()
                                stopEglContextLocked()
                                shouldReleaseEglContext = false
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
                                rajawaliTextureViewWeakRef.get()?.let {
                                    val preserveEglContextOnPause = it.preserveEGLContextOnPause
                                    if (!preserveEglContextOnPause || glThreadManager.shouldReleaseEGLContextWhenPausing()) {
                                        stopEglContextLocked()
                                        if (LOG_SURFACE) {
                                            Log.i("RajawaliGLThread", "releasing EGL context because paused tid=$id")
                                        }
                                    }
                                }

                            }

                            // When pausing, optionally terminate EGL:
                            if (pausing) {
                                if (glThreadManager.shouldTerminateEGLWhenPausing()) {
                                    eglHelper?.finish()
                                    if (LOG_SURFACE) {
                                        Log.i("RajawaliGLThread", "terminating EGL because paused tid=$id")
                                    }
                                }
                            }

                            // Have we lost the SurfaceView surface?
                            if (!hasSurface && !waitingForSurface) {
                                if (LOG_SURFACE) {
                                    Log.i("RajawaliGLThread", "noticed surfaceView surface lost tid=$id")
                                }
                                if (haveEglSurface) {
                                    stopEglSurfaceLocked()
                                }
                                waitingForSurface = true
                                surfaceIsBad = false
                                glThreadManager.notifyAll()
                            }

                            // Have we acquired the surface view surface?
                            if (hasSurface && waitingForSurface) {
                                if (LOG_SURFACE) {
                                    Log.i("RajawaliGLThread", "noticed surfaceView surface acquired tid=$id")
                                }
                                waitingForSurface = false
                                glThreadManager.notifyAll()
                            }

                            if (doRenderNotification) {
                                if (LOG_SURFACE) {
                                    Log.i("RajawaliGLThread", "sending render notification tid=$id")
                                }
                                wantRenderNotification = false
                                doRenderNotification = false
                                renderComplete = true
                                glThreadManager.notifyAll()
                            }

                            // Ready to draw?
                            if (readyToDraw()) {
                                // If we don't have an EGL context, try to acquire one.
                                if (!haveEglContext) {
                                    if (askedToReleaseEglContext) {
                                        askedToReleaseEglContext = false
                                    } else if (glThreadManager.tryAcquireEglContextLocked(this)) {
                                        try {
                                            eglHelper?.start()
                                        } catch (t: RuntimeException) {
                                            glThreadManager.releaseEglContextLocked(this)
                                            throw t
                                        }

                                        haveEglContext = true
                                        createEglContext = true

                                        glThreadManager.notifyAll()
                                    }
                                }

                                if (haveEglContext && !haveEglSurface) {
                                    haveEglSurface = true
                                    createEglSurface = true
                                    createGlInterface = true
                                    sizeChangedLocal = true
                                }

                                if (haveEglSurface) {
                                    if (sizeChanged) {
                                        sizeChangedLocal = true
                                        w = width
                                        h = height
                                        wantRenderNotification = true
                                        if (LOG_SURFACE) {
                                            Log.i("RajawaliGLThread", "noticing that we want render notification tid=$id")
                                        }

                                        // Destroy and recreate the EGL surface.
                                        createEglSurface = true

                                        sizeChanged = false
                                    }
                                    requestRender = false
                                    glThreadManager.notifyAll()
                                    break
                                }
                            }

                            // By design, this is the only place in a RajawaliGLThread thread where we wait().
                            if (LOG_THREADS) {
                                Log.i("RajawaliGLThread", "waiting tid=" + id
                                        + " haveEglContext: " + haveEglContext
                                        + " haveEglSurface: " + haveEglSurface
                                        + " finishedCreatingEglSurface: " + finishedCreatingEglSurface
                                        + " paused: " + paused
                                        + " hasSurface: " + hasSurface
                                        + " surfaceIsBad: " + surfaceIsBad
                                        + " waitingForSurface: " + waitingForSurface
                                        + " width: " + width
                                        + " height: " + height
                                        + " requestRender: " + requestRender
                                        + " renderMode1: " + renderMode1)
                            }
                            glThreadManager.wait()
                        }
                    } // end of synchronized(glThreadManager)

                    if (event != null) {
                        event?.run()
                        event = null
                        continue
                    }

                    if (createEglSurface) {
                        if (LOG_SURFACE) {
                            Log.w("RajawaliGLThread", "egl createSurface")
                        }
                        if (eglHelper!!.createSurface()) {
                            synchronized(glThreadManager) {
                                finishedCreatingEglSurface = true
                                glThreadManager.notifyAll()
                            }
                        } else {
                            synchronized(glThreadManager) {
                                finishedCreatingEglSurface = true
                                surfaceIsBad = true
                                glThreadManager.notifyAll()
                            }
                            continue
                        }
                        createEglSurface = false
                    }

                    if (createGlInterface) {
                        gl = eglHelper?.createGL() as GL10

                        glThreadManager.checkGLDriver(gl)
                        createGlInterface = false
                    }

                    if (createEglContext) {
                        if (LOG_RENDERER) {
                            Log.w("RajawaliGLThread", "onSurfaceCreated")
                        }
                        val view = rajawaliTextureViewWeakRef.get()
                        view?.rendererDelegate?.renderer?.onRenderSurfaceCreated(eglHelper?.mEglConfig, gl, -1, -1)
                        createEglContext = false
                    }

                    if (sizeChangedLocal) {
                        if (LOG_RENDERER) {
                            Log.w("RajawaliGLThread", "onSurfaceChanged($w, $h)")
                        }
                        val view = rajawaliTextureViewWeakRef.get()
                        view?.rendererDelegate?.renderer?.onRenderSurfaceSizeChanged(gl, w, h)
                        sizeChangedLocal = false
                    }

                    if (LOG_RENDERER_DRAW_FRAME) {
                        Log.w("RajawaliGLThread", "onDrawFrame tid=$id")
                    }
                    run {
                        val view = rajawaliTextureViewWeakRef.get()
                        view?.rendererDelegate?.renderer?.onRenderFrame(gl)
                    }
                    val swapError = eglHelper!!.swap()
                    when (swapError) {
                        EGL10.EGL_SUCCESS -> Unit
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

                            synchronized(glThreadManager) {
                                surfaceIsBad = true
                                glThreadManager.notifyAll()
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
                synchronized(glThreadManager) {
                    stopEglSurfaceLocked()
                    stopEglContextLocked()
                }
            }
        }

        private fun ableToDraw() = haveEglContext && haveEglSurface && readyToDraw()

        private fun readyToDraw() = !paused && hasSurface && !surfaceIsBad
                && width > 0 && height > 0
                && (requestRender || renderMode1 == ISurface.RENDERMODE_CONTINUOUSLY)

        fun requestRender() {
            synchronized(glThreadManager) {
                requestRender = true
                glThreadManager.notifyAll()
            }
        }

        fun surfaceCreated(w: Int, h: Int) {
            synchronized(glThreadManager) {
                if (LOG_THREADS) {
                    Log.i("RajawaliGLThread", "surfaceCreated tid=$id")
                }
                hasSurface = true
                width = w
                height = h
                finishedCreatingEglSurface = false
                glThreadManager.notifyAll()
                while (waitingForSurface
                        && !finishedCreatingEglSurface
                        && !exited) {
                    try {
                        glThreadManager.wait()
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }

                }
            }
        }

        fun surfaceDestroyed() {
            synchronized(glThreadManager) {
                if (LOG_THREADS) {
                    Log.i("RajawaliGLThread", "surfaceDestroyed tid=$id")
                }
                hasSurface = false
                glThreadManager.notifyAll()
                while (!waitingForSurface && !exited) {
                    try {
                        glThreadManager.wait()
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }

                }
            }
        }

        fun onPause() {
            synchronized(glThreadManager) {
                if (LOG_PAUSE_RESUME) {
                    Log.i("RajawaliGLThread", "onPause tid=$id")
                }
                requestPaused = true
                glThreadManager.notifyAll()
                while (!exited && !paused) {
                    if (LOG_PAUSE_RESUME) {
                        Log.i("Main thread", "onPause waiting for paused.")
                    }
                    try {
                        glThreadManager.wait()
                    } catch (ex: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }

                }
            }
        }

        fun onResume() {
            synchronized(glThreadManager) {
                if (LOG_PAUSE_RESUME) {
                    Log.i("RajawaliGLThread", "onResume tid=$id")
                }
                requestPaused = false
                requestRender = true
                renderComplete = false
                glThreadManager.notifyAll()
                while (!exited && paused && !renderComplete) {
                    if (LOG_PAUSE_RESUME) {
                        Log.i("Main thread", "onResume waiting for !paused.")
                    }
                    try {
                        glThreadManager.wait()
                    } catch (ex: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }

                }
            }
        }

        fun onWindowResize(w: Int, h: Int) {
            synchronized(glThreadManager) {
                width = w
                height = h
                sizeChanged = true
                requestRender = true
                renderComplete = false
                glThreadManager.notifyAll()

                // Wait for thread to react to resize and render a frame
                while (!exited && !paused && !renderComplete
                        && ableToDraw()) {
                    if (LOG_SURFACE) {
                        Log.i("Main thread", "onWindowResize waiting for render complete from tid=$id")
                    }
                    try {
                        glThreadManager.wait()
                    } catch (ex: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }

                }
            }
        }

        fun requestExitAndWait() {
            // don't call this from RajawaliGLThread thread or it is a guaranteed
            // deadlock!
            synchronized(glThreadManager) {
                shouldExit = true
                glThreadManager.notifyAll()
                while (!exited) {
                    try {
                        glThreadManager.wait()
                    } catch (ex: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }

                }
            }
        }

        fun requestReleaseEglContextLocked() {
            shouldReleaseEglContext = true
            glThreadManager.notifyAll()
        }

        /**
         * Queue an "event" to be run on the GL rendering thread.
         *
         * @param runnable the runnable to be run on the GL rendering thread.
         */
        fun queueEvent(runnable: Runnable?) {
            if (runnable == null) {
                throw IllegalArgumentException("runnable must not be null")
            }
            synchronized(glThreadManager) {
                eventQueue.add(runnable)
                glThreadManager.notifyAll()
            }
        }
    }

    // Every class in Kotlin inherits from Any, but Any doesn't declare wait(), notify() and notifyAll(),
    // meaning that these methods can't be called on a Kotlin class.
    // It's hacky to extend from Object and concurrency mechanisms can done in a better way, eg Coroutines
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private class GLThreadManager : Object() {

        private var glesVersionCheckComplete: Boolean = false
        private var glesVersion: Int = 0
        private var glesDriverCheckComplete: Boolean = false
        private var multipleGLESContextsAllowed: Boolean = false
        private var limitedGLESContexts: Boolean = false
        private var eglOwner: GLThread? = null

        @Synchronized
        fun threadExiting(thread: GLThread) {
            if (LOG_THREADS) {
                Log.i("RajawaliGLThread", "exiting tid=" + thread.id)
            }
            thread.exited = true
            if (eglOwner === thread) {
                eglOwner = null
            }
            notifyAll()
        }

        /*
         * Tries once to acquire the right to use an EGL
         * context. Does not block. Requires that we are already
         * in the glThreadManager monitor when this is called.
         *
         * @return true if the right to use an EGL context was acquired.
         */
        fun tryAcquireEglContextLocked(thread: GLThread): Boolean {
            if (eglOwner === thread || eglOwner == null) {
                eglOwner = thread
                notifyAll()
                return true
            }
            checkGLESVersion()
            if (multipleGLESContextsAllowed) {
                return true
            }
            // Notify the owning thread that it should release the context.
            // TODO: implement a fairness policy. Currently
            // if the owning thread is drawing continuously it will just
            // reacquire the EGL context.
            eglOwner?.requestReleaseEglContextLocked()
            return false
        }

        /*
         * Releases the EGL context. Requires that we are already in the
         * glThreadManager monitor when this is called.
         */
        fun releaseEglContextLocked(thread: GLThread) {
            if (eglOwner === thread) {
                eglOwner = null
            }
            notifyAll()
        }

        @Synchronized
        fun shouldReleaseEGLContextWhenPausing(): Boolean {
            // Release the EGL context when pausing even if
            // the hardware supports multiple EGL contexts.
            // Otherwise the device could run out of EGL contexts.
            return limitedGLESContexts
        }

        @Synchronized
        fun shouldTerminateEGLWhenPausing(): Boolean {
            checkGLESVersion()
            return !multipleGLESContextsAllowed
        }

        private fun checkGLESVersion() {
            if (!glesVersionCheckComplete) {
                glesVersion = Capabilities.getGLESMajorVersion()
                if (glesVersion >= kGLES_20) {
                    multipleGLESContextsAllowed = true
                }
                if (LOG_SURFACE) {
                    Log.w(TAG, "checkGLESVersion glesVersion =" +
                            " " + glesVersion + " multipleGLESContextsAllowed = " + multipleGLESContextsAllowed)
                }
                glesVersionCheckComplete = true
            }
        }

        @Synchronized
        fun checkGLDriver(gl: GL10?) {
            if (!glesDriverCheckComplete) {
                checkGLESVersion()
                val renderer = gl?.glGetString(GL10.GL_RENDERER)
                if (glesVersion < kGLES_20) {
                    multipleGLESContextsAllowed = !renderer?.startsWith(kMSM7K_RENDERER_PREFIX)!!
                    notifyAll()
                }
                limitedGLESContexts = !multipleGLESContextsAllowed
                if (LOG_SURFACE) {
                    Log.w(TAG, "checkGLDriver renderer = \"" + renderer + "\" multipleContextsAllowed = "
                            + multipleGLESContextsAllowed
                            + " limitedGLESContexts = " + limitedGLESContexts)
                }
                glesDriverCheckComplete = true
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

        private val glThreadManager = GLThreadManager()

    }

}
