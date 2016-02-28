package im.actor.runtime;

public class AssetsRuntimeProvider implements AssetsRuntime {

    @Override
    public boolean hasAsset(String name) {
        return false;
    }

    @Override
    public String loadAsset(String name) {
        return null;
    }

    @Override
    public byte[] loadBinAsset(String name) {
        return new byte[0];
    }
}
