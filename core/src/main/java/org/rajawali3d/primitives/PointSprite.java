package org.rajawali3d.primitives;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3.Axis;


public class PointSprite extends Plane {
	public PointSprite(float width, float height) {
		super(width, height, 1, 1, Axis.Z);
	}

    public PointSprite(float width, float height, boolean createVBOs) {
        super(width, height, 1, 1, Axis.Z, true, false, 1, createVBOs);
    }
	
	@Override
	public void render(Camera camera, final Matrix4 vpMatrix, final Matrix4 projMatrix, final Matrix4 vMatrix,
			final Matrix4 parentMatrix, Material sceneMaterial) {
		setLookAt(camera.getPosition());		
		super.render(camera, vpMatrix, projMatrix, vMatrix, parentMatrix, sceneMaterial);
	}
}
