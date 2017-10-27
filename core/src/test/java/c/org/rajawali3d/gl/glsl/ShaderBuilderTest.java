package c.org.rajawali3d.gl.glsl;

import org.junit.Test;

import c.org.rajawali3d.gl.glsl.DataType.*;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class ShaderBuilderTest {

    private static final class TestShader extends ShaderBuilder {

        @Override
        protected void main() {
            new BOOL(this, "bool_1", "true");
            writeConstructor(BOOL.typeString, new BOOL(this, "bool_1", "true", false));
            endLine();
            writeConstructor(BOOL.typeString, "false");
            endLine();
            new BOOL(this, "bool_2", constructor(BOOL.typeString, "true"));
            newLine();
        }
    }

    @Test
    public void construct() throws Exception {

        final TestShader testShader = new TestShader();

        final String shader = testShader.construct();

        System.out.println(shader);
    }
}