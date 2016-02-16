package im.actor.runtime.cocoa;

import im.actor.runtime.AssetsRuntime;

public class CocoaAssetsProxyProvider implements AssetsRuntime {

    private static AssetsRuntime assetsRuntime;

    public static void setAssetsRuntime(AssetsRuntime assetsRuntime) {
        CocoaAssetsProxyProvider.assetsRuntime = assetsRuntime;
    }

    @Override
    public boolean hasAsset(String name) {
        return assetsRuntime.hasAsset(name);
    }

    @Override
    public String loadAsset(String name) {
        return assetsRuntime.loadAsset(name);
    }

    @Override
    public byte[] loadBinAsset(String name) {
        return assetsRuntime.loadBinAsset(name);
    }

//    @Override
//    public native boolean hasAsset(String name)/*-[
//        NSBundle *mainBundle = [NSBundle mainBundle];
//        NSString *filePath = [mainBundle pathForResource:name ofType:nil];
//        return filePath != nil;
//    ]-*/;
//
//    @Override
//    public native String loadAsset(String name)/*-[
//        NSBundle *mainBundle = [NSBundle mainBundle];
//        NSString *filePath = [mainBundle pathForResource:name ofType:nil];
//        NSStringEncoding enc;
//        NSString* res = [NSString stringWithContentsOfFile:filePath usedEncoding:&enc error:nil];
//        return res;
//    ]-*/;
}
