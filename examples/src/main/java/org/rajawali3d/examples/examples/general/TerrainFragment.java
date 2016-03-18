package org.rajawali3d.examples.examples.general;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.SplineTranslateAnimation3D;
import org.rajawali3d.cameras.ChaseCamera;
import org.rajawali3d.curves.CatmullRomCurve3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.plugins.FogMaterialPlugin;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.NormalMapTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.MathUtil;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.terrain.SquareTerrain;
import org.rajawali3d.terrain.TerrainGenerator;

public class TerrainFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new TerrainRenderer(getActivity(), this);
	}

	public static final class TerrainRenderer extends AExampleRenderer {

		private SquareTerrain mTerrain;
		private double mLastY = 0;

		public TerrainRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		public void initScene() {
			getCurrentScene().setBackgroundColor(0x999999);

			//
			// -- Use a chase camera that follows and invisible ('empty') object
			//    and add fog for a nice effect.
			//

			ChaseCamera chaseCamera = new ChaseCamera(new Vector3(0, 4, -8));
			chaseCamera.setFarPlane(1000);
			getCurrentScene().replaceAndSwitchCamera(chaseCamera, 0);

			getCurrentScene().setFog(new FogMaterialPlugin.FogParams(FogMaterialPlugin.FogType.LINEAR, 0x999999, 50, 100));

			//
			// -- Load a bitmap that represents the terrain. Its color values will
			//    be used to generate heights.
			//

			Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),
													  R.drawable.terrain);

			try {
				SquareTerrain.Parameters terrainParams = SquareTerrain.createParameters(bmp);
				// -- set terrain scale
				terrainParams.setScale(4f, 54f, 4f);
				// -- the number of plane subdivisions
				terrainParams.setDivisions(128);
				// -- the number of times the textures should be repeated
				terrainParams.setTextureMult(4);
				//
				// -- Terrain colors can be set by manually specifying base, middle and
				//    top colors.
				//
				// --  terrainParams.setBasecolor(Color.argb(255, 0, 0, 0));
				//     terrainParams.setMiddleColor(Color.argb(255, 200, 200, 200));
				//     terrainParams.setUpColor(Color.argb(255, 0, 30, 0));
				//
				// -- However, for this example we'll use a bitmap
				//
				terrainParams.setColorMapBitmap(bmp);
				//
				// -- create the terrain
				//
				mTerrain = TerrainGenerator.createSquareTerrainFromBitmap(terrainParams, true);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//
			// -- The bitmap won't be used anymore, so get rid of it.
			//
			bmp.recycle();

			DirectionalLight light = new DirectionalLight(0.2f, -1f, 0f);
			light.setPower(1f);
			getCurrentScene().addLight(light);

			//
			// -- A normal map material will give the terrain a bit more detail.
			//
			Material material = new Material();
			material.enableLighting(true);
			material.useVertexColors(true);
			material.setDiffuseMethod(new DiffuseMethod.Lambert());
			try {
				Texture groundTexture = new Texture("ground", R.drawable.ground);
				groundTexture.setInfluence(.5f);
				material.addTexture(groundTexture);
				material.addTexture(new NormalMapTexture("groundNormalMap", R.drawable.groundnor));
				material.setColorInfluence(0);
			} catch (ATexture.TextureException e) {
				e.printStackTrace();
			}

			//
			// -- Blend the texture with the vertex colors
			//
			material.setColorInfluence(.5f);

			mTerrain.setMaterial(material);

			getCurrentScene().addChild(mTerrain);

			//
			// -- The empty object that will move along a curve and that
			//    will be follow by the camera
			//
			Object3D empty = new Object3D();
			empty.setVisible(false);

			//
			// -- Tell the camera to chase the empty.
			//
			chaseCamera.setLinkedObject(empty);

			//
			// -- Create a camera path based on the terrain height
			//
			CatmullRomCurve3D cameraPath = createCameraPath();

			SplineTranslateAnimation3D anim = new SplineTranslateAnimation3D(cameraPath);
			anim.setTransformable3D(empty);
			anim.setDurationMilliseconds(60000);
			anim.setRepeatMode(Animation.RepeatMode.INFINITE);
			anim.setOrientToPath(true);
			getCurrentScene().registerAnimation(anim);
			anim.play();
		}

		private CatmullRomCurve3D createCameraPath() {
			CatmullRomCurve3D path = new CatmullRomCurve3D();

			float radius = 200;
			float degreeStep = 15;
			float distanceFromGround = 20;

			for (int i = 0; i < 360; i += degreeStep) {
				double radians = MathUtil.degreesToRadians(i);
				double x = Math.cos(radians) * Math.sin(radians)
						* radius;
				double z = Math.sin(radians) * radius;
				double y = mTerrain.getAltitude(x, z) + distanceFromGround;

				if (i > 0)
					y = (y + mLastY) * .5f;
				mLastY = y;

				path.addPoint(new Vector3(x, y, z));
			}
			path.isClosedCurve(true);
			return path;
		}

	}

}
