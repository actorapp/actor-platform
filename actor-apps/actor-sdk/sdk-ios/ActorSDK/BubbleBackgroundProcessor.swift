//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

private let ENABLE_LOGS = false

/**
    Display list preprocessed list state
*/
class PreprocessedList: NSObject {

    // Array of items in list
    var items: [ACMessage]!

    // Map from rid to index of item
    var indexMap: [jlong: Int]!
    
    // Current settings of cell look and feel
    var cellSettings: [CellSetting]!
    
    // Prepared layouts of cells
    var layouts: [CellLayout]!
    
    // Height of item
    var heights: [CGFloat]!
    
    // Is item need to be force-udpated
    var forceUpdated: [Bool]!
    
    // Is item need to be soft-updated
    var updated: [Bool]!
}

/**
    Display list preprocessor. Used for performing all required calculations
    before collection is updated
*/
class ListProcessor: NSObject, ARListProcessor {
    
    let layoutCache: LayoutCache = LayoutCache()
    
    let settingsCache = Cache<CachedSetting>()
    
    let peer: ACPeer
    
    init(peer: ACPeer) {
        self.peer = peer
    }
    
    func processWithItems(items: JavaUtilList!, withPrevious previous: AnyObject!) -> AnyObject! {
        
        let start = CFAbsoluteTimeGetCurrent()
        var section = start
        
        // Building content list and dictionary from rid to index list
        var objs = [ACMessage]()
        var indexes = [jlong: Int]()
        for i in 0..<items.size() {
            let msg = items.getWithInt(i) as! ACMessage
            indexes.updateValue(Int(i), forKey: msg.rid)
            objs.append(msg)
        }
        
        if ENABLE_LOGS { log("processing(items): \(CFAbsoluteTimeGetCurrent() - section)") }
        section = CFAbsoluteTimeGetCurrent()
        
        // Calculating cell settings
        // TODO: Cache and avoid recalculation of whole list
        var settings = [CellSetting]()
        for i in 0..<objs.count {
            settings.append(buildCellSetting(i, items: objs))
        }
        
        if ENABLE_LOGS { log("processing(settings): \(CFAbsoluteTimeGetCurrent() - section)") }
        section = CFAbsoluteTimeGetCurrent()
        
        // Building cell layouts
        var layouts = [CellLayout]()
        for i in 0..<objs.count {
            layouts.append(buildLayout(objs[i], layoutCache: layoutCache))
        }
        
        if ENABLE_LOGS { log("processing(layouts): \(CFAbsoluteTimeGetCurrent() - section)") }
        section = CFAbsoluteTimeGetCurrent()
        
        // Calculating force and simple updates
        var forceUpdates = [Bool]()
        var updates = [Bool]()
        if let prevList = previous as? PreprocessedList {
            for i in 0..<objs.count {
                var isForced = false
                var isUpdated = false
                
                let obj = objs[i]
                let oldIndex: Int! = prevList.indexMap[obj.rid]
                if oldIndex != nil {
                    
                    // Check if layout keys are same
                    // If text was replaced by media it might force-updated
                    // If size of bubble changed you might to change layout key
                    // to update it's size
                    // TODO: In the future releases it might be implemented
                    // in more flexible way
                    if prevList.layouts[oldIndex].key != layouts[i].key {
                        
                        // Mark as forced update
                        isForced = true
                        
                        // Hack for rewriting layout information
                        // Removing layout from cache
                        layoutCache.revoke(objs[i].rid)
                        // Building new layout
                        layouts[i] = buildLayout(objs[i], layoutCache: layoutCache)
                        
                    } else {
                        
                        // Otherwise check bubble settings to check
                        // if it is changed and we need to recalculate size
                        let oldSetting = prevList.cellSettings[oldIndex]
                        let setting = settings[i]
                    
                        if setting != oldSetting {
                            if setting.showDate != oldSetting.showDate {
                                
                                // Date separator change size so make cell for resize
                                isForced = true
                            } else {
                                
                                // Other changes doesn't change size, so just update content
                                // without resizing
                                isUpdated = true
                            }
                        }
                    }
                }
                
                // Saving update state value
                if isForced {
                    forceUpdates.append(true)
                    updates.append(false)
                } else if isUpdated {
                    forceUpdates.append(false)
                    updates.append(true)
                } else {
                    forceUpdates.append(false)
                    updates.append(false)
                }
            }
        } else {
            for _ in 0..<objs.count {
                forceUpdates.append(false)
                updates.append(false)
            }
        }
        
        if ENABLE_LOGS { log("processing(updates): \(CFAbsoluteTimeGetCurrent() - section)") }
        section = CFAbsoluteTimeGetCurrent()
        
        // Updating cell heights
        // TODO: Need to implement width calculations too
        //       to make bubble menu appear at right place
        var heights = [CGFloat]()
        for i in 0..<objs.count {
            let height = measureHeight(objs[i], setting: settings[i], layout: layouts[i])
            heights.append(height)
        }
        
        if ENABLE_LOGS { log("processing(heights): \(CFAbsoluteTimeGetCurrent() - section)") }
        
        if ENABLE_LOGS { log("processing(all): \(CFAbsoluteTimeGetCurrent() - start)") }
        
        // Put everything together
        let res = PreprocessedList()
        res.items = objs
        res.cellSettings = settings
        res.layouts = layouts
        res.heights = heights
        res.indexMap = indexes
        res.forceUpdated = forceUpdates
        res.updated = updates
        return res
    }
    
    /**
        Building Cell Setting: Information about decorators like date separator 
        and clenching of bubbles
    */
    func buildCellSetting(index: Int, items: [ACMessage]) -> CellSetting {
        
        let current = items[index]
        let id = Int64(current.rid)
        let next: ACMessage! = index > 0 ? items[index - 1] : nil
        let prev: ACMessage! = index + 1 < items.count ? items[index + 1] : nil
        
        let cached: CachedSetting! = settingsCache.pick(id)
        if cached != nil {
            if cached.isValid(prev, next: next) {
                return cached.cached
            }
        }
        
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
        
        let res = CellSetting(showDate: isShowDate, clenchTop: clenchTop, clenchBottom: clenchBottom)
        
        settingsCache.cache(id, value: CachedSetting(cached: res, prevId: prev?.rid, nextId: next?.rid))
        
        return res
    }
    
    /**
        Checking if messages have same send day
    */
    func areSameDate(source:ACMessage, prev: ACMessage) -> Bool {
        let calendar = NSCalendar.currentCalendar()
        
        let currentDate = NSDate(timeIntervalSince1970: Double(source.date)/1000.0)
        let currentDateComp = calendar.components([.Day, .Year, .Month], fromDate: currentDate)
        
        let nextDate = NSDate(timeIntervalSince1970: Double(prev.date)/1000.0)
        let nextDateComp = calendar.components([.Day, .Year, .Month], fromDate: nextDate)
        
        return (currentDateComp.year == nextDateComp.year && currentDateComp.month == nextDateComp.month && currentDateComp.day == nextDateComp.day)
    }
    
    /**
        Checking if it is good to make bubbles clenched
    */
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
    
    func measureHeight(message: ACMessage, setting: CellSetting, layout: CellLayout) -> CGFloat {
        
        let content = message.content!
        
        var height = layout.height
        if content is ACServiceContent {
            height += AABubbleCell.bubbleTop
            height += AABubbleCell.bubbleBottom
        } else {
            height += (setting.clenchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop)
            height += (setting.clenchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom)
        }
        
        // Date separator
        if (setting.showDate) {
            height += AABubbleCell.dateSize
        }
        
        return height
    }
    
    func buildLayout(message: ACMessage, layoutCache: LayoutCache) -> CellLayout {
        
        var layout: CellLayout! = layoutCache.pick(message.rid)
        
        if (layout == nil) {
            // Usually never happens
            layout = Bubbles.buildLayout(peer, message: message)
            layoutCache.cache(message.rid, value: layout!)
        }
        
        return layout
    }
}