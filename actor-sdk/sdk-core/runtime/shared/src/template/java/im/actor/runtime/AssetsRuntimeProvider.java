package im.actor.runtime;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class AssetsRuntimeProvider implements AssetsRuntime {

    @Override
    public boolean hasAsset(String name) {
        return false;
    }

    @Override
    public String loadAsset(String name) {
        return null;
    }
}
