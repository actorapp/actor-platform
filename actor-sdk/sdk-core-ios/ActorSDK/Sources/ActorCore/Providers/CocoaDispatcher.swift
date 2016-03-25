//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaDispatcher: NSObject, ARCocoaDispatcherProxy {
    
    func dispatchOnBackground(runnable: JavaLangRunnable!, withDelay delay: jlong) -> ARDispatchCancel! {
        dispatchBackgroundDelayed(Double(delay) / 1000) { () -> Void in
            runnable.run()
        }
        return DispatchCancel()
    }
}

private class DispatchCancel: NSObject, ARDispatchCancel {
    
    @objc private func cancel() {
        // Do Nothing
    }
}