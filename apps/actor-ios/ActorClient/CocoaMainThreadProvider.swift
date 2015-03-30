//
//  CocoaMainThreadProvider.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 15.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class CocoaMainThreadProvider:NSObject, AMMainThreadProvider {
    func runOnUiThread(runnable: JavaLangRunnable!) {
        dispatch_async(dispatch_get_main_queue(), {
           runnable.run()
        });
    }
    
    func isMainThread() -> Bool {
        return NSThread.currentThread().isMainThread
    }
    
    func isSingleThread() -> Bool {
        return false
    }
}