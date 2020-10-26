package bindings;

import org.astonbitecode.j4rs.api.Instance;

import java.io.File;

public class RustDefs {
    public static native void exampleMethod(Instance<String> instance);

    public static native void blurImage(Instance<String> path);

    public static native void blendImages(Instance<String> path1, Instance<String> path2);

    public static native void acceptFile(Instance<File> fileInstance);

    static {
       System.loadLibrary("krustynative");
    }
}
