package bindings;

import org.astonbitecode.j4rs.api.Instance;

public class RustDefs {
    static {
        System.loadLibrary("krustynative");
    }

    public static native void exampleMethod(Instance<String> instance);

    public static native Instance<String> blurImage(Instance<String> path, Instance<Integer> passes);

    public static native void blendImages(Instance<String> path1, Instance<String> path2);

    public static native Instance<String> rotateImage(Instance<String> path, Instance<Integer> degree, Instance<Integer> r, Instance<Integer> g, Instance<Integer> b);

    public static native void print(Instance<String> str);
}
