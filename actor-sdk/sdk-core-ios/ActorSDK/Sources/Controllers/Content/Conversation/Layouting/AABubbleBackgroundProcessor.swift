//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

private let ENABLE_LOGS = false

/**
    Display list preprocessed list state
*/
class AAPreprocessedList: NSObject {

    // Array of items in list
    var items: [ACMessage]!

    // Map from rid to index of item
    var indexMap: [jlong: Int]!
    
    // Current settings of cell look and feel
    var cellSettings: [AACellSetting]!
    
    // Prepared layouts of cells
    var layouts: [AACellLayout]!
    
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
class AAListProcessor: NSObject, ARListProcessor {
    
    let layoutCache = AALayoutCache()
    
    let settingsCache = AACache<AACachedSetting>()
    
    let peer: ACPeer
    
    init(peer: ACPeer) {
        self.peer = peer
    }
    
    func process(withItems items: JavaUtilList, withPrevious previous: Any?) -> Any? {
        
        var objs = [ACMessage]()
        var indexes = [jlong: Int]()
        var layouts = [AACellLayout]()
        var settings = [AACellSetting]()
        var heights = [CGFloat]()
        var forceUpdates = [Bool]()
        var updates = [Bool]()
        
        autoreleasepool {
            
            let start = CFAbsoluteTimeGetCurrent()
            var section = start
            
            // Capacity
            objs.reserveCapacity(Int(items.size()))
            layouts.reserveCapacity(Int(items.size()))
            settings.reserveCapacity(Int(items.size()))
            heights.reserveCapacity(Int(items.size()))
            forceUpdates.reserveCapacity(Int(items.size()))
            updates.reserveCapacity(Int(items.size()))
            
            // Building content list and dictionary from rid to index list
            for i in 0..<items.size() {
                let msg = items.getWith(i) as! ACMessage
                indexes.updateValue(Int(i), forKey: msg.rid)
                objs.append(msg)
            }
            
            if ENABLE_LOGS { log("processing(items): \(CFAbsoluteTimeGetCurrent() - section)") }
            section = CFAbsoluteTimeGetCurrent()
            
            // Calculating cell settings
            // TODO: Cache and avoid recalculation of whole list
            for i in 0..<objs.count {
                settings.append(buildCellSetting(i, items: objs))
            }
            
            if ENABLE_LOGS { log("processing(settings): \(CFAbsoluteTimeGetCurrent() - section)") }
            section = CFAbsoluteTimeGetCurrent()
            
            // Building cell layouts
            for i in 0..<objs.count {
                layouts.append(buildLayout(objs[i], layoutCache: layoutCache))
            }
            
            if ENABLE_LOGS { log("processing(layouts): \(CFAbsoluteTimeGetCurrent() - section)") }
            section = CFAbsoluteTimeGetCurrent()
            
            // Calculating force and simple updates
            if let prevList = previous as? AAPreprocessedList {
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
            for i in 0..<objs.count {
                let height = measureHeight(objs[i], setting: settings[i], layout: layouts[i])
                heights.append(height)
            }
            
            if ENABLE_LOGS { log("processing(heights): \(CFAbsoluteTimeGetCurrent() - section)") }
            
            if ENABLE_LOGS { log("processing(all): \(CFAbsoluteTimeGetCurrent() - start)") }
        }
        
        // Put everything together
        let res = AAPreprocessedList()
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
    func buildCellSetting(_ index: Int, items: [ACMessage]) -> AACellSetting {
        
        let current = items[index]
        let id = Int64(current.rid)
        let next: ACMessage! = index > 0 ? items[index - 1] : nil
        let prev: ACMessage! = index + 1 < items.count ? items[index + 1] : nil
        
        let cached: AACachedSetting! = settingsCache.pick(id)
        if cached != nil {
            if cached.isValid(prev, next: next) {
                return cached.cached
            }
        }
        
        var isShowDate = true
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
        
        let res = AACellSetting(showDate: isShowDate, clenchTop: clenchTop, clenchBottom: clenchBottom)
        
        settingsCache.cache(id, value: AACachedSetting(cached: res, prevId: prev?.rid, nextId: next?.rid))
        
        return res
    }
    
    /**
        Checking if messages have same send day
    */
    func areSameDate(_ source:ACMessage, prev: ACMessage) -> Bool {
        let calendar = Calendar.current
        
        let currentDate = Date(timeIntervalSince1970: Double(source.date)/1000.0)
        let currentDateComp = (calendar as NSCalendar).components([.day, .year, .month], from: currentDate)
        
        let nextDate = Date(timeIntervalSince1970: Double(prev.date)/1000.0)
        let nextDateComp = (calendar as NSCalendar).components([.day, .year, .month], from: nextDate)
        
        return (currentDateComp.year == nextDateComp.year && currentDateComp.month == nextDateComp.month && currentDateComp.day == nextDateComp.day)
    }
    
    /**
        Checking if it is good to make bubbles clenched
    */
    func useCompact(_ source: ACMessage, next: ACMessage) -> Bool {
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
    
    func measureHeight(_ message: ACMessage, setting: AACellSetting, layout: AACellLayout) -> CGFloat {
        
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
    
    func buildLayout(_ message: ACMessage, layoutCache: AALayoutCache) -> AACellLayout {
        
        var layout: AACellLayout! = layoutCache.pick(message.rid)
        
        if (layout == nil) {
            // Usually never happens
            layout = AABubbles.buildLayout(peer, message: message)
            layoutCache.cache(message.rid, value: layout!)
        }
        
        return layout
    }
}
