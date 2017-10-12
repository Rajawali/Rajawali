/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class Assimp {

    static {
        System.loadLibrary("assimp-native");
    }

    public native void init();
}
