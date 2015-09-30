//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

var Actor : ACCocoaMessenger!
var Analytics: CocoaAnalytics!
var AppConfig: Config!

func createActor() {
    if Actor != nil {
        return
    }
    
    ARCocoaStorageProxyProvider.setStorageRuntime(CocoaStorageRuntime())
    ARCocoaHttpProxyProvider.setHttpRuntime(CocoaHttpRuntime())
    ARCocoaFileSystemProxyProvider.setFileSystemRuntime(CocoaFileSystemRuntime())
    ARCocoaNetworkProxyProvider.setNetworkRuntime(CocoaNetworkRuntime())
    
    AppConfig = Config()
    
    let builder = ACConfigurationBuilder();
    
    // Api Connections
    let apiId = 2
    let apiKey = "2ccdc3699149eac0a13926c77ca84e504afd68b4f399602e06d68002ace965a3"
    let deviceKey = NSUUID().UUIDString
    let deviceName = UIDevice.currentDevice().name
    let appTitle = "Actor iOS"
    for url in AppConfig.endpoints {
        builder.addEndpoint(url);
    }
    builder.setApiConfiguration(ACApiConfiguration(appTitle: appTitle, withAppId: jint(apiId), withAppKey: apiKey, withDeviceTitle: deviceName, withDeviceId: deviceKey))
    
    // Providers
    builder.setPhoneBookProvider(PhoneBookProvider())
    builder.setNotificationProvider(iOSNotificationProvider())
    
    // Stats
    builder.setPlatformType(ACPlatformTypeEnum.values().objectAtIndex(ACPlatformType.IOS.rawValue) as! ACPlatformTypeEnum)
    builder.setDeviceCategory(ACDeviceCategoryEnum.values().objectAtIndex(ACDeviceCategory.MOBILE.rawValue) as! ACDeviceCategoryEnum)
    
    // Logs
    // builder.setEnableFilesLogging(true)
    
    // Creating messenger
    Actor = ACCocoaMessenger(configuration: builder.build())
    
    // Creating analytics
    Analytics = CocoaAnalytics(internalAnalytics: ACActorAnalytics(ACMessenger: Actor))
}