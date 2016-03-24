package org.rajawali3d.debug;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Cylinder;
import org.rajawali3d.primitives.NPrism;

/**
 * @author Jared Woolston (jwoolston@idealcorp.com)
 */
public class CoordinateTrident extends Object3D {

    public CoordinateTrident() {

        final Material material = new Material();

        // Box at origin
        final Cube cube = new Cube(0.25f);
        cube.setMaterial(material);
        cube.setColor(0xffffff00); // Yellow
        addChild(cube);

        // Axis Arms
        final Cylinder xAxis = new Cylinder(0.75f, 0.05f, 1, 6);
        xAxis.setMaterial(material);
        xAxis.setColor(0xffff0000); // Red
        xAxis.rotate(Vector3.Axis.Y, 90.0);
        xAxis.setPosition(0.375, 0, 0);
        addChild(xAxis);

        final NPrism xTip = new NPrism(8, 0, 0.1f, 0.25f);
        xTip.setMaterial(material);
        xTip.setColor(0xffff0000); // Red
        xTip.rotate(Vector3.Axis.Z, 90.0);
        xTip.setPosition(0.875f, 0, 0);
        addChild(xTip);

        final Cylinder yAxis = new Cylinder(0.75f, 0.05f, 1, 6);
        yAxis.setMaterial(material);
        yAxis.setColor(0xff00ff00); // Green
        yAxis.rotate(Vector3.Axis.X, 90.0);
        yAxis.setPosition(0, 0.375, 0);
        addChild(yAxis);

        final NPrism yTip = new NPrism(8, 0, 0.1f, 0.25f);
        yTip.setMaterial(material);
        yTip.setColor(0xff00ff00); // Green
        yTip.setPosition(0, 0.875f, 0);
        addChild(yTip);

        final Cylinder zAxis = new Cylinder(0.75f, 0.05f, 1, 6);
        zAxis.setMaterial(material);
        zAxis.setColor(0xff0000ff); // Blue
        zAxis.setPosition(0, 0, 0.375);
        addChild(zAxis);

        final NPrism zTip = new NPrism(8, 0, 0.1f, 0.25f);
        zTip.setMaterial(material);
        zTip.setColor(0xff0000ff); // Blue
        zTip.rotate(Vector3.Axis.X, -90.0);
        zTip.setPosition(0, 0, 0.875f);
        addChild(zTip);
    }
}
