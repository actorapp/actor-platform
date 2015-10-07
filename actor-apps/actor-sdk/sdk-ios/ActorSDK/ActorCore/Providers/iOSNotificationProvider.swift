//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation
import AudioToolbox.AudioServices

@objc class iOSNotificationProvider: NSObject, ACNotificationProvider {

    var internalMessage:SystemSoundID = 0
    var sounds: [String: SystemSoundID] = [:]
    var lastSoundPlay: Double = 0
    
    override init() {
        super.init()
        let path = NSBundle.mainBundle().URLForResource("notification", withExtension: "caf");
        AudioServicesCreateSystemSoundID(path!, &internalMessage)
    }
    
    func onMessageArriveInAppWithMessenger(messenger: ACMessenger!) {
        let currentTime = NSDate().timeIntervalSinceReferenceDate
        if (currentTime - lastSoundPlay > 0.2) {
            AudioServicesPlaySystemSound(internalMessage)
            lastSoundPlay = currentTime
        }
    }
    
    func onNotificationWithMessenger(messenger: ACMessenger!, withTopNotifications topNotifications: JavaUtilList!, withMessagesCount messagesCount: jint, withConversationsCount conversationsCount: jint) {
        
        let n = topNotifications.getWithInt(0) as! ACNotification
        
        messenger.getFormatter().formatNotificationText(n)
        
        var message = messenger.getFormatter().formatNotificationText(n)
        if (!messenger.isShowNotificationsText()) {
            message = NSLocalizedString("NotificationSecretMessage", comment: "New Message")
        }
        let senderUser = messenger.getUserWithUid(n.sender)
        var sender = senderUser.getNameModel().get()
        let peer = n.peer
        
        if (peer.isGroup) {
            let group = messenger.getGroupWithGid(n.peer.peerId)
            sender = "\(sender)@\(group.getNameModel().get())"
        }
        
        dispatchOnUi { () -> Void in
            let localNotification =  UILocalNotification ()
            localNotification.alertBody = "\(sender): \(message)"
            if (messenger.isNotificationSoundEnabled()) {
                localNotification.soundName = "\(self.getNotificationSound(messenger)).caf"
            }
            localNotification.applicationIconBadgeNumber = Actor.getAppState().globalCounter.get().integerValue
            UIApplication.sharedApplication().presentLocalNotificationNow(localNotification)
        }
    }
    
    func onUpdateNotificationWithMessenger(messenger: ACMessenger!, withTopNotifications topNotifications: JavaUtilList!, withMessagesCount messagesCount: jint, withConversationsCount conversationsCount: jint) {
        // Not Supported
    }
    
    func hideAllNotifications() {
        dispatchOnUi { () -> Void in
            // Clearing notifications
            let number = Actor.getAppState().globalCounter.get().integerValue
            UIApplication.sharedApplication().applicationIconBadgeNumber = 0 // If current value will equals to number + 1
            UIApplication.sharedApplication().applicationIconBadgeNumber = number + 1
            UIApplication.sharedApplication().applicationIconBadgeNumber = number
            
            // Clearing local notifications
            UIApplication.sharedApplication().cancelAllLocalNotifications()
        }
    }
    
    func getNotificationSound(messenger: ACMessenger!) -> String {
        if (messenger.getNotificationSound() != nil) {
            let path = NSBundle.mainBundle().pathForResource(messenger.getNotificationSound(), ofType: "caf")
            if (NSFileManager.defaultManager().fileExistsAtPath(path!)) {
                return messenger.getNotificationSound()
            }
        }
        return "iapetus"
    }
}
