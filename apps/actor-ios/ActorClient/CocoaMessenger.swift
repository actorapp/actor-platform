//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

private var holder:CocoaMessenger?;

var MSG : CocoaMessenger {
get{
    if (holder == nil){
        var dbPath = NSSearchPathForDirectoriesInDomains(.DocumentDirectory,
            .UserDomainMask, true)[0].stringByAppendingPathComponent("actor.db");
    

        // Providers
        var builder = AMConfigurationBuilder();    
        builder.setLogProvider(CocoaLogProvider())
        builder.setNetworkProvider(AMManagedNetworkProvider(AMAsyncConnectionFactory: CocoaTcpConnectionFactory()))
        builder.setHttpDownloaderProviderWithAMHttpDownloaderProvider(CocoaHttpProvider())
        builder.setThreadingProvider(AMCocoaThreadingProvider())
        builder.setStorageProvider(CocoaStorage(dbPath: dbPath))
        builder.setMainThreadProvider(CocoaMainThreadProvider())
        builder.setLocaleProvider(CocoaLocale())
        builder.setPhoneBookProvider(PhoneBookProvider())
        builder.setCryptoProvider(CocoaCryptoProvider())
        builder.setFileSystemProvider(CocoaFileSystem())
        builder.setDispatcherProvider(CocoaDispatcherProvider())
        builder.setNotificationProvider(iOSNotificationProvider())
        builder.setEnableNetworkLogging(true)
        builder.setEnableFilesLoggingWithBoolean(true)
        
        // Connection
        var url = NSBundle.mainBundle().objectForInfoDictionaryKey("API_URL") as! String
        NSLog("url: \(url)")
        builder.addEndpoint(url);
        
        var deviceKey = NSUUID().UUIDString;
        
        builder.setApiConfiguration(AMApiConfiguration(NSString: "Actor iOS", withInt: 1, withNSString: "???", withNSString: "My Device", withNSString: deviceKey))

        holder = CocoaMessenger(AMConfiguration: builder.build());
    }
    return holder!;
    }
}

@objc class CocoaMessenger : AMBaseMessenger {
    class func messenger() -> CocoaMessenger { return MSG }
    
    func sendUIImage(image: UIImage, peer: AMPeer) {
        var thumb = image.resizeSquare(90, maxH: 90);
        var resized = image.resizeOptimize(1200 * 1200);
        
        var thumbData = UIImageJPEGRepresentation(thumb, 0.55);
        
        NSLog("Thumb size \(thumbData.length), \(thumb.size.width * thumb.scale)x\(thumb.size.height * thumb.scale)");
        
        var descriptor = "/tmp/"+NSUUID().UUIDString
        var path = CocoaFiles.pathFromDescriptor(descriptor);
        
        UIImageJPEGRepresentation(resized, 0.80).writeToFile(path, atomically: true)
        
        MSG.sendPhotoWithAMPeer(peer, withNSString: "image.jpg", withInt: jint(resized.size.width), withInt: jint(resized.size.height), withAMFastThumb: AMFastThumb(int: jint(thumb.size.width), withInt: jint(thumb.size.height), withByteArray: thumbData.toJavaBytes()), withAMFileSystemReference: CocoaFile(path: descriptor))
    }
}