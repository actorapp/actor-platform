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
        builder.setLifecycleProvider(CocoaLifecycleProvider())
        builder.setEnableFilesLogging(true)
        
        // Setting Analytics provider
        if let apiKey = NSBundle.mainBundle().infoDictionary?["MIXPANEL_API_KEY"] as? String {
            if (apiKey.trim().size() > 0) {
                builder.setAnalyticsProvider(MixpanelProvider(token: apiKey))
            }
        }
        
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
        
        builder.setApiConfiguration(AMApiConfiguration(appTitle: appTitle, withAppId: jint(apiId), withAppKey: apiKey, withDeviceTitle: deviceName, withDeviceId: deviceKey))

        // Creating messenger
        holder = CocoaMessenger(configuration: builder.build());
    }
    return holder!;
    }
}

@objc class CocoaMessenger : AMBaseMessenger {
    class func messenger() -> CocoaMessenger { return MSG }

    override init!(configuration: AMConfiguration!) {
        var env = AMMessengerEnvironmentEnum.values().objectAtIndex(AMMessengerEnvironment.IOS.rawValue) as! AMMessengerEnvironmentEnum
        super.init(environment: env, withConfiguration: configuration)
    }
    
    func sendUIImage(image: UIImage, peer: AMPeer) {
        var thumb = image.resizeSquare(90, maxH: 90);
        var resized = image.resizeOptimize(1200 * 1200);
        
        var thumbData = UIImageJPEGRepresentation(thumb, 0.55);
        var fastThumb = AMFastThumb(int: jint(thumb.size.width), withInt: jint(thumb.size.height), withByteArray: thumbData.toJavaBytes())
        
        var descriptor = "/tmp/"+NSUUID().UUIDString
        var path = CocoaFiles.pathFromDescriptor(descriptor);
        
        UIImageJPEGRepresentation(resized, 0.80).writeToFile(path, atomically: true)
        
        sendPhotoWithPeer(peer, withName: "image.jpg", withW: jint(resized.size.width), withH: jint(resized.size.height), withThumb: fastThumb, withDescriptor: descriptor)
    }
    
    private func prepareAvatar(image: UIImage) -> String {
        var res = "/tmp/" + NSUUID().UUIDString
        let avatarPath = CocoaFiles.pathFromDescriptor(res)
        var thumb = image.resizeSquare(800, maxH: 800);
        UIImageJPEGRepresentation(thumb, 0.8).writeToFile(avatarPath, atomically: true)
        return res
    }
    
    func changeOwnAvatar(image: UIImage) {
        changeMyAvatarWithDescriptor(prepareAvatar(image))
    }
    
    func changeGroupAvatar(gid: jint, image: UIImage) {
        changeGroupAvatarWithGid(gid, withDescriptor: prepareAvatar(image))
    }
}