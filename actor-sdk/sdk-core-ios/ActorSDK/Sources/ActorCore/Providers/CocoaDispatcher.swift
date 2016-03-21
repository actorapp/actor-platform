//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaDispatcher: NSObject, ARCocoaDispatcherProxy {
    
    func dispatchOnBackgroundWithJavaLangRunnable(runnable: JavaLangRunnable!, withLong delay: jlong) {
        dispatchBackgroundDelayed(Double(delay) / 1000) { () -> Void in
            runnable.run()
        }
    }
}