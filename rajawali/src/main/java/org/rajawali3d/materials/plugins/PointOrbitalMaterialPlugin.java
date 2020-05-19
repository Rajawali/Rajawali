package org.rajawali3d.materials.plugins;

import android.opengl.GLES20;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;

public class PointOrbitalMaterialPlugin implements IMaterialPlugin {
    private PointOrbitalVertexShaderFragment mVertexShaderFragment;

    public PointOrbitalMaterialPlugin() {
        this.mVertexShaderFragment = new PointOrbitalVertexShaderFragment(1);
    }

    public PointOrbitalMaterialPlugin(float speed) {
        this.mVertexShaderFragment = new PointOrbitalVertexShaderFragment(speed);
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return Material.PluginInsertLocation.PRE_LIGHTING;
    }

    @Override
    public IShaderFragment getVertexShaderFragment() {
        return mVertexShaderFragment;
    }

    @Override
    public IShaderFragment getFragmentShaderFragment() {
        return null;
    }

    @Override
    public void bindTextures(int i) {

    }

    @Override
    public void unbindTextures() {

    }

    public void setSpeed(float speed) {
        mVertexShaderFragment.setSpeed(speed);
    }

    private class PointOrbitalVertexShaderFragment extends AShader implements IShaderFragment
    {
        public final static String SHADER_ID = "ROTATION_VERTEX_FRAGMENT";
        float mSpeed;
        RFloat muSpeed;
        int muSpeedHandle;

        public PointOrbitalVertexShaderFragment(float speed)
        {
            super(ShaderType.VERTEX_SHADER_FRAGMENT);
            mSpeed = speed;
            initialize();
        }

        @Override
        public void initialize() {
            super.initialize();
            muSpeed = (RFloat) addUniform("uSpeed", DataType.FLOAT);
        }

        public void setSpeed(float speed) {
            mSpeed = speed;
            applyParams();
        }

        /*
         * animates points by reflecting the initial point position by a plane that rotates
         * through the origin and oriented to the initial position of each point.
         */
        @Override
        public void main() {
            RFloat u_time = (RFloat) getGlobal(DefaultShaderVar.U_TIME);
            RVec4 a_position = (RVec4) getGlobal(DefaultShaderVar.A_POSITION);
            RVec4 g_position = (RVec4) getGlobal(DefaultShaderVar.G_POSITION);

            RFloat p = new RFloat("p");
            p.assign(a_position.x().divide(length(a_position.xy())));
            RFloat q = new RFloat("q");
            q.assign(a_position.y().divide(length(a_position.xy())));

            RMat3 yaw = new RMat3("yaw");
            yaw.assign("mat3(\n" +
                    "    q,-p,0,\n" +
                    "    p,q,0,\n" +
                    "    0,0,1\n" +
                    ")");

            RFloat t = new RFloat("t");
            t.assign(u_time.multiply(muSpeed));

            RMat3 pitch = new RMat3("pitch");
            pitch.assign("mat3(\n" +
                    "    cos(t),0,sin(t),\n" +
                    "    0,1,0,\n" +
                    "    -sin(t),0,cos(t)\n" +
                    ")");

            RVec3 n = new RVec3("n");
            n.assign(0,0,1);
            n.assignMultiply(pitch);
            n.assignMultiply(yaw);
            // pitch before yaw because this is a reflecting normal

            RFloat a = new RFloat("a");
            a.assign(n.x());
            RFloat b = new RFloat("b");
            b.assign(n.y());
            RFloat c = new RFloat("c");
            c.assign(n.z());

            RMat4 reflect = new RMat4("reflect");
            reflect.assign("mat4(\n" +
                    "    1.0-2.*a*a, -2.*a*b, -2.*a*c, 0,\n" +
                    "    -2.*a*b, 1.0-2.*b*b, -2.*b*c, 0,\n" +
                    "    -2.*a*c, -2.*b*c, 1.0-2.*c*c, 0,\n" +
                    "    0, 0, 0, 1\n" +
                    ")");

            g_position.assign(a_position.multiply(reflect));
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muSpeedHandle = getUniformLocation(programHandle, "uSpeed");
        }

        @Override
        public void applyParams() {
            super.applyParams();
            GLES20.glUniform1f(muSpeedHandle, mSpeed);
        }


        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return Material.PluginInsertLocation.IGNORE;
        }

        @Override
        public void bindTextures(int nextIndex) {}

        @Override
        public void unbindTextures() {}
    }


}
