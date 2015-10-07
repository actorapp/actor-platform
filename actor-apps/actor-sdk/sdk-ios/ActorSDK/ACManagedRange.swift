//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

// Base Range 

public protocol ACManagedRange {
    
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
    
    // Delete
    
    func rangeCanDelete(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool
    
    func rangeDelete(table: ACManagedTable, indexPath: ACRangeIndexPath)
    
    // Binding
    
    func rangeBind(table: ACManagedTable, binder: Binder)
    
    func rangeUnbind(table: ACManagedTable, binder: Binder)
}

// Default implementations of ACManagedRangeDelegate

public extension ACManagedRange {
    
    public func rangeCanSelect(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        // Do nothing
        return false
    }
    
    public func rangeSelect(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        // Do nothing
        return false
    }
    
    public func rangeCanCopy(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        // Do nothing
        return false
    }
    
    public func rangeCopy(table: ACManagedTable, indexPath: ACRangeIndexPath) {
        // Do nothing
    }
    
    public func rangeCanDelete(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        // Do nothing
        return false
    }
    
    public func rangeDelete(table: ACManagedTable, indexPath: ACRangeIndexPath) {
        // Do nothing
    }
    
    public func rangeBind(table: ACManagedTable, binder: Binder) {
        // Do nothing
    }
    
    public func rangeUnbind(table: ACManagedTable, binder: Binder) {
        // Do nothing
    }
}

public class ACRangeIndexPath {
    
    public let section: Int
    public let range: Int
    public let item: Int
    public let indexPath: NSIndexPath
    
    public init(section: Int, range: Int, item: Int, indexPath: NSIndexPath) {
        self.section = section
        self.range = range
        self.item = item
        self.indexPath = indexPath
    }
}