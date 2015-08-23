//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

//class BubbleBackgroundProcessor: NSObject, ARBackgroundProcessor {
//    
//    let layoutCache: LayoutCache = LayoutCache()
//    
//    func processInBackgroundWithId(item: AnyObject!) {
//        var message = item as! ACMessage
//        
//        Actor.getUserWithUid(message.senderId)
//        
//        var cached = layoutCache.pick(message.rid)
//        if (cached != nil) {
//            return
//        }
//        
//        println("process \(message.rid)")
//        var layout = MessagesLayouting.buildLayout(message, layoutCache: layoutCache)
//        self.layoutCache.cache(message.rid, layout: layout)
//    }
//}

class ListProcessor: NSObject, ARListProcessor {
    
    let layoutCache: LayoutCache = LayoutCache()
    let isGroup: Bool
    
    init(isGroup: Bool) {
        self.isGroup = isGroup
    }
    
    func buildLayout(message: ACMessage) {
        Actor.getUserWithUid(message.senderId)
        
        var cached = layoutCache.pick(message.rid)
        if (cached != nil) {
            return
        }
        
        var layout = MessagesLayouting.buildLayout(message, layoutCache: layoutCache)
        self.layoutCache.cache(message.rid, layout: layout)
    }
    
    func processWithItems(items: JavaUtilList!, withPrevious previous: AnyObject!) -> AnyObject! {
        var objs = [ACMessage]()
        var indexes = [jlong: Int]()
        for i in 0..<items.size() {
            var msg = items.getWithInt(i) as! ACMessage
            indexes.updateValue(Int(i), forKey: msg.rid)
            objs.append(msg)
        }
        
        var settings = [CellSetting]()
        for i in 0..<objs.count {
            settings.append(buildCellSetting(i, items: objs))
        }
        
        var layouts = [CellLayout]()
        for i in 0..<objs.count {
            layouts.append(MessagesLayouting.buildLayout(objs[i], layoutCache: layoutCache))
        }
        
        var forceUpdates = [Bool]()
        var updates = [Bool]()
        if let prevList = previous as? PreprocessedList {
            for i in 0..<objs.count {
                var obj = objs[i]
                var oldIndex = prevList.indexMap[obj.rid]
                if oldIndex != nil {
                    var oldSetting = prevList.cellSettings[oldIndex!]
                    var setting = settings[i]
                    
                    if setting.clenchTop != oldSetting.clenchTop || setting.clenchBottom != oldSetting.clenchBottom || setting.showDate != oldSetting.showDate {
                        if setting.showDate != oldSetting.showDate {
                            forceUpdates.append(true)
                            updates.append(false)
                        } else {
                            forceUpdates.append(false)
                            updates.append(true)
                        }
                    } else {
                        forceUpdates.append(false)
                        updates.append(false)
                    }
                } else {
                    forceUpdates.append(false)
                    updates.append(false)
                }
            }
        } else {
            for i in 0..<objs.count {
                forceUpdates.append(false)
                updates.append(false)
            }
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
        res.indexMap = indexes
        res.forceUpdated = forceUpdates
        res.updated = updates
        return res
    }
    
    func buildCellSetting(index: Int, items: [ACMessage]) -> CellSetting {
        
        var current = items[index]
        var next: ACMessage! = index > 0 ? items[index - 1] : nil
        var prev: ACMessage! = index + 1 < items.count ? items[index + 1] : nil
        
        var isShowDate = true
        var isShowDateNext = true
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
        
        return CellSetting(showDate: isShowDate, clenchTop: clenchTop, clenchBottom: clenchBottom)
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
    var forceUpdated: [Bool]!
    var updated: [Bool]!
    var indexMap: [jlong: Int]!
}