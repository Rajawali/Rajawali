package net.rbgrn.opengl;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import net.rbgrn.opengl.GLWallpaperService.GLEngine;

import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.opengl.GLSurfaceView.EGLContextFactory;
import android.opengl.GLSurfaceView.EGLWindowSurfaceFactory;
import android.opengl.GLSurfaceView.GLWrapper;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;
import android.view.SurfaceHolder;

class GLThread extends Thread {
	// ===========================================================
	// Constants
	// ===========================================================
	
	private final static boolean LOG_THREADS = false;
	public final static int DEBUG_CHECK_GL_ERROR = 1;
	public final static int DEBUG_LOG_GL_CALLS = 2;

	// ===========================================================
	// Fields
	// ===========================================================

	private final GLThreadManager sGLThreadManager = new GLThreadManager();
	private GLThread mEglOwner;

	private final EGLConfigChooser mEGLConfigChooser;
	private final EGLContextFactory mEGLContextFactory;
	private final EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
	private final GLWrapper mGLWrapper;

	public SurfaceHolder mHolder;
	private boolean mSizeChanged = true;

	// Once the thread is started, all accesses to the following member
	// variables are protected by the sGLThreadManager monitor
	public boolean mDone;
	private boolean mPaused;
	private boolean mHasSurface;
	private boolean mWaitingForSurface;
	private boolean mHaveEgl;
	private int mWidth;
	private int mHeight;
	private int mRenderMode;
	private boolean mRequestRender;
	private boolean mEventsWaiting;
	// End of member variables protected by the sGLThreadManager monitor.

	private final Renderer mRenderer;
	private final ArrayList<Runnable> mEventQueue = new ArrayList<Runnable>();
	private EglHelper mEglHelper;
	
	// ===========================================================
	// Constructors
	// ===========================================================

