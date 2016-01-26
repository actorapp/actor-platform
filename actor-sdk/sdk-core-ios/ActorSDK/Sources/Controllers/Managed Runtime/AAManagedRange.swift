//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

// Base Range 

public protocol AAManagedRange {
    
    // Total items count
    
    func rangeNumberOfItems(table: AAManagedTable) -> Int
    
    // Cell
    
    func rangeCellHeightForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat
    
    func rangeCellForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell
    
    // Selection
    
    func rangeCanSelect(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool
    
    func rangeSelect(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool
    
    // Copying
    
    func rangeCanCopy(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool
    
    func rangeCopy(table: AAManagedTable, indexPath: AARangeIndexPath)
    
    // Delete
    
    func rangeCanDelete(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool
    
    func rangeDelete(table: AAManagedTable, indexPath: AARangeIndexPath)
    
    // Binding
    
    func rangeBind(table: AAManagedTable, binder: AABinder)
    
    func rangeUnbind(table: AAManagedTable, binder: AABinder)
}

// Default implementations of ACManagedRangeDelegate

public extension AAManagedRange {
    
    public func rangeCanSelect(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        // Do nothing
        return false
    }
    
    public func rangeSelect(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        // Do nothing
        return false
    }
    
    public func rangeCanCopy(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        // Do nothing
        return false
    }
    
    public func rangeCopy(table: AAManagedTable, indexPath: AARangeIndexPath) {
        // Do nothing
    }
    
    public func rangeCanDelete(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        // Do nothing
        return false
    }
    
    public func rangeDelete(table: AAManagedTable, indexPath: AARangeIndexPath) {
        // Do nothing
    }
    
    public func rangeBind(table: AAManagedTable, binder: AABinder) {
        // Do nothing
    }
    
    public func rangeUnbind(table: AAManagedTable, binder: AABinder) {
        // Do nothing
    }
}

public class AARangeIndexPath {
    
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