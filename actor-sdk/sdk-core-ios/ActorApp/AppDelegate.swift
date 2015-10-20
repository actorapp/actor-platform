//
//  Copyright (C) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

import Fabric
import Crashlytics
import ActorSDK

@objc public class AppDelegate : ActorApplicationDelegate {
    
    override init() {
        super.init()
        
        // Even when Fabric/Crashlytics not configured
        // this method doesn't crash
        Fabric.with([Crashlytics.self()])
        
        ActorSDK.sharedActor().inviteUrlHost = "quit.email"
        ActorSDK.sharedActor().inviteUrlScheme = "actor"
        
        // Creating Actor
        ActorSDK.sharedActor().createActor()
    }
    
    public override func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject : AnyObject]?) -> Bool {
        super.application(application, didFinishLaunchingWithOptions: launchOptions)
//        let style = ActorSDK.sharedActor().style
//        
//        style.isDarkApp = true
//        
//        style.vcStatusBarStyle = .LightContent
//        style.vcTintColor = UIColor(rgb: 0x4e88ff)
//        style.vcBgColor = UIColor(rgb: 0x282c34)
//        style.vcBackyardColor = UIColor(rgb: 0x21252b)
//        style.vcSeparatorColor = UIColor(rgb: 0x1e2128)
//        style.vcTextColor = UIColor(rgb: 0xa9b0bd)
//        style.vcHintColor = UIColor(red: 164/255.0, green: 164/255.0, blue: 164/255.0, alpha: 1)
//        style.vcSectionColor = UIColor.whiteColor()
//        style.vcDestructiveColor = UIColor(rgb: 0xff4848)
//        style.vcSelectedColor = UIColor(rgb: 0x4e88ff)
//        style.vcSwitchOn = UIColor(rgb: 0x95c174)
//        style.vcPanelBgColor = UIColor(rgb: 0x181a1f)
//        
//        style.searchStatusBarStyle = .LightContent
//        style.searchBackgroundColor = UIColor(rgb: 0x21252b)
//        style.searchFieldTextColor = UIColor(rgb: 0xa9b0bd)
//        style.searchFieldBgColor = UIColor(rgb: 0x282c34)
//        style.searchCancelColor = UIColor(rgb: 0x4e88ff)
//        
//        style.navigationBgColor = UIColor(rgb: 0x1f2228)
//        style.navigationTitleColor = UIColor.whiteColor()
//        
//        style.cellFooterColor = style.vcHintColor
//        
//        style.chatTextBubbleOutColor = UIColor(rgb: 0x2385d8)
//        style.chatTextBubbleOutBorderColor = UIColor(rgb: 0x1c5591)
//        style.chatTextOutColor = UIColor.whiteColor()
//        style.chatTextOutUnsupportedColor = UIColor(rgb: 0x50b1ae)
//        style.chatTextDateOutColor = UIColor.alphaWhite(0.56)
//        
//        style.chatTextBubbleInColor =  UIColor.whiteColor()
//        style.chatTextBubbleInBorderColor = UIColor(rgb: 0x95959b)
//        style.chatTextInColor = UIColor(rgb: 0x141617)
//        style.chatTextInUnsupportedColor = UIColor(rgb: 0x50b1ae)
//        style.chatTextDateInColor = UIColor(rgb: 0x979797)
//        
//        style.chatStatusActive = UIColor(rgb: 0x13f17b)
//        style.chatStatusPassive = UIColor.alphaWhite(0.4)
//        
//        // style.chatBgColor = UIColor(patternImage: UIImage(named: "bg_items")!)
//        
//        style.placeholderBgColor = UIColor(rgb: 0x313237)
        
        ActorSDK.sharedActor().presentMessengerInNewWindow()
        
        return true;
    }
    
//    public override func application(application: UIApplication, handleOpenURL url: NSURL) -> Bool {
//        return true
//    }
////    
//    public override func application(application: UIApplication, openURL url: NSURL, sourceApplication: String?, annotation: AnyObject) -> Bool {
//        return false
//    }
}