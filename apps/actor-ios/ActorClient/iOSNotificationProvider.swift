//
//  iOSNotificationProvider.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 17.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
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
    
    func onMessageArriveInApp() {
        AudioServicesPlaySystemSound(internalMessage)
    }
    
    func onDialogsOpen() {
        
    }
    
    func onChatOpenWithAMPeer(peer: AMPeer!) {
        
    }
    
    func onNotificationWithJavaUtilList(topNotifications: JavaUtilList!, withInt messagesCount: jint, withInt conversationsCount: jint) {
        
    }
}
