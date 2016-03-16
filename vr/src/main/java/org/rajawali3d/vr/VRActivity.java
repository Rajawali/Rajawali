/**
 * Copyright 2015 Dennis Ippel
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
package org.rajawali3d.vr;

import android.app.ActionBar;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import org.rajawali3d.vr.renderer.VRRenderer;
import org.rajawali3d.vr.surface.VRSurfaceView;

/**
 * @author dennis.ippel
 */
public class VRActivity extends CardboardActivity {

    private CardboardView mSurfaceView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        mSurfaceView = new VRSurfaceView(this);

        addContentView(mSurfaceView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT));

		int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
							| View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE;
		}
        mSurfaceView.setSystemUiVisibility(uiFlags);

        setCardboardView(mSurfaceView);
	}

	protected void setRenderer(VRRenderer renderer) {
	    mSurfaceView.setRenderer(renderer);
	}

    public CardboardView getSurfaceView() {
        return mSurfaceView;
    }

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
}

