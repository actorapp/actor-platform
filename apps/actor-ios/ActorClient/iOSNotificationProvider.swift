//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation

@objc class iOSNotificationProvider: NSObject, AMNotificationProvider {

    var internalMessage:SystemSoundID = 0
    
    override init() {
        super.init()
        var path = NSBundle.mainBundle().URLForResource("notification", withExtension: "aiff");
        AudioServicesCreateSystemSoundID(path, &internalMessage)
    }
    
    func onMessageArriveInAppWithAMMessenger(messenger: AMMessenger!) {
        AudioServicesPlaySystemSound(internalMessage)
    }
    
    func onNotificationWithAMMessenger(messenger: AMMessenger!, withJavaUtilList topNotifications: JavaUtilList!, withInt messagesCount: jint, withInt conversationsCount: jint, withBoolean silentUpdate: Bool, withBoolean isInApp: Bool) {
        if (silentUpdate) {
            return
        }
        
        if (isInApp) {
            // TODO: Implement
        } else {
            var localNotification =  UILocalNotification ()
            localNotification.alertTitle = "New Message!"
            localNotification.alertBody = "Message body"
            UIApplication.sharedApplication().presentLocalNotificationNow(localNotification)
        }
    }
    
    func hideAllNotifications() {
        
    }
}
