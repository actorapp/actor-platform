//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

@objc class CocoaLogProvider : NSObject, AMLogProvider {
    
    func warringWithTag(tag: String!, withMessage message: String!) {
        NSLog("⚠️ %@: %@", tag, message);
    }
    
    func verboseWithTag(tag: String!, withMessage message: String!) {
        NSLog("[V] %@: %@", tag, message);
    }
    
    func errorWithTag(tag: String!, withThrowable throwable: JavaLangThrowable!) {
        NSLog("🔴 %@: %@", tag, throwable);
    }
    
    func debugWithTag(tag: String!, withMessage message: String!) {
        NSLog("[D] %@: %@", tag, message);
    }
}