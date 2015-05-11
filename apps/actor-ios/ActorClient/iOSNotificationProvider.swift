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
        
        var n = topNotifications.getWithInt(0) as! AMNotification
        
        var message = messenger.getFormatter().formatContentDialogTextWithInt(n.getSender(), withAMContentTypeEnum: n.getContentDescription().getContentType(), withNSString: n.getContentDescription().getText(), withInt: n.getContentDescription().getRelatedUser())
        var senderUser = messenger.getUsers().getWithLong(jlong(n.getSender())) as! AMUserVM
        var sender = senderUser.getName().get() as! String
        
        if (UInt(n.getPeer().getPeerType().ordinal()) == AMPeerType.GROUP.rawValue) {
            var group = messenger.getGroups().getWithLong(jlong(n.getPeer().getPeerId())) as! AMGroupVM
            sender = "\(sender)@\(group.getName().get() as! String)"
        }
        
        if (isInApp) {
            dispatch_async(dispatch_get_main_queue(), { () -> Void in
                TWMessageBarManager.sharedInstance()
                    .showMessageWithTitle(sender, description: message, type: TWMessageBarMessageType.Info)
            })
        } else {
            var localNotification =  UILocalNotification ()
            localNotification.alertTitle = sender
            localNotification.alertBody = message
            localNotification.soundName = "carme.caf"
            UIApplication.sharedApplication().presentLocalNotificationNow(localNotification)
        }
    }
    
    func hideAllNotifications() {
        
    }
}
