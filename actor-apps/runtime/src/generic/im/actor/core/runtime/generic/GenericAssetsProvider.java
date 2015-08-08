package im.actor.core.runtime.generic;

import java.io.InputStream;

import im.actor.runtime.AssetsRuntime;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class GenericAssetsProvider implements AssetsRuntime {

    @Override
    public boolean hasAsset(String name) {
        return getClass().getClassLoader().getResource(name) != null;
    }

    @Override
    public String loadAsset(String name) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
        // TODO: Read all
        return null;
    }
}
