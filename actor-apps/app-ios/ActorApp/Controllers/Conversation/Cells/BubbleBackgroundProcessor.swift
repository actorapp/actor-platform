//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class BubbleBackgroundProcessor: NSObject, ARBackgroundProcessor {
    
    let layoutCache: LayoutCache = LayoutCache()
    
    func processInBackgroundWithId(item: AnyObject!) {
        var message = item as! ACMessage
        
        Actor.getUserWithUid(message.senderId)
        
        var cached = layoutCache.pick(message.rid)
        if (cached != nil) {
            return
        }
        
        println("process \(message.rid)")
        var layout = MessagesLayouting.buildLayout(message, layoutCache: layoutCache)
        self.layoutCache.cache(message.rid, layout: layout)
        
//        dispatchOnUi { () -> Void in
//            println("put to cache \(message.getRid())")
//            self.layoutCache.cache(message.getRid(), layout: layout)
//        }
    }
}