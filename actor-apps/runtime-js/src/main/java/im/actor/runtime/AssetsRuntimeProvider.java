package im.actor.runtime;

import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.ResourcePrototype;
import com.google.gwt.resources.client.TextResource;

import java.util.ArrayList;

public class AssetsRuntimeProvider implements AssetsRuntime {

    private static ArrayList<ClientBundleWithLookup> bundles = new ArrayList<ClientBundleWithLookup>();

    public static void registerBundle(ClientBundleWithLookup bundleWithLookup) {
        bundles.add(bundleWithLookup);
    }

    @Override
    public boolean hasAsset(String name) {
        name = name.replace('.', '_');

        Log.d("AssetsRuntime", "HasAsset: " + name);
        for (ClientBundleWithLookup b : bundles) {
            if (b.getResource(name) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String loadAsset(String name) {
        name = name.replace('.', '_');

        Log.d("AssetsRuntime", "loadAsset: " + name);
        for (ClientBundleWithLookup b : bundles) {
            ResourcePrototype res = b.getResource(name);
            if (res != null) {
                Log.d("AssetsRuntime", "loadAsset " + res);
                return ((TextResource) res).getText();
            }
        }

        return null;
    }
}
