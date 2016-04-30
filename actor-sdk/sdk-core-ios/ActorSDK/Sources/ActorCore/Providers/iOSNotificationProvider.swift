//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation
import AudioToolbox.AudioServices

@objc class iOSNotificationProvider: NSObject, ACNotificationProvider {

    var isLoaded = false
    var internalMessage:SystemSoundID = 0
    var sounds: [String: SystemSoundID] = [:]
    var lastSoundPlay: Double = 0
    
    override init() {
        super.init()
    }
    
    func loadSound(){
        if !isLoaded {
            isLoaded = true
            let path = NSBundle.framework.URLForResource("notification", withExtension: "caf");
            AudioServicesCreateSystemSoundID(path!, &internalMessage)
        }
    }
    
    func onMessageArriveInAppWithMessenger(messenger: ACMessenger!) {
        let currentTime = NSDate().timeIntervalSinceReferenceDate
        if (currentTime - lastSoundPlay > 0.2) {
            loadSound()
            AudioServicesPlaySystemSound(internalMessage)
            lastSoundPlay = currentTime
        }
    }
    
    func onNotificationWithMessenger(messenger: ACMessenger!, withTopNotifications topNotifications: JavaUtilList!, withMessagesCount messagesCount: jint, withConversationsCount conversationsCount: jint) {
        // Not Supported
    }
    
    func onUpdateNotificationWithMessenger(messenger: ACMessenger!, withTopNotifications topNotifications: JavaUtilList!, withMessagesCount messagesCount: jint, withConversationsCount conversationsCount: jint) {
        // Not Supported
    }
    
    func hideAllNotifications() {
        dispatchOnUi { () -> Void in
            // Clearing notifications
            if let number = Actor.getGlobalState().globalCounter.get() {
                UIApplication.sharedApplication().applicationIconBadgeNumber = 0 // If current value will equals to number + 1
                UIApplication.sharedApplication().applicationIconBadgeNumber = number.integerValue + 1
                UIApplication.sharedApplication().applicationIconBadgeNumber = number.integerValue
            } else {
                UIApplication.sharedApplication().applicationIconBadgeNumber = 0
            }
            
            // Clearing local notifications
            UIApplication.sharedApplication().cancelAllLocalNotifications()
        }
    }
}
