//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

private var holder:CocoaMessenger?;

var MSG : CocoaMessenger {
get{
    if (holder == nil){
        
        // Providers
        var builder = AMConfigurationBuilder();    
        builder.setLogProvider(CocoaLogProvider())
        builder.setNetworkProvider(CocoaNetworkProvider())
        builder.setHttpProvider(CocoaHttpProvider())
        builder.setThreadingProvider(AMCocoaThreadingProvider())
        builder.setStorageProvider(CocoaStorage())
        builder.setMainThreadProvider(CocoaMainThreadProvider())
        builder.setLocaleProvider(CocoaLocale())
        builder.setPhoneBookProvider(PhoneBookProvider())
        builder.setCryptoProvider(CocoaCryptoProvider())
        builder.setFileSystemProvider(CocoaFileSystem())
        builder.setDispatcherProvider(CocoaDispatcherProvider())
        builder.setNotificationProvider(iOSNotificationProvider())
        builder.setAppCategory(AMAppCategoryEnum.values().objectAtIndex(AMAppCategory.IOS.rawValue) as! AMAppCategoryEnum)
        builder.setDeviceCategory(AMDeviceCategoryEnum.values().objectAtIndex(AMDeviceCategory.MOBILE.rawValue) as! AMDeviceCategoryEnum)
        
        // Parameters
        var apiId = (NSBundle.mainBundle().objectForInfoDictionaryKey("API_ID") as! String).toInt()!
        var apiKey = (NSBundle.mainBundle().objectForInfoDictionaryKey("API_KEY") as! String)
        var apiUrl = NSBundle.mainBundle().objectForInfoDictionaryKey("API_URL") as! String
        var apiUrl2 = NSBundle.mainBundle().objectForInfoDictionaryKey("API_URL2") as! String
        var deviceKey = NSUUID().UUIDString
        var deviceName = UIDevice.currentDevice().name
        var appTitle = "Actor iOS"
        
        builder.addEndpoint(apiUrl);
        builder.addEndpoint(apiUrl2);
        builder.setApiConfiguration(AMApiConfiguration(NSString: appTitle, withInt: jint(apiId), withNSString: apiKey, withNSString: deviceName, withNSString: deviceKey))

        // Creating messenger
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
        
        var descriptor = "/tmp/"+NSUUID().UUIDString
        var path = CocoaFiles.pathFromDescriptor(descriptor);
        
        UIImageJPEGRepresentation(resized, 0.80).writeToFile(path, atomically: true)
        
        MSG.sendPhotoWithAMPeer(peer, withNSString: "image.jpg", withInt: jint(resized.size.width), withInt: jint(resized.size.height), withAMFastThumb: AMFastThumb(int: jint(thumb.size.width), withInt: jint(thumb.size.height), withByteArray: thumbData.toJavaBytes()), withAMFileSystemReference: CocoaFile(path: descriptor))
    }
}