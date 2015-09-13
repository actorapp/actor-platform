//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

var Actor : CocoaMessenger!

func createActor() {
    if Actor != nil {
        return
    }
    
    ARCocoaStorageProxyProvider.setStorageRuntime(CocoaStorageRuntime())
    ARCocoaHttpProxyProvider.setHttpRuntime(CocoaHttpRuntime())
    ARCocoaFileSystemProxyProvider.setFileSystemRuntime(CocoaFileSystemRuntime())
    ARCocoaNetworkProxyProvider.setNetworkRuntime(CocoaNetworkRuntime())
    
    let config = Config()
    
    let builder = ACConfigurationBuilder();
    
    // Api Connections
    let apiId = 2
    let apiKey = "2ccdc3699149eac0a13926c77ca84e504afd68b4f399602e06d68002ace965a3"
    let deviceKey = NSUUID().UUIDString
    let deviceName = UIDevice.currentDevice().name
    let appTitle = "Actor iOS"
    for url in config.endpoints {
        builder.addEndpoint(url);
    }
    builder.setApiConfiguration(ACApiConfiguration(appTitle: appTitle, withAppId: jint(apiId), withAppKey: apiKey, withDeviceTitle: deviceName, withDeviceId: deviceKey))
    
    // Providers
    builder.setPhoneBookProvider(PhoneBookProvider())
    builder.setNotificationProvider(iOSNotificationProvider())
    if config.mixpanel != nil {
        builder.setAnalyticsProvider(MixpanelProvider(token: config.mixpanel!))
    }
    
    // Stats
    builder.setPlatformType(ACPlatformTypeEnum.values().objectAtIndex(ACPlatformType.IOS.rawValue) as! ACPlatformTypeEnum)
    builder.setDeviceCategory(ACDeviceCategoryEnum.values().objectAtIndex(ACDeviceCategory.MOBILE.rawValue) as! ACDeviceCategoryEnum)
    
    // Logs
    builder.setEnableFilesLogging(true)
    
    // Creating messenger
    Actor = CocoaMessenger(configuration: builder.build(), config: config);
}

@objc class CocoaMessenger : ACCocoaMessenger {
    
    let config: Config

    init!(configuration: ACConfiguration!, config: Config) {
        self.config = config
        super.init(configuration: configuration)
    }
    
    func sendUIImage(image: UIImage, peer: ACPeer) {
        let thumb = image.resizeSquare(90, maxH: 90);
        let resized = image.resizeOptimize(1200 * 1200);
        
        let thumbData = UIImageJPEGRepresentation(thumb, 0.55);
        let fastThumb = ACFastThumb(int: jint(thumb.size.width), withInt: jint(thumb.size.height), withByteArray: thumbData!.toJavaBytes())
        
        let descriptor = "/tmp/"+NSUUID().UUIDString
        let path = CocoaFiles.pathFromDescriptor(descriptor);
        
        UIImageJPEGRepresentation(resized, 0.80)!.writeToFile(path, atomically: true)
        
        sendPhotoWithPeer(peer, withName: "image.jpg", withW: jint(resized.size.width), withH: jint(resized.size.height), withThumb: fastThumb, withDescriptor: descriptor)
    }
    
    private func prepareAvatar(image: UIImage) -> String {
        let res = "/tmp/" + NSUUID().UUIDString
        let avatarPath = CocoaFiles.pathFromDescriptor(res)
        let thumb = image.resizeSquare(800, maxH: 800);
        UIImageJPEGRepresentation(thumb, 0.8)!.writeToFile(avatarPath, atomically: true)
        return res
    }
    
    func changeOwnAvatar(image: UIImage) {
        changeMyAvatarWithDescriptor(prepareAvatar(image))
    }
    
    func changeGroupAvatar(gid: jint, image: UIImage) {
        changeGroupAvatarWithGid(gid, withDescriptor: prepareAvatar(image))
    }
}