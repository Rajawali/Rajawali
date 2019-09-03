package org.rajawali3d.examples.examples.general;

import android.content.Context;
import androidx.annotation.Nullable;
import android.view.animation.BounceInterpolator;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.cameras.OrthographicCamera;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Plane;

import java.util.Random;

public class OrthographicFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new OrthographicRenderer(getActivity(), this);
	}

	private final class OrthographicRenderer extends AExampleRenderer {

		public OrthographicRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			OrthographicCamera orthoCam = new OrthographicCamera();
            orthoCam.setLookAt(0, 0, 0);
            orthoCam.enableLookAt();
            orthoCam.setY(1.5);
			int[][] grid;

			getCurrentScene().switchCamera(orthoCam);

			DirectionalLight spotLight = new DirectionalLight(1f, -.1f, -.5f);
			spotLight.setPower(2);
			getCurrentScene().addLight(spotLight);

			grid = new int[10][];
			for (int i = 0; i < 10; i++)
				grid[i] = new int[10];

			Material material = new Material();
			try {
				material.addTexture(new Texture("checkerboard", R.drawable.checkerboard));
				material.setColorInfluence(0);
			} catch (ATexture.TextureException e) {
				e.printStackTrace();
			}

			Object3D group = new Object3D();
			group.setRotX(-45);
			group.setRotY(-45);
			group.setY(-.8f);

			Object3D innerGroup = new Object3D();
			group.addChild(innerGroup);

			Plane plane = new Plane(Vector3.Axis.Y);
			plane.setMaterial(material);
			plane.setDoubleSided(true);
			plane.setColor(0xff0000ff);
			innerGroup.addChild(plane);

			Material cubeMaterial = new Material();
			cubeMaterial.enableLighting(true);
			cubeMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());

			Random random = new Random();

			for (int i = 0; i < 40; i++) {
				Cube cube = new Cube(.1f);
				cube.setMaterial(cubeMaterial);
				cube.setY(7);
				cube.setColor(0x666666 + random.nextInt(0x999999));
				innerGroup.addChild(cube);

				// find grid available grid cell
				boolean foundCell = false;
				int row = 0, column = 0;
				while (!foundCell) {
					int cell = (int) Math.floor(Math.random() * 100);
					row = (int) Math.floor(cell / 10.f);
					column = cell % 10;
					if (grid[row][column] == 0) {
						grid[row][column] = 1;
						foundCell = true;
					}
				}

				Vector3 toPosition = new Vector3(-.45f + (column * .1f), .05f,
						-.45f + (row * .1f));
				Vector3 fromPosition = new Vector3(toPosition.x, 7,
						toPosition.z);

				TranslateAnimation3D anim = new TranslateAnimation3D(
						fromPosition, toPosition);
				anim.setDurationMilliseconds(4000 + (int) (4000 * Math.random()));
				anim.setInterpolator(new BounceInterpolator());
				anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
				anim.setTransformable3D(cube);
				anim.setDelayMilliseconds((int) (10000 * Math.random()));
				getCurrentScene().registerAnimation(anim);
				anim.play();
			}

			RotateOnAxisAnimation anim = new RotateOnAxisAnimation(Vector3.Axis.Y, 359);
			anim.setDurationMilliseconds(20000);
			anim.setRepeatMode(Animation.RepeatMode.INFINITE);
			anim.setTransformable3D(innerGroup);
			getCurrentScene().registerAnimation(anim);
			anim.play();

			getCurrentScene().addChild(group);
		}

	}

}
