//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public func dispatchOnUi(closure: () -> Void) {
    dispatch_async(dispatch_get_main_queue(), { () -> Void in
        closure()
    })
}

public func dispatchBackground(closure: () -> Void) {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), { () -> Void in
        closure()
    })
}
