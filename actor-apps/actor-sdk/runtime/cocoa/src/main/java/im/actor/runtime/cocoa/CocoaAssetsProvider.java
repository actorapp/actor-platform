package im.actor.runtime.cocoa;

import im.actor.runtime.AssetsRuntime;

public class CocoaAssetsProvider implements AssetsRuntime {

    @Override
    public native boolean hasAsset(String name)/*-[
        NSBundle *mainBundle = [NSBundle mainBundle];
        NSString *filePath = [mainBundle pathForResource:name ofType:nil];
        return filePath != nil;
    ]-*/;

    @Override
    public native String loadAsset(String name)/*-[
        NSBundle *mainBundle = [NSBundle mainBundle];
        NSString *filePath = [mainBundle pathForResource:name ofType:nil];
        NSStringEncoding enc;
        NSString* res = [NSString stringWithContentsOfFile:filePath usedEncoding:&enc error:nil];
        return res;
    ]-*/;
}
