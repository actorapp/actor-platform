////
////  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
////
//
//import Foundation
//
//class MixpanelProvider:NSObject, ACAnalyticsProvider {
//    
//    var mixpanel: Mixpanel!
//    
//    init(token: String) {
//        mixpanel = Mixpanel.sharedInstanceWithToken(token)
//    }
//    
//    private func saveDeviceId(deviceId: String!) {
//        mixpanel.registerSuperProperties(["deviceId": deviceId])
//    }
//    
//    func onLoggedOutWithDeviceId(deviceId: String!) {
//        saveDeviceId(deviceId)
//        mixpanel.identify("device:\(deviceId)")
//    }
//    
//    func onLoggedInWithDeviceId(deviceId: String!, withUid uid: jint, withPhoneNumber phoneNumber: jlong, withUserName userName: String!) {
//        saveDeviceId(deviceId)
//        mixpanel.identify("uid:\(uid)")
//        mixpanel.people.set("$phone", to: "\(phoneNumber)")
//        mixpanel.people.set("$name", to: "\(userName)")
//    }
//    
//    func onLoggedInPerformedWithDeviceId(deviceId: String!, withUid uid: jint, withPhoneNumber phoneNumber: jlong, withUserName userName: String!) {
//        saveDeviceId(deviceId)
//        mixpanel.createAlias("uid:\(uid)", forDistinctID: "device:\(deviceId)")
//        mixpanel.identify("uid:\(uid)")
//        mixpanel.people.set("$phone", to: "\(phoneNumber)")
//        mixpanel.people.set("$name", to: "\(userName)")
//    }
//    
//    func trackEvent(event: String!) {
//        mixpanel.track(event)
//    }
//    
//    func trackEvent(event: String!, withArgs hashMap: JavaUtilHashMap!) {
//        var props : [NSObject: AnyObject] = [:]
//        var keys = hashMap.keySet().toArray()
//        for i in 0..<hashMap.size() {
//            var key = keys.objectAtIndex(UInt(i)) as! String
//            var value = hashMap.getWithId(key) as! String
//            props.updateValue(key, forKey: value)
//        }
//        mixpanel.track(event, properties: props)
//    }
//}