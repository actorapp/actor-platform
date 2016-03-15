//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

@objc class UDPreferencesStorage: NSObject, ARPreferencesStorage {
    
    let prefs = NSUserDefaults.standardUserDefaults()
    
    var cachedPrefs = [String: AnyObject?]()
    
    func putLongWithKey(key: String!, withValue v: jlong) {
        setObject(key, obj: NSNumber(longLong: v))
    }

    func getLongWithKey(key: String!, withDefault def: jlong) -> jlong {
        let val = fetchObj(key)
        if (val == nil || !(val is NSNumber)) {
            return def;
        } else {
            return (val as! NSNumber).longLongValue
        }
    }
    
    func putIntWithKey(key: String!, withValue v: jint) {
        setObject(key, obj: Int(v))
    }
    
    func getIntWithKey(key: String!, withDefault def: jint) -> jint {
        let val: AnyObject? = fetchObj(key)
        if (val == nil || !(val is NSNumber)) {
            return def;
        } else {
            return (val as! NSNumber).intValue
        }
    }
    
    func putBoolWithKey(key: String!, withValue v: Bool) {
        setObject(key, obj: v)
    }
    
    func getBoolWithKey(key: String!, withDefault def: Bool) -> Bool {
        let val: AnyObject? = fetchObj(key);
        if (val == nil || (!(val is Bool))) {
            return def
        } else {
            return val as! Bool;
        }
    }
    
    func putBytesWithKey(key: String!, withValue v: IOSByteArray!) {
        if (v == nil) {
            setObject(key, obj: nil)
        } else {
            setObject(key, obj: v.toNSData())
        }
    }
    
    func getBytesWithKey(key: String!) -> IOSByteArray! {
        let val = fetchObj(key);
        if (val == nil || !(val is NSData)){
            return nil
        } else {
            return (val as! NSData).toJavaBytes()
        }
    }
    
    func putStringWithKey(key: String!, withValue v: String!) {
        setObject(key, obj: v)
    }
    
    func getStringWithKey(key: String!) -> String! {
        let val = fetchObj(key);
        if (val == nil || !(val is String)) {
            return nil
        } else {
            return val as! String
        }
    }
    
    func clear() {
        let appDomain = NSBundle.mainBundle().bundleIdentifier!
        prefs.removePersistentDomainForName(appDomain)
    }
    
    
    //
    // Interface
    //
    
    private func setObject(key: String, obj: AnyObject?) {
        if obj != nil {
            prefs.setObject(obj, forKey: key)
            cachedPrefs[key] = obj
        } else {
            prefs.removeObjectForKey(key)
            cachedPrefs.removeValueForKey(key)
        }
        prefs.synchronize()
    }
    
    private func fetchObj(key: String) -> AnyObject? {
        if let obj = cachedPrefs[key] {
           return obj
        }
        let res = prefs.objectForKey(key)
        cachedPrefs[key] = res
        return res
    }
}