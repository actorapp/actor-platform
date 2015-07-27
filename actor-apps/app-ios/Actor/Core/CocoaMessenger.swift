//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

private var holder:CocoaMessenger?;

var MSG : CocoaMessenger {
get{
    if (holder == nil){
        
        var config = Config()
        
        // Providers
        var builder = AMConfigurationBuilder();    
        builder.setLogProvider(CocoaLogProvider())
        builder.setNetworkProvider(CocoaNetworkProvider())
        builder.setHttpProvider(CocoaHttpProvider())
        builder.setThreadingProvider(CocoaThreadingProvider())
        builder.setStorageProvider(CocoaStorage())
        builder.setMainThreadProvider(CocoaMainThreadProvider())
        builder.setLocaleProvider(CocoaLocaleProvider())
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
        if config.mixpanel != nil {
            builder.setAnalyticsProvider(MixpanelProvider(token: config.mixpanel!))
        }
        
        // Parameters
        var apiId = 2
        var apiKey = "2ccdc3699149eac0a13926c77ca84e504afd68b4f399602e06d68002ace965a3"
        var deviceKey = NSUUID().UUIDString
        var deviceName = UIDevice.currentDevice().name
        var appTitle = "Actor iOS"

        for url in config.endpoints {
            builder.addEndpoint(url);
        }
        
        builder.setApiConfiguration(AMApiConfiguration(appTitle: appTitle, withAppId: jint(apiId), withAppKey: apiKey, withDeviceTitle: deviceName, withDeviceId: deviceKey))

        // Creating messenger
        holder = CocoaMessenger(configuration: builder.build(), config: config);
    }
    return holder!;
    }
}

@objc class CocoaMessenger : AMBaseMessenger {
    class func messenger() -> CocoaMessenger { return MSG }
    
    let config: Config

    init!(configuration: AMConfiguration!, config: Config) {
        var env = AMMessengerEnvironmentEnum.values().objectAtIndex(AMMessengerEnvironment.IOS.rawValue) as! AMMessengerEnvironmentEnum
        self.config = config
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