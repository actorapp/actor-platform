package im.actor.runtime;

public interface AssetsRuntime {
    boolean hasAsset(String name);

    String loadAsset(String name);
}
