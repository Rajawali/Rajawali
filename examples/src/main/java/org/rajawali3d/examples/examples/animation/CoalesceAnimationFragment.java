package org.rajawali3d.examples.examples.animation;

import android.content.Context;
import androidx.annotation.Nullable;

import android.graphics.Color;
import android.view.animation.LinearInterpolator;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.CoalesceAnimation3D;
import org.rajawali3d.curves.LogarithmicSpiral3D;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;

public class CoalesceAnimationFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new CoalesceAnimationRenderer(getActivity(), this);
	}

	public class CoalesceAnimationRenderer extends AExampleRenderer {

		CoalesceAnimationRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
            // Create our light and camera
			PointLight mLight = new PointLight();
			mLight.setPosition(-2, 1, -4);
			mLight.setPower(1.5f);
            getCurrentScene().addLight(mLight);
			getCurrentCamera().setPosition(0, 0, -14);
			getCurrentCamera().setLookAt(0, 0, 0);

            // Create the objects in the scene
            Sphere rootObject = new Sphere(0.4f, 8, 8);
            Sphere orbit1 = new Sphere(0.25f, 8, 8);
            Sphere orbit2 = new Sphere(0.25f, 8, 8);
            Sphere orbit3 = new Sphere(0.25f, 8, 8);

            // Create and apply the materials to the objects
            Material material = new Material();
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            rootObject.setMaterial(material);
            rootObject.setColor(Color.GREEN);
            orbit1.setMaterial(material);
            orbit1.setColor(Color.YELLOW);
            orbit2.setMaterial(material);
            orbit2.setColor(Color.CYAN);
            orbit3.setMaterial(material);
            orbit3.setColor(Color.BLUE);

            // Add the objects to the scene
            getCurrentScene().addChild(rootObject);
            getCurrentScene().addChild(orbit1);
            getCurrentScene().addChild(orbit2);
            getCurrentScene().addChild(orbit3);

            // Create the spiral paths
            LogarithmicSpiral3D rootSpiral = new LogarithmicSpiral3D(0.0625, new Vector3(2.0, 0, 0), Vector3.Z, true);
            LogarithmicSpiral3D orbit1Spiral = new LogarithmicSpiral3D(0.03125, new Vector3(2.0, 1.0, 0), new Vector3(0, 0, 1), true);
            LogarithmicSpiral3D orbit2Spiral = new LogarithmicSpiral3D(0.05, new Vector3(2.0, -1.0, 0), Vector3.Z, true);
            LogarithmicSpiral3D orbit3Spiral = new LogarithmicSpiral3D(0.01, new Vector3(1.0, 0, 0), new Vector3(1, 1, 0), true);

            // Create the object path configurations
            CoalesceAnimation3D.CoalesceConfig rootConfig = new CoalesceAnimation3D.CoalesceConfig(rootSpiral, rootObject, Vector3.ZERO, 0.1);
            CoalesceAnimation3D.CoalesceConfig orbit1Config = new CoalesceAnimation3D.CoalesceConfig(orbit1Spiral, orbit1, rootObject, 0.1);
            CoalesceAnimation3D.CoalesceConfig orbit2Config = new CoalesceAnimation3D.CoalesceConfig(orbit2Spiral, orbit2, rootObject, 0.1);
            CoalesceAnimation3D.CoalesceConfig orbit3Config = new CoalesceAnimation3D.CoalesceConfig(orbit3Spiral, orbit3, rootObject, 0.1);

            // Create the animation
            CoalesceAnimation3D coalesceAnimation3D = new CoalesceAnimation3D(rootConfig);
            coalesceAnimation3D.addCoalescingObject(orbit1Config);
            coalesceAnimation3D.addCoalescingObject(orbit2Config);
            coalesceAnimation3D.addCoalescingObject(orbit3Config);

            // Adjust the animation behaviors
            coalesceAnimation3D.setInterpolator(new LinearInterpolator());
            coalesceAnimation3D.setDurationMilliseconds(10000);
            coalesceAnimation3D.setRepeatMode(Animation.RepeatMode.INFINITE);

            // Register and play the animation
			getCurrentScene().registerAnimation(coalesceAnimation3D);
            coalesceAnimation3D.play();
		}
	}
}
