//
//  CocoaLogProvider.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 15.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

@objc class CocoaLogProvider : NSObject, AMLogProvider {
    
    func w(tag: String!, withMessage message: String!) {
        NSLog("⚠️ %@: %@", tag, message);
    }
    
    func v(tag: String!, withError throwable: JavaLangThrowable!) {
        NSLog("🔴 %@: %@", tag, throwable);
    }
    
    func v(tag: String!, withMessage message: String!) {
        NSLog("[V] %@: %@", tag, message);
    }
    
    func d(tag: String!, withMessage message: String!) {
        NSLog("[D] %@: %@", tag, message);
    }
}