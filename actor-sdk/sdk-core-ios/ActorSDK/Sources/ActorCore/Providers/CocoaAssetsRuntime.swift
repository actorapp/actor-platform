//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaAssetsRuntime: NSObject, ARAssetsRuntime {

    func hasAsset(with name: String!) -> jboolean {
        if Bundle.main.path(forResource: name, ofType: nil) != nil {
            return true
        }
        if Bundle.framework.path(forResource: name, ofType: nil) != nil {
            return true
        }
        return false
    }
    
    func loadAsset(with name: String!) -> String! {
        var path: String?
        path = Bundle.main.path(forResource: name, ofType: nil)
        if path != nil {
            do {
                return try String(contentsOfFile: path!)
            } catch {
                
            }
        }
        
        path = Bundle.framework.path(forResource: name, ofType: nil)
        if path != nil {
            do {
                return try String(contentsOfFile: path!)
            } catch {
                
            }
        }
        
        return nil
    }
    
    func loadBinAsset(with name: String!) -> IOSByteArray! {
        var path: String?
        path = Bundle.main.path(forResource: name, ofType: nil)
        if path != nil {
            if let data = try? Data(contentsOf: URL(fileURLWithPath: path!)) {
                return data.toJavaBytes()
            }
        }
        
        path = Bundle.framework.path(forResource: name, ofType: nil)
        if path != nil {
            if let data = try? Data(contentsOf: URL(fileURLWithPath: path!)) {
                return data.toJavaBytes()
            }
        }
        
        return nil
    }
}
