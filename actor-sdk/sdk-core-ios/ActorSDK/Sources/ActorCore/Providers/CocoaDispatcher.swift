//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaDispatcher: NSObject, ARCocoaDispatcherProxy {
    
    private let queue = YYDispatchQueueGetForQOS(NSQualityOfService.Background)
    
    func dispatchOnBackgroundWithJavaLangRunnable(runnable: JavaLangRunnable!, withLong delay: jlong) {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, Int64(Double(delay) * Double(NSEC_PER_MSEC))), queue) {
            runnable.run()
        }
    }
}