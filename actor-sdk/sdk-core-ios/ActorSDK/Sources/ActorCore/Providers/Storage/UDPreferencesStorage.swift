//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

@objc class UDPreferencesStorage: NSObject, ARPreferencesStorage {
    
    let prefs = UserDefaults.standard
    
    var cachedPrefs = [String: AnyObject?]()
    
    func putLong(withKey key: String!, withValue v: jlong) {
        setObject(key, obj: NSNumber(value: v as Int64))
    }

    func getLongWithKey(_ key: String!, withDefault def: jlong) -> jlong {
        let val = fetchObj(key)
        if (val == nil || !(val is NSNumber)) {
            return def;
        } else {
            return (val as! NSNumber).int64Value
        }
    }
    
    func putInt(withKey key: String!, withValue v: jint) {
        setObject(key, obj: Int(v) as AnyObject?)
    }
    
    func getIntWithKey(_ key: String!, withDefault def: jint) -> jint {
        let val: AnyObject? = fetchObj(key)
        if (val == nil || !(val is NSNumber)) {
            return def;
        } else {
            return (val as! NSNumber).int32Value
        }
    }
    
    func putBool(withKey key: String!, withValue v: Bool) {
        setObject(key, obj: v as AnyObject?)
    }
    
    func getBoolWithKey(_ key: String!, withDefault def: Bool) -> Bool {
        let val: AnyObject? = fetchObj(key);
        if (val == nil || (!(val is Bool))) {
            return def
        } else {
            return val as! Bool;
        }
    }
    
    func putBytes(withKey key: String!, withValue v: IOSByteArray!) {
        if (v == nil) {
            setObject(key, obj: nil)
        } else {
            setObject(key, obj: v.toNSData() as AnyObject?)
        }
    }
    
    func getBytesWithKey(_ key: String!) -> IOSByteArray! {
        let val = fetchObj(key);
        if (val == nil || !(val is NSData)){
            return nil
        } else {
            return (val as! Data).toJavaBytes()
        }
    }
    
    func putString(withKey key: String!, withValue v: String!) {
        setObject(key, obj: v as AnyObject?)
    }
    
    func getStringWithKey(_ key: String!) -> String! {
        let val = fetchObj(key);
        if (val == nil || !(val is String)) {
            return nil
        } else {
            return val as! String
        }
    }
    
    func clear() {
        let appDomain = Bundle.main.bundleIdentifier!
        prefs.removePersistentDomain(forName: appDomain)
    }
    
    
    //
    // Interface
    //
    
    fileprivate func setObject(_ key: String, obj: AnyObject?) {
        if obj != nil {
            prefs.set(obj, forKey: key)
            cachedPrefs[key] = obj
        } else {
            prefs.removeObject(forKey: key)
            cachedPrefs.removeValue(forKey: key)
        }
        prefs.synchronize()
    }
    
    fileprivate func fetchObj(_ key: String) -> AnyObject? {
        if let obj = cachedPrefs[key] {
           return obj
        }
        let res = prefs.object(forKey: key)
        cachedPrefs[key] = res as AnyObject??
        return res as AnyObject?
    }
}
