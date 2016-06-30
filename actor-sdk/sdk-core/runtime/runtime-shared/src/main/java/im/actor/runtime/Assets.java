package im.actor.runtime;

public class Assets {

    private static final AssetsRuntime assetsRuntime = new AssetsRuntimeProvider();

    public static boolean hasAsset(String name) {
        return assetsRuntime.hasAsset(name);
    }

    public static String loadAsset(String name) {
        return assetsRuntime.loadAsset(name);
    }

    public static byte[] loadBinAsset(String name) {
        return assetsRuntime.loadBinAsset(name);
    }
}