	GLThread(final Renderer renderer, final EGLConfigChooser chooser, final EGLContextFactory contextFactory, final EGLWindowSurfaceFactory surfaceFactory, final GLWrapper wrapper) {
		super();
		this.mDone = false;
		this.mWidth = 0;
		this.mHeight = 0;
		this.mRequestRender = true;
		this.mRenderMode = GLEngine.RENDERMODE_CONTINUOUSLY;
		this.mRenderer = renderer;
		this.mEGLConfigChooser = chooser;
		this.mEGLContextFactory = contextFactory;
		this.mEGLWindowSurfaceFactory = surfaceFactory;
		this.mGLWrapper = wrapper;
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void run() {
		this.setName("GLThread " + this.getId());
		if (LOG_THREADS) {
			Log.i("GLThread", "starting tid=" + this.getId());
		}

		try {
			this.guardedRun();
		} catch (final InterruptedException e) {
			// fall thru and exit normally
		} finally {
			this.sGLThreadManager.threadExiting(this);
		}
	}
	
	// ===========================================================
	// Methods
	// ===========================================================

	/*
	 * This private method should only be called inside a
	 * synchronized(sGLThreadManager) block.
	 */
	private void stopEglLocked() {
		if (this.mHaveEgl) {
			this.mHaveEgl = false;
			this.mEglHelper.destroySurface();
			this.mEglHelper.finish();
			this.sGLThreadManager.releaseEglSurface(this);
		}
	}

	private void guardedRun() throws InterruptedException {
		this.mEglHelper = new EglHelper(this.mEGLConfigChooser, this.mEGLContextFactory, this.mEGLWindowSurfaceFactory, this.mGLWrapper);
		try {
			GL10 gl = null;
			boolean tellRendererSurfaceCreated = true;
			boolean tellRendererSurfaceChanged = true;

			/*
			 * This is our main activity thread's loop, we go until asked to
			 * quit.
			 */
			while (!this.isDone()) {
				/*
				 * Update the asynchronous state (window size)
				 */
				int w = 0;
				int h = 0;
				boolean changed = false;
				boolean needStart = false;
				boolean eventsWaiting = false;

				synchronized (this.sGLThreadManager) {
					while (true) {
						// Manage acquiring and releasing the SurfaceView
						// surface and the EGL surface.
						if (this.mPaused) {
							this.stopEglLocked();
						}
						if (!this.mHasSurface) {
							if (!this.mWaitingForSurface) {
								this.stopEglLocked();
								this.mWaitingForSurface = true;
								this.sGLThreadManager.notifyAll();
							}
						} else {
							if (!this.mHaveEgl) {
								if (this.sGLThreadManager.tryAcquireEglSurface(this)) {
									this.mHaveEgl = true;
									this.mEglHelper.start();
									this.mRequestRender = true;
									needStart = true;
								}
							}
						}

						// Check if we need to wait. If not, update any state
						// that needs to be updated, copy any state that
						// needs to be copied, and use "break" to exit the
						// wait loop.

						if (this.mDone) {
							return;
						}

						if (this.mEventsWaiting) {
							eventsWaiting = true;
							this.mEventsWaiting = false;
							break;
						}

						if ((!this.mPaused) && this.mHasSurface && this.mHaveEgl && (this.mWidth > 0) && (this.mHeight > 0) && (this.mRequestRender || (this.mRenderMode == GLEngine.RENDERMODE_CONTINUOUSLY))) {
							changed = this.mSizeChanged;
							w = this.mWidth;
							h = this.mHeight;
							this.mSizeChanged = false;
							this.mRequestRender = false;
							if (this.mHasSurface && this.mWaitingForSurface) {
								changed = true;
								this.mWaitingForSurface = false;
								this.sGLThreadManager.notifyAll();
							}
							break;
						}

						// By design, this is the only place where we wait().

						if (LOG_THREADS) {
							Log.i("GLThread", "waiting tid=" + this.getId());
						}
						this.sGLThreadManager.wait();
					}
				} // end of synchronized(sGLThreadManager)

				/*
				 * Handle queued events
				 */
				if (eventsWaiting) {
					Runnable r;
					while ((r = this.getEvent()) != null) {
						r.run();
						if (this.isDone()) {
							return;
						}
					}
					// Go back and see if we need to wait to render.
					continue;
				}

				if (needStart) {
					tellRendererSurfaceCreated = true;
					changed = true;
				}
				if (changed) {
					gl = (GL10) this.mEglHelper.createSurface(this.mHolder);
					tellRendererSurfaceChanged = true;
				}
				if (tellRendererSurfaceCreated) {
					this.mRenderer.onSurfaceCreated(gl, this.mEglHelper.mEglConfig);
					tellRendererSurfaceCreated = false;
				}
				if (tellRendererSurfaceChanged) {
					this.mRenderer.onSurfaceChanged(gl, w, h);
					tellRendererSurfaceChanged = false;
				}
				if ((w > 0) && (h > 0)) {
					/* draw a frame here */
					this.mRenderer.onDrawFrame(gl);

					/*
					 * Once we're done with GL, we need to call swapBuffers() to
					 * instruct the system to display the rendered frame
					 */
					this.mEglHelper.swap();
				}
			}
		} finally {
			/*
			 * clean-up everything...
			 */
			synchronized (this.sGLThreadManager) {
				this.stopEglLocked();
			}
		}
	}

	private boolean isDone() {
		synchronized (this.sGLThreadManager) {
			return this.mDone;
		}
	}

	public void setRenderMode(final int renderMode) {
		if (!((GLEngine.RENDERMODE_WHEN_DIRTY <= renderMode) && (renderMode <= GLEngine.RENDERMODE_CONTINUOUSLY))) {
			throw new IllegalArgumentException("renderMode");
		}
		synchronized (this.sGLThreadManager) {
			this.mRenderMode = renderMode;
			if (renderMode == GLEngine.RENDERMODE_CONTINUOUSLY) {
				this.sGLThreadManager.notifyAll();
			}
		}
	}

	public int getRenderMode() {
		synchronized (this.sGLThreadManager) {
			return this.mRenderMode;
		}
	}

	public void requestRender() {
		synchronized (this.sGLThreadManager) {
			this.mRequestRender = true;
			this.sGLThreadManager.notifyAll();
		}
	}

	public void surfaceCreated(final SurfaceHolder holder) {
		this.mHolder = holder;
		synchronized (this.sGLThreadManager) {
			if (LOG_THREADS) {
				Log.i("GLThread", "surfaceCreated tid=" + this.getId());
			}
			this.mHasSurface = true;
			this.sGLThreadManager.notifyAll();
		}
	}

	public void surfaceDestroyed() {
		synchronized (this.sGLThreadManager) {
			if (LOG_THREADS) {
				Log.i("GLThread", "surfaceDestroyed tid=" + this.getId());
			}
			this.mHasSurface = false;
			this.sGLThreadManager.notifyAll();
			while (!this.mWaitingForSurface && this.isAlive() && !this.mDone) {
				try {
					this.sGLThreadManager.wait();
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	public void onPause() {
		synchronized (this.sGLThreadManager) {
			this.mPaused = true;
			this.sGLThreadManager.notifyAll();
		}
	}

	public void onResume() {
		synchronized (this.sGLThreadManager) {
			this.mPaused = false;
			this.mRequestRender = true;
			this.sGLThreadManager.notifyAll();
		}
	}

	public void onWindowResize(final int w, final int h) {
		synchronized (this.sGLThreadManager) {
			this.mWidth = w;
			this.mHeight = h;
			this.mSizeChanged = true;
			this.sGLThreadManager.notifyAll();
		}
	}

	public void requestExitAndWait() {
		// don't call this from GLThread thread or it is a guaranteed
		// deadlock!
		synchronized (this.sGLThreadManager) {
			this.mDone = true;
			this.sGLThreadManager.notifyAll();
		}
		try {
			this.join();
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Queue an "event" to be run on the GL rendering thread.
	 * 
	 * @param r
	 *            the runnable to be run on the GL rendering thread.
	 */
	public void queueEvent(final Runnable r) {
		synchronized (this) {
			this.mEventQueue.add(r);
			synchronized (this.sGLThreadManager) {
				this.mEventsWaiting = true;
				this.sGLThreadManager.notifyAll();
			}
		}
	}

	private Runnable getEvent() {
		synchronized (this) {
			if (this.mEventQueue.size() > 0) {
				return this.mEventQueue.remove(0);
			}

		}
		return null;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private class GLThreadManager {

		public synchronized void threadExiting(final GLThread thread) {
			if (LOG_THREADS) {
				Log.i("GLThread", "exiting tid=" + thread.getId());
			}
			thread.mDone = true;
			if (GLThread.this.mEglOwner == thread) {
				GLThread.this.mEglOwner = null;
			}
			this.notifyAll();
		}

		/*
		 * Tries once to acquire the right to use an EGL surface. Does not
		 * block.
		 * 
		 * @return true if the right to use an EGL surface was acquired.
		 */
		public synchronized boolean tryAcquireEglSurface(final GLThread thread) {
			if (GLThread.this.mEglOwner == thread || GLThread.this.mEglOwner == null) {
				GLThread.this.mEglOwner = thread;
				this.notifyAll();
				return true;
			}
			return false;
		}

		public synchronized void releaseEglSurface(final GLThread thread) {
			if (GLThread.this.mEglOwner == thread) {
				GLThread.this.mEglOwner = null;
			}
			this.notifyAll();
		}
	}
}