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
    }
}

class ListProcessor: NSObject, ARListProcessor {
    
    let layoutCache: LayoutCache
    let isGroup: Bool
    
    init(layoutCache: LayoutCache, isGroup: Bool) {
        self.layoutCache = layoutCache
        self.isGroup = isGroup
    }
    
    func processWithJavaUtilList(items: JavaUtilList!) -> AnyObject! {
    
        var objs = [ACMessage]()
        for i in 0..<items.size() {
            objs.append(items.getWithInt(i) as! ACMessage)
        }
        
        var settings = [CellSetting]()
        for i in 0..<objs.count {
            settings.append(buildCellSetting(i, items: objs))
        }
        
        var layouts = [CellLayout]()
        for i in 0..<objs.count {
            layouts.append(MessagesLayouting.buildLayout(objs[i], layoutCache: layoutCache))
        }
        
        var heights = [CGFloat]()
        for i in 0..<objs.count {
            heights.append(buildHeight(i, items: objs, settings: settings))
        }
    
        var res = PreprocessedList()
        res.items = objs
        res.cellSettings = settings
        res.layouts = layouts
        res.layoutCache = layoutCache
        res.heights = heights
        return res
    }
    
    func buildCellSetting(index: Int, items: [ACMessage]) -> CellSetting {
        
        var current = items[index]
        var next: ACMessage! = index > 0 ? items[index - 1] : nil
        var prev: ACMessage! = index + 1 < items.count ? items[index + 1] : nil
        
        var isShowDate = true
        var isShowDateNext = true
        var isShowNewMessages = false//(unreadMessageId == current.rid)
        var clenchTop = false
        var clenchBottom = false
        
        if (prev != nil) {
            isShowDate = !areSameDate(current, prev: prev)
            if !isShowDate {
                clenchTop = useCompact(current, next: prev)
            }
        }
        
        if (next != nil) {
            if areSameDate(next, prev: current) {
                clenchBottom = useCompact(current, next: next)
            }
        }
        
        return CellSetting(showDate: isShowDate, clenchTop: clenchTop, clenchBottom: clenchBottom, showNewMessages: isShowNewMessages)
    }
    
    func areSameDate(source:ACMessage, prev: ACMessage) -> Bool {
        let calendar = NSCalendar.currentCalendar()
        
        var currentDate = NSDate(timeIntervalSince1970: Double(source.date)/1000.0)
        var currentDateComp = calendar.components(.CalendarUnitDay | .CalendarUnitYear | .CalendarUnitMonth, fromDate: currentDate)
        
        var nextDate = NSDate(timeIntervalSince1970: Double(prev.date)/1000.0)
        var nextDateComp = calendar.components(.CalendarUnitDay | .CalendarUnitYear | .CalendarUnitMonth, fromDate: nextDate)
        
        return (currentDateComp.year == nextDateComp.year && currentDateComp.month == nextDateComp.month && currentDateComp.day == nextDateComp.day)
    }
    
    func useCompact(source: ACMessage, next: ACMessage) -> Bool {
        if (source.content is ACServiceContent) {
            if (next.content is ACServiceContent) {
                return true
            }
        } else {
            if (next.content is ACServiceContent) {
                return false
            }
            if (source.senderId == next.senderId) {
                return true
            }
        }
        
        return false
    }
    
    func buildHeight(index: Int, items: [ACMessage], settings: [CellSetting]) -> CGFloat {
        var message = items[index]
        var setting = settings[index]
        return MessagesLayouting.measureHeight(message, group: isGroup, setting: setting, layoutCache: layoutCache)
    }
}

@objc class PreprocessedList {
    var items: [ACMessage]!
    var cellSettings: [CellSetting]!
    var layouts: [CellLayout]!
    var layoutCache: LayoutCache!
    var heights: [CGFloat]!
}