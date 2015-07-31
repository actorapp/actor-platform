//
//  CocoaThreadingProvider.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 27.07.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class CocoaThreadingProvider: AMCocoaThreadingProvider {
    
    override func createTimerWithJavaLangRunnable(runnable: JavaLangRunnable!) -> AMAbsTimerCompat! {
        return SwiftTimer(javaLangRunnable: runnable)
    }
}