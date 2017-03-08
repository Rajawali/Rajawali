package c.org.rajawali3d.materials.shaders.ES2;

import c.org.rajawali3d.materials.shaders.definitions.DataType;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface DataTypeES2 extends DataType {

    class VOID implements DataTypeES2 {

        @Override public String toString() {
            return DataType.VOID;
        }
    }

    class BOOL implements DataTypeES2 {

        @Override public String toString() {
            return DataType.BOOL;
        }
    }

    class INT implements DataTypeES2 {

        @Override public String toString() {
            return DataType.INT;
        }
    }

    class FLOAT implements DataTypeES2 {

        @Override public String toString() {
            return DataType.FLOAT;
        }
    }

    class VEC2 implements DataTypeES2 {

        @Override public String toString() {
            return DataType.VEC2;
        }
    }

    class VEC3 implements DataTypeES2 {

        @Override public String toString() {
            return DataType.VEC3;
        }
    }

    class VEC4 implements DataTypeES2 {

        @Override public String toString() {
            return DataType.VEC4;
        }
    }

    class BVEC2 implements DataTypeES2 {

        @Override public String toString() {
            return DataType.BVEC2;
        }
    }

    class BVEC3 implements DataTypeES2 {

        @Override public String toString() {
            return DataType.BVEC3;
        }
    }

    class BVEC4 implements DataTypeES2 {

        @Override public String toString() {
            return DataType.BVEC4;
        }
    }

    class IVEC2 implements DataTypeES2 {

        @Override public String toString() {
            return DataType.IVEC2;
        }
    }

    class IVEC3 implements DataTypeES2 {

        @Override public String toString() {
            return DataType.IVEC3;
        }
    }

    class IVEC4 implements DataTypeES2 {

        @Override public String toString() {
            return DataType.IVEC4;
        }
    }

    class MAT2 implements DataTypeES2 {

        @Override public String toString() {
            return DataType.MAT2;
        }
    }
    class MAT3 implements DataTypeES2 {

        @Override public String toString() {
            return DataType.MAT3;
        }
    }
    class MAT4 implements DataTypeES2 {

        @Override public String toString() {
            return DataType.MAT4;
        }
    }

    class SAMPLER2D implements DataTypeES2 {

        @Override public String toString() {
            return DataType.SAMPLER2D;
        }
    }
    class SAMPLER_CUBE implements DataTypeES2 {

        @Override public String toString() {
            return DataType.SAMPLER_CUBE;
        }
    }
}
