/**
 * Copyright 2013 Dennis Ippel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d.wallpaper;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import c.org.rajawali3d.core.RenderControl;
import c.org.rajawali3d.core.RenderControlClient;
import org.rajawali3d.renderer.ISurfaceRenderer;

import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.surface.SurfaceSize;
import c.org.rajawali3d.surface.gles.GlesSurfaceView;
import c.org.rajawali3d.surface.gles.GlesSurfaceAntiAliasing;

import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

public abstract class Wallpaper extends WallpaperService {

    protected class WallpaperEngine extends Engine implements RenderControlClient {

        protected Context mContext;
        protected ISurfaceRenderer mRenderer;
        protected WallpaperSurfaceView mSurfaceView;
        protected GlesSurfaceAntiAliasing mAntiAliasing;
        protected float mDefaultPreviewOffsetX;

        class WallpaperSurfaceView extends GlesSurfaceView {

            WallpaperSurfaceView(Context context) {
                super(context);
            }

            @Override
            public SurfaceHolder getHolder() {
                return getSurfaceHolder();
            }

            public void onDestroy() {
                super.onDetachedFromWindow();
            }
            /**
             * Sets the WallpaperCallback.
             *
             * @param callback the {@link WallpaperCallback} instance to set, or null to unset
             */
            public void setWallpaperCallback(@Nullable WallpaperCallback callback) {
                // TODO
            };
        }

        public WallpaperEngine(Context context, ISurfaceRenderer renderer) {
            this(context, renderer, GlesSurfaceAntiAliasing.NONE);
        }

        // TODO need a Scene instead of a renderer?
        public WallpaperEngine(Context context, ISurfaceRenderer renderer,
                               GlesSurfaceAntiAliasing antiAliasing) {
            mContext = context;
            mRenderer = renderer;
            mAntiAliasing = antiAliasing;
            mDefaultPreviewOffsetX = 0.5f;
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep,
                                     int xPixelOffset, int yPixelOffset) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            if (mRenderer != null) {
                if (isPreview() && enableDefaultXOffsetInPreview())
                    xOffset = mDefaultPreviewOffsetX;

                mRenderer.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            }
        }

        public boolean enableDefaultXOffsetInPreview() {
            return true;
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if (mRenderer != null)
                mRenderer.onTouchEvent(event);
        }

        @Override
        @TargetApi(15)
        public void setOffsetNotificationsEnabled(boolean enabled) {
            if (Build.VERSION.SDK_INT >= 15) {
                super.setOffsetNotificationsEnabled(enabled);
            }
        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            mSurfaceView = new WallpaperSurfaceView(mContext);
            mSurfaceView.setEGLContextClientVersion(Capabilities.getGLESMajorVersion());
            mSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);
            mSurfaceView.setSurfaceAntiAliasing(mAntiAliasing);
            mSurfaceView.configure(this);
            //mSurfaceView.setSurfaceRenderer(mRenderer);  ??
            setTouchEventsEnabled(true);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
        }

        @Override
        public void onDestroy() {
            setTouchEventsEnabled(false);
            mRenderer.onRenderSurfaceDestroyed(null);
            mRenderer = null;
            mSurfaceView.onDestroy();
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                mSurfaceView.onResume();
            } else {
                mSurfaceView.onPause();
            }
        }

        @Override
        public void onRenderControlAvailable(RenderControl renderControl, SurfaceSize surfaceSize) {
            // TODO
        }

        @Override
        public void onSurfaceSizeChanged(SurfaceSize surfaceSize) {
            // TODO ?
        }


    }
}
