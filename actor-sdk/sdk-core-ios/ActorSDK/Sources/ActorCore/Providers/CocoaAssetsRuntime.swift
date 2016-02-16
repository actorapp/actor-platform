//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaAssetsRuntime: NSObject, ARAssetsRuntime {

    func hasAssetWithNSString(name: String!) -> jboolean {
        if NSBundle.mainBundle().pathForResource(name, ofType: nil) != nil {
            return true
        }
        if NSBundle.framework.pathForResource(name, ofType: nil) != nil {
            return true
        }
        return false
    }
    
    func loadAssetWithNSString(name: String!) -> String! {
        var path: String?
        path = NSBundle.mainBundle().pathForResource(name, ofType: nil)
        if path != nil {
            do {
                return try String(contentsOfFile: path!)
            } catch {
                
            }
        }
        
        path = NSBundle.framework.pathForResource(name, ofType: nil)
        if path != nil {
            do {
                return try String(contentsOfFile: path!)
            } catch {
                
            }
        }
        
        return nil
    }
    
    func loadBinAssetWithNSString(name: String!) -> IOSByteArray! {
        var path: String?
        path = NSBundle.mainBundle().pathForResource(name, ofType: nil)
        if path != nil {
            if let data = NSData(contentsOfFile: path!) {
                return data.toJavaBytes()
            }
        }
        
        path = NSBundle.framework.pathForResource(name, ofType: nil)
        if path != nil {
            if let data = NSData(contentsOfFile: path!) {
                return data.toJavaBytes()
            }
        }
        
        return nil
    }
}