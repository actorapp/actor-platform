//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class BubbleBackgroundProcessor: NSObject, ARBackgroundProcessor {
    
    let layoutCache: LayoutCache = LayoutCache()
    
    func processInBackgroundWithId(item: AnyObject!) {
        var message = item as! ACMessage
        
        Actor.getUserWithUid(message.getSenderId())
        
        var cached = layoutCache.pick(message.getRid())
        if (cached != nil) {
            return
        }
        
        // println("process \(message.getRid())")
        var layout = MessagesLayouting.buildLayout(message, layoutCache: layoutCache)
        
        dispatch_async(dispatch_get_main_queue(), { () -> Void in
            self.layoutCache.cache(message.getRid(), layout: layout)
        })
    }
}