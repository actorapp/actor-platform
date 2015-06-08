//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaMainThreadProvider:NSObject, AMMainThreadProvider {

    func postToMainThreadWithRunnable(runnable: JavaLangRunnable!) {
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