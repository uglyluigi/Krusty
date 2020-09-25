package bindings;

public class RustDefs {
    public static native void exampleMethod();

    static {
       System.loadLibrary("krustynative");
    }
}
