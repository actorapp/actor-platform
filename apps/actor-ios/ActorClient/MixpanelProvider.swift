//
//  File.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 10.05.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class MixpanelProvider:NSObject, AMAnalyticsProvider {
    
    var mixpanel: Mixpanel!
    
    init(token: String) {
        mixpanel = Mixpanel.sharedInstanceWithToken(token)
    }
    
    private func saveDeviceId(deviceId: String!) {
        mixpanel.registerSuperProperties(["deviceId": deviceId])
    }
    
    func onLoggedOutWithNSString(deviceId: String!) {
        saveDeviceId(deviceId)
        mixpanel.identify("device:\(deviceId)")
    }

    func onLoggedInWithNSString(deviceId: String!, withInt uid: jint, withLong phoneNumber: jlong, withNSString userName: String!) {
        saveDeviceId(deviceId)
        mixpanel.identify("uid:\(uid)")
        mixpanel.people.set("PhoneNumber", to: "\(phoneNumber)")
        mixpanel.people.set("$name", to: "\(userName)")
    }
    
    func onLoggedInPerformedWithNSString(deviceId: String!, withInt uid: jint, withLong phoneNumber: jlong, withNSString userName: String!) {
        saveDeviceId(deviceId)
        mixpanel.createAlias("uid:\(uid)", forDistinctID: "device:\(deviceId)")
        mixpanel.identify("uid:\(uid)")
        mixpanel.people.set("PhoneNumber", to: "\(phoneNumber)")
        mixpanel.people.set("$name", to: "\(userName)")
    }
    
    func trackEventWithNSString(event: String!) {
        mixpanel.track(event)
    }
    
    func trackEventWithNSString(event: String!, withJavaUtilHashMap hashMap: JavaUtilHashMap!) {
        var props : [NSObject: AnyObject] = [:]
        var keys = hashMap.keySet().toArray()
        for i in 0..<hashMap.size() {
            var key = keys.objectAtIndex(UInt(i)) as! String
            var value = hashMap.getWithId(key) as! String
            props.updateValue(key, forKey: value)
        }
        mixpanel.track(event, properties: props)
    }
}