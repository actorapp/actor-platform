//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation
import AudioToolbox.AudioServices

@objc class iOSNotificationProvider: NSObject, AMNotificationProvider {

    var internalMessage:SystemSoundID = 0
    var sounds: [String: SystemSoundID] = [:]
    
    override init() {
        super.init()
        var path = NSBundle.mainBundle().URLForResource("notification", withExtension: "caf");
        AudioServicesCreateSystemSoundID(path, &internalMessage)
    }
    
    func onMessageArriveInAppWithMessenger(messenger: AMMessenger!) {
        AudioServicesPlaySystemSound(internalMessage)
    }
    
    func onNotificationWithMessenger(messenger: AMMessenger!, withTopNotifications topNotifications: JavaUtilList!, withMessagesCount messagesCount: jint, withConversationsCount conversationsCount: jint, withSilentUpdate silentUpdate: Bool, withIsInApp isInApp: Bool) {
        if (silentUpdate) {
            return
        }
        
        var n = topNotifications.getWithInt(0) as! AMNotification
        
        messenger.getFormatter().formatNotificationText(n)
        
        var message = messenger.getFormatter().formatNotificationText(n)
        if (!messenger.isShowNotificationsText()) {
            message = NSLocalizedString("NotificationSecretMessage", comment: "New Message")
        }
        var senderUser = messenger.getUserWithUid(n.getSender())
        var sender = senderUser.getNameModel().get()
        var peer = n.getPeer()
        
        if (UInt(n.getPeer().getPeerType().ordinal()) == AMPeerType.GROUP.rawValue) {
            var group = messenger.getGroupWithGid(n.getPeer().getPeerId())
            sender = "\(sender)@\(group.getNameModel().get())"
        }
        
        if (isInApp) {
            if (messenger.isInAppNotificationSoundEnabled()) {
                var path = getNotificationSound(messenger)
                if (sounds[path] == nil) {
                    var fileUrl = NSBundle.mainBundle().URLForResource(path, withExtension: "caf");
                    var messageSound:SystemSoundID = 0
                    AudioServicesCreateSystemSoundID(fileUrl, &messageSound)
                    sounds[path] = messageSound
                }
                AudioServicesPlaySystemSound(sounds[path]!)
            }
            
            if (messenger.isInAppNotificationVibrationEnabled()) {
                AudioServicesPlaySystemSound(SystemSoundID(kSystemSoundID_Vibrate))
            }
            dispatch_async(dispatch_get_main_queue(), { () -> Void in
                TWMessageBarManager.sharedInstance().showMessageWithTitle(sender, description: message, type: TWMessageBarMessageType.Info, callback: { () -> Void in
                    var root = UIApplication.sharedApplication().keyWindow!.rootViewController!
                    if let tab = root as? MainTabViewController {
                        var controller = tab.viewControllers![tab.selectedIndex] as! AANavigationController
                        var destController = ConversationViewController(peer: peer)
                        destController.hidesBottomBarWhenPushed = true
                        controller.pushViewController(destController, animated: true)
                    } else if let split = root as? MainSplitViewController {
                        split.navigateDetail(ConversationViewController(peer: peer))
                    }
                })
            })
        } else {
            dispatch_async(dispatch_get_main_queue(), { () -> Void in
                var localNotification =  UILocalNotification ()
                localNotification.alertBody = "\(sender): \(message)"
                if (messenger.isNotificationSoundEnabled()) {
                    localNotification.soundName = "\(self.getNotificationSound(messenger)).caf"
                }
                UIApplication.sharedApplication().presentLocalNotificationNow(localNotification)
            })
        }
    }
    
    func hideAllNotifications() {
        
    }
    
    func getNotificationSound(messenger: AMMessenger!) -> String {
        if (messenger.getNotificationSound() != nil) {
            var path = NSBundle.mainBundle().pathForResource(messenger.getNotificationSound(), ofType: "caf")
            if (NSFileManager.defaultManager().fileExistsAtPath(path!)) {
                return messenger.getNotificationSound()
            }
        }
        return "iapetus"
    }
}
