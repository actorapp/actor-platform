//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class UDPreferencesStorage: NSObject, DKPreferencesStorage {
    
    let prefs = NSUserDefaults.standardUserDefaults()
    
    func putLongWithKey(key: String!, withValue v: jlong) {
        prefs.setObject(NSNumber(longLong: v), forKey: key)
        prefs.synchronize()
    }

    func getLongWithKey(key: String!, withDefault def: jlong) -> jlong {
        var val: AnyObject? = prefs.objectForKey(key)
        if (val == nil || !(val is NSNumber)) {
            return def;
        } else {
            return (val as! NSNumber).longLongValue
        }
    }
    
    func putIntWithKey(key: String!, withValue v: jint) {
        prefs.setInteger(Int(v), forKey: key)
        prefs.synchronize()
    }
    
    func getIntWithKey(key: String!, withDefault def: jint) -> jint {
        var val: AnyObject? = prefs.objectForKey(key)
        if (val == nil || !(val is NSNumber)) {
            return def;
        } else {
            return (val as! NSNumber).intValue
        }
    }
    
    func putBoolWithKey(key: String!, withValue v: Bool) {
        prefs.setBool(v, forKey: key)
        prefs.synchronize()
    }
    
    func getBoolWithKey(key: String!, withDefault def: Bool) -> Bool {
        var val: AnyObject? = prefs.objectForKey(key);
        if (val == nil || (!(val is Bool))) {
            return def;
        } else {
            return val as! Bool;
        }
    }
    
    func putBytesWithKey(key: String!, withValue v: IOSByteArray!) {
        prefs.setObject(v.toNSData(), forKey: key)
        prefs.synchronize()
    }
    
    func getBytesWithKey(key: String!) -> IOSByteArray! {
        var val: AnyObject? = prefs.objectForKey(key);
        if (val == nil || !(val is NSData)){
            return nil
        } else {
            return (val as! NSData).toJavaBytes()
        }
    }
    
    func putStringWithKey(key: String!, withValue v: String!) {
        prefs.setObject(v, forKey: key)
        prefs.synchronize()
    }
    
    func getStringWithKey(key: String!) -> String! {
        var val: AnyObject? = prefs.objectForKey(key);
        if (val == nil || !(val is String)) {
            return nil
        } else {
            return val as! String
        }
    }
    
    func clear() {
        var appDomain = NSBundle.mainBundle().bundleIdentifier!
        prefs.removePersistentDomainForName(appDomain)
    }
}