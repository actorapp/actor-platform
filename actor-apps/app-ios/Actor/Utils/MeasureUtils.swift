//
//  MeasureUtils.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 27.07.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

private var startTime: NSDate!

func startMeasure() {
    startTime = NSDate()
}

func trackMeasure(name: String) {
    if startTime == nil {
        return
    }
    let timeInterval = Int(NSDate().timeIntervalSinceDate(startTime) * 1000)
    startTime = NSDate()
    log("Measured \(name) in \(timeInterval) ms")
}

func stopMeasure() {
    startTime = nil
}