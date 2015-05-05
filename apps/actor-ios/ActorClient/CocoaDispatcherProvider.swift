//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation
class CocoaDispatcherProvider: NSObject, AMDispatcherProvider {
    func dispatchWithJavaLangRunnable(runnable: JavaLangRunnable!) {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), {
            runnable.run()
        });
    }
}