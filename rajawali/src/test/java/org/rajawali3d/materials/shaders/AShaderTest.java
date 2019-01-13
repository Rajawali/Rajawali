package org.rajawali3d.materials.shaders;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class AShaderTest {

    @Test
    public void testGetShaderString() {
        AShader s = new AShader() {
            @Override
            public void main() {
            }
        };
        s.initialize();
        s.buildShader();
        assertEquals("\n"
                + "void main() {\n"
                + "}\n", s.getShaderString());
    }

    @Test
    public void testVec2Assignment() {
        AShader s = new AShader() {
            @Override
            public void main() {
                RVec2 v = new RVec2("v");
                v.assign(0);
                v.assign(0, 0);
                v.assignAdd(0);
                v.assignAdd(v);
                v.assignSubtract(0);
                v.assignSubtract(v);
                v.assignMultiply(0);
                v.assignMultiply(v);
                v.s().assign(0);
                v.s().assign(0x00);
                v.s().assign("(0+0)");
                v.t().assign(0);
                v.t().assign(0x00);
                v.t().assign("(0+0)");
                v.x().assign(0);
                v.x().assign(0x00);
                v.x().assign("(0+0)");
                v.y().assign(0);
                v.y().assign(0x00);
                v.y().assign("(0+0)");
            }
        };
        s.initialize();
        s.buildShader();
        assertEquals("\n"
                + "void main() {\n"
                + "vec2 v = vec2(0.0);\n"
                + "v = vec2(0.0, 0.0);\n"
                + "v += 0.0;\n"
                + "v += v;\n"
                + "v -= 0.0;\n"
                + "v -= v;\n"
                + "v *= 0.0;\n"
                + "v *= v;\n"
                + "v.s = 0.0;\n"
                + "v.s = 0.0;\n"
                + "v.s = (0+0);\n"
                + "v.t = 0.0;\n"
                + "v.t = 0.0;\n"
                + "v.t = (0+0);\n"
                + "v.x = 0.0;\n"
                + "v.x = 0.0;\n"
                + "v.x = (0+0);\n"
                + "v.y = 0.0;\n"
                + "v.y = 0.0;\n"
                + "v.y = (0+0);\n"
                + "}\n", s.getShaderString());
    }

    @Test
    public void testVec3Assignment() {
        AShader s = new AShader() {
            @Override
            public void main() {
                RVec3 v = new RVec3("v");
                v.assign(0);
                v.assign(0, 0, 0);
                v.assignAdd(0);
                v.assignAdd(v);
                v.assignSubtract(0);
                v.assignSubtract(v);
                v.assignMultiply(0);
                v.assignMultiply(v);
                v.r().assign(0);
                v.r().assign(0x00);
                v.r().assign("(0+0)");
                v.g().assign(0);
                v.g().assign(0x00);
                v.g().assign("(0+0)");
                v.b().assign(0);
                v.b().assign(0x00);
                v.b().assign("(0+0)");
                v.x().assign(0);
                v.x().assign(0x00);
                v.x().assign("(0+0)");
                v.y().assign(0);
                v.y().assign(0x00);
                v.y().assign("(0+0)");
                v.z().assign(0);
                v.z().assign(0x00);
                v.z().assign("(0+0)");
            }
        };
        s.initialize();
        s.buildShader();
        assertEquals("\n"
                + "void main() {\n"
                + "vec3 v = vec3(0.0);\n"
                + "v = vec3(0.0, 0.0, 0.0);\n"
                + "v += 0.0;\n"
                + "v += v;\n"
                + "v -= 0.0;\n"
                + "v -= v;\n"
                + "v *= 0.0;\n"
                + "v *= v;\n"
                + "v.r = 0.0;\n"
                + "v.r = 0.0;\n"
                + "v.r = (0+0);\n"
                + "v.g = 0.0;\n"
                + "v.g = 0.0;\n"
                + "v.g = (0+0);\n"
                + "v.b = 0.0;\n"
                + "v.b = 0.0;\n"
                + "v.b = (0+0);\n"
                + "v.x = 0.0;\n"
                + "v.x = 0.0;\n"
                + "v.x = (0+0);\n"
                + "v.y = 0.0;\n"
                + "v.y = 0.0;\n"
                + "v.y = (0+0);\n"
                + "v.z = 0.0;\n"
                + "v.z = 0.0;\n"
                + "v.z = (0+0);\n"
                + "}\n", s.getShaderString());
    }

    @Test
    public void testVec4Assignment() {
        AShader s = new AShader() {
            @Override
            public void main() {
                RVec4 v = new RVec4("v");
                v.assign(0);
                v.assign(0, 0, 0, 0);
                v.assignAdd(0);
                v.assignAdd(v);
                v.assignSubtract(0);
                v.assignSubtract(v);
                v.assignMultiply(0);
                v.assignMultiply(v);
                v.r().assign(0);
                v.r().assign(0x00);
                v.r().assign("(0+0)");
                v.g().assign(0);
                v.g().assign(0x00);
                v.g().assign("(0+0)");
                v.b().assign(0);
                v.b().assign(0x00);
                v.b().assign("(0+0)");
                v.a().assign(0);
                v.a().assign(0x00);
                v.a().assign("(0+0)");
                v.w().assign(0);
                v.w().assign(0x00);
                v.w().assign("(0+0)");
                v.x().assign(0);
                v.x().assign(0x00);
                v.x().assign("(0+0)");
                v.y().assign(0);
                v.y().assign(0x00);
                v.y().assign("(0+0)");
                v.z().assign(0);
                v.z().assign(0x00);
                v.z().assign("(0+0)");
            }
        };
        s.initialize();
        s.buildShader();
        assertEquals("\n"
                + "void main() {\n"
                + "vec4 v = vec4(0.0);\n"
                + "v = vec4(0.0, 0.0, 0.0, 0.0);\n"
                + "v += 0.0;\n"
                + "v += v;\n"
                + "v -= 0.0;\n"
                + "v -= v;\n"
                + "v *= 0.0;\n"
                + "v *= v;\n"
                + "v.r = 0.0;\n"
                + "v.r = 0.0;\n"
                + "v.r = (0+0);\n"
                + "v.g = 0.0;\n"
                + "v.g = 0.0;\n"
                + "v.g = (0+0);\n"
                + "v.b = 0.0;\n"
                + "v.b = 0.0;\n"
                + "v.b = (0+0);\n"
                + "v.a = 0.0;\n"
                + "v.a = 0.0;\n"
                + "v.a = (0+0);\n"
                + "v.w = 0.0;\n"
                + "v.w = 0.0;\n"
                + "v.w = (0+0);\n"
                + "v.x = 0.0;\n"
                + "v.x = 0.0;\n"
                + "v.x = (0+0);\n"
                + "v.y = 0.0;\n"
                + "v.y = 0.0;\n"
                + "v.y = (0+0);\n"
                + "v.z = 0.0;\n"
                + "v.z = 0.0;\n"
                + "v.z = (0+0);\n"
                + "}\n", s.getShaderString());
    }
}
