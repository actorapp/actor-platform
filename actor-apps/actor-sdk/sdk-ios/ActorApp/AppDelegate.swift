//
//  Copyright (C) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

import Fabric
import Crashlytics
import ActorSDK

class AppDelegate : ActorApplicationDelegate {
    
    override init() {
        super.init()
        
        // Even when Fabric/Crashlytics not configured
        // this method doesn't crash
        Fabric.with([Crashlytics.self()])
        
        // Creating Actor
        ActorSDK.sharedActor().createActor()
    }
    
    func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject : AnyObject]?) -> Bool {
        
        let style = ActorSDK.sharedActor().style
        
        style.isDarkApp = true
        
        style.searchStatusBarStyle = .LightContent
        style.searchBackgroundColor = UIColor(rgb: 0x272422)
        style.searchFieldBgColor = UIColor(rgb: 0x322d28)
        
        style.avatarDarkBlue = UIColor(rgb: 0x0087fe)
        style.avatarLightBlue = UIColor(rgb: 0x6494ed)
        style.avatarGreen = UIColor(rgb: 0x94c247)
        style.avatarOrange = UIColor(rgb: 0xdf7948)
        style.avatarPink = UIColor(rgb: 0x9b5c3c)
        style.avatarPurple = UIColor(rgb: 0x35875c)
        style.avatarYellow = UIColor(rgb: 0xffb452)
        
        style.vcBackyardColor = UIColor(rgb: 0x272422)
        style.vcBgColor = UIColor(rgb: 0x322d28)
        style.vcPanelBgColor = UIColor(rgb: 0x272422)
        
        style.vcTextColor = UIColor(rgb: 0xe6ce94)
        style.vcHintColor = UIColor(rgb: 0x575045)
        style.vcTintColor = UIColor(rgb: 0x93c760)
        style.vcDestructiveColor = UIColor(rgb: 0xe8440e)
        
        style.vcSwitchOn = UIColor(rgb: 0x738c58)
        style.vcSwitchOff = UIColor(rgb: 0x272422)
        
        style.vcSeparatorColor = UIColor(rgb: 0x504a41)
        style.vcSelectedColor = UIColor(rgb: 0x4b443b)
        
        style.cellHeaderColor = UIColor(rgb: 0x7c7365)
        style.cellFooterColor = UIColor(rgb: 0x7c7365)
        
        style.tabSelectedIconColor = UIColor(rgb: 0xf3eadf)
        style.tabUnselectedIconColor = UIColor(rgb: 0x7c7365)
        style.tabSelectedTextColor = UIColor(rgb: 0xf3eadf)
        style.tabUnselectedTextColor = UIColor(rgb: 0x7c7365)
        
        style.navigationBgColor = UIColor(rgb: 0x272422)
        style.navigationTintColor = UIColor(rgb: 0xf3eadf)
        style.navigationTitleColor = UIColor(rgb: 0xf3eadf)
        style.navigationSubtitleColor = UIColor(rgb: 0x7c7365)
        
        ActorSDK.sharedActor().presentMessengerInNewWindow()
        
        return true;
    }
}