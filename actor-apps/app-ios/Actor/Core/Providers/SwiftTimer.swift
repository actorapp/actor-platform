////
////  SwiftTimer.swift
////  ActorApp
////
////  Created by Stepan Korshakov on 27.07.15.
////  Copyright (c) 2015 Actor LLC. All rights reserved.
////
//
//import Foundation
//
//class SwiftTimer: ARAbsTimerCompat {
//    
//    private var cancellable: cancellable_closure
//    
//    override init!(javaLangRunnable runnable: JavaLangRunnable!) {
//        super.init(javaLangRunnable: runnable)
//    }
//
//    override func cancel() {
//        cancellable?()
//        cancellable = nil
//    }
//    
//    override func scheduleWithLong(delay: jlong) {
//        cancellable = dispatch_after(seconds: Double(delay)/1000.0, queue: dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0)) { () -> () in
//            self.invokeRun()
//            self.cancellable = nil
//        }
//    }
//}