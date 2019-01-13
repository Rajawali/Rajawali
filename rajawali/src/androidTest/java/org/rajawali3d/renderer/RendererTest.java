package org.rajawali3d.renderer;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.view.MotionEvent;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class RendererTest {

    @Test
    public void testInstantiation() {
        new Renderer(InstrumentationRegistry.getTargetContext()) {

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
