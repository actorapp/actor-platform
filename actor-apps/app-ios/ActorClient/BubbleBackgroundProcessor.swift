//
//  MessagesBackgroundProcessor.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 26.06.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class BubbleBackgroundProcessor: NSObject, AMBackgroundProcessor {
    
    let layoutCache: LayoutCache = LayoutCache()
    
    func processInBackgroundWithId(item: AnyObject!) {
        var message = item as! AMMessage
        
        MSG.getUserWithUid(message.getSenderId())
        
        var cached = layoutCache.pick(message.getRid())
        if (cached != nil) {
            return
        }
        
        println("process \(message.getRid())")
        var layout = MessagesLayouting.buildLayout(message, layoutCache: layoutCache)
        
        dispatch_async(dispatch_get_main_queue(), { () -> Void in
            self.layoutCache.cache(message.getRid(), layout: layout)
        })
    }
}