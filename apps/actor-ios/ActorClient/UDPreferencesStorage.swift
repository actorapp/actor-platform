//
//  UDPreferencesStorage.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 15.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class UDPreferencesStorage: NSObject, DKPreferencesStorage {
    
    let prefs = NSUserDefaults.standardUserDefaults()
    
    func putLong(key: String!, withValue v: jlong) {
        prefs.setObject(NSNumber(longLong: v), forKey: key)
        prefs.synchronize()
    }
    
    func getLong(key: String!, withDefault def: jlong) -> jlong {
        var val: AnyObject? = prefs.objectForKey(key)
        if (val == nil || !(val is NSNumber)) {
            return def;
        } else {
           return (val as! NSNumber).longLongValue
        }
    }
    
    func putInt(key: String!, withValue v: jint) {
        prefs.setInteger(Int(v), forKey: key)
        prefs.synchronize()
    }
    
    func getInt(key: String!, withDefault def: jint) -> jint {
        var val: AnyObject? = prefs.objectForKey(key)
        if (val == nil || !(val is NSNumber)) {
            return def;
        } else {
            return (val as! NSNumber).intValue
        }
    }
    
    func putBool(key: String!, withValue v: Bool) {
        prefs.setBool(v, forKey: key)
        prefs.synchronize()
    }
    
    func getBool(key: String!, withDefault def: Bool) -> Bool {
        var val: AnyObject? = prefs.objectForKey(key);
        if (val == nil || (!(val is Bool))) {
            return def;
        } else {
            return val as! Bool;
        }
    }
    
    func putBytes(key: String!, withValue v: IOSByteArray!) {
        prefs.setObject(v.toNSData(), forKey: key)
        prefs.synchronize()
    }
    
    func getBytes(key: String!) -> IOSByteArray! {
        var val: AnyObject? = prefs.objectForKey(key);
        if (val == nil || !(val is NSData)){
            return nil
        } else {
            return (val as! NSData).toJavaBytes()
        }
    }
    
    func putString(key: String!, withValue v: String!) {
        prefs.setObject(v, forKey: key)
        prefs.synchronize()
    }
    
    func getString(key: String!) -> String! {
        var val: AnyObject? = prefs.objectForKey(key);
        if (val == nil || !(val is String)) {
            return nil
        } else {
            return val as! String
        }
    }
}