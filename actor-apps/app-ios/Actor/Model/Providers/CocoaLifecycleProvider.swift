//
//  CocoaLifecycleProvider.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 02.06.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class CocoaLifecycleProvider: NSObject, AMLifecycleProvider {
    func killApp() {
        fatalError("Device is logged in")
    }
}