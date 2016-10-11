//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

// Base Range 

public protocol AAManagedRange {
    
    // Initing Table
    
    func initTable(_ table: AAManagedTable)
    
    // Total items count
    
    func rangeNumberOfItems(_ table: AAManagedTable) -> Int
    
    // Cell
    
    func rangeCellHeightForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat
    
    func rangeCellForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell
    
    // Selection
    
    func rangeCanSelect(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool
    
    func rangeSelect(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool
    
    // Copying
    
    func rangeCanCopy(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool
    
    func rangeCopy(_ table: AAManagedTable, indexPath: AARangeIndexPath)
    
    // Delete
    
    func rangeCanDelete(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool
    
    func rangeDelete(_ table: AAManagedTable, indexPath: AARangeIndexPath)
    
    // Binding
    
    func rangeBind(_ table: AAManagedTable, binder: AABinder)
    
    func rangeUnbind(_ table: AAManagedTable, binder: AABinder)
}

// Default implementations of ACManagedRangeDelegate

public extension AAManagedRange {
    
    public func rangeCanSelect(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        // Do nothing
        return false
    }
    
    public func rangeSelect(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        // Do nothing
        return false
    }
    
    public func rangeCanCopy(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        // Do nothing
        return false
    }
    
    public func rangeCopy(_ table: AAManagedTable, indexPath: AARangeIndexPath) {
        // Do nothing
    }
    
    public func rangeCanDelete(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        // Do nothing
        return false
    }
    
    public func rangeDelete(_ table: AAManagedTable, indexPath: AARangeIndexPath) {
        // Do nothing
    }
    
    public func rangeBind(_ table: AAManagedTable, binder: AABinder) {
        // Do nothing
    }
    
    public func rangeUnbind(_ table: AAManagedTable, binder: AABinder) {
        // Do nothing
    }
}

open class AARangeIndexPath {
    
    open let section: Int
    open let range: Int
    open let item: Int
    open let indexPath: IndexPath
    
    public init(section: Int, range: Int, item: Int, indexPath: IndexPath) {
        self.section = section
        self.range = range
        self.item = item
        self.indexPath = indexPath
    }
}
