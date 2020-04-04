package org.rajawali3d.renderer;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.view.MotionEvent;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class RendererTest {

    @Test
    public void testInstantiation() {
        new Renderer(InstrumentationRegistry.getInstrumentation().getTargetContext()) {

            @Override
            public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            }

            @Override
            public void onTouchEvent(MotionEvent event) {
            }

            @Override
            protected void initScene() {
            }

        };
    }

}
