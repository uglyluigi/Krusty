package bindings;

import org.astonbitecode.j4rs.api.Instance;

import java.io.File;

public class RustDefs {
    public static native void exampleMethod(Instance<String> instance);

    public static native Instance<String> blurImage(Instance<String> path, Instance<Integer> passes);

    public static native void blendImages(Instance<String> path1, Instance<String> path2);

    public static native void print(Instance<String> str);

    static {
       System.loadLibrary("krustynative");
    }
}
