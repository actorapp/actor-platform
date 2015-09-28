//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

// Base Range 

protocol ACManagedRange {
    
    // Total items count
    
    func rangeNumberOfItems(table: ACManagedTable) -> Int
    
    // Cell
    
    func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat
    
    func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell
    
    // Selection
    
    func rangeCanSelect(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool
    
    func rangeSelect(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool
    
    // Copying
    
    func rangeCanCopy(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool
    
    func rangeCopy(table: ACManagedTable, indexPath: ACRangeIndexPath)
}

// Default implementations of ACManagedRangeDelegate

extension ACManagedRange {
    
    func rangeCanSelect(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        // Do nothing
        return false
    }
    
    func rangeSelect(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        // Do nothing
        return false
    }
    
    func rangeCanCopy(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        // Do nothing
        return false
    }
    
    func rangeCopy(table: ACManagedTable, indexPath: ACRangeIndexPath) {
        // Do nothing
    }
}

class ACRangeIndexPath {
    
    let section: Int
    let range: Int
    let item: Int
    let indexPath: NSIndexPath
    
    init(section: Int, range: Int, item: Int, indexPath: NSIndexPath) {
        self.section = section
        self.range = range
        self.item = item
        self.indexPath = indexPath
    }
}