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
    
    func loadSound(_ soundFile:String? = ""){
        if !isLoaded {
            isLoaded = true
            
            var path = Bundle.framework.url(forResource: "notification", withExtension: "caf")
            
            if let fileURL: URL = URL(fileURLWithPath: "/Library/Ringtones/\(soundFile)") {
                path = fileURL
            }
            
            AudioServicesCreateSystemSoundID(path! as CFURL, &internalMessage)
        }
    }
    
    func onMessageArriveInApp(with messenger: ACMessenger!) {
        let currentTime = Date().timeIntervalSinceReferenceDate
        if (currentTime - lastSoundPlay > 0.2) {
            let peer = ACPeer.user(with: jint(messenger.myUid()))
            let soundFileSting = messenger.getNotificationsSound(with: peer)
            loadSound(soundFileSting)
            AudioServicesPlaySystemSound(internalMessage)
            lastSoundPlay = currentTime
        }
    }
    
    
    func onNotification(with messenger: ACMessenger!, withTopNotifications topNotifications: JavaUtilList!, withMessagesCount messagesCount: jint, withConversationsCount conversationsCount: jint) {
        // Not Supported
    }
    
    func onUpdateNotification(with messenger: ACMessenger!, withTopNotifications topNotifications: JavaUtilList!, withMessagesCount messagesCount: jint, withConversationsCount conversationsCount: jint) {
        // Not Supported
    }
    
    func hideAllNotifications() {
        dispatchOnUi { () -> Void in
            // Clearing notifications
            if let number = Actor.getGlobalState().globalCounter.get() {
                UIApplication.shared.applicationIconBadgeNumber = 0 // If current value will equals to number + 1
                UIApplication.shared.applicationIconBadgeNumber = number.intValue + 1
                UIApplication.shared.applicationIconBadgeNumber = number.intValue
            } else {
                UIApplication.shared.applicationIconBadgeNumber = 0
            }
            
            // Clearing local notifications
            UIApplication.shared.cancelAllLocalNotifications()
        }
    }
}
