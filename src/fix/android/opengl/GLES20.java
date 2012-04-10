package fix.android.opengl;

public class GLES20
{
    static
    {
        System.loadLibrary("fix-GLES20");
    }

    native public static void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset);

    native public static void glDrawElements(int mode, int count, int type, int offset);


    private GLES20()
    {}
}
