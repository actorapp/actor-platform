//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAManagedSection {
    
    open var headerHeight: Double = 0
    open var footerHeight: Double = 0
    
    open var headerText: String? = nil
    open var footerText: String? = nil
    
    open let index: Int
    
    open var autoSeparatorTopOffset: Int = 0
    open var autoSeparatorBottomOffset: Int = 0
    open var autoSeparators: Bool = false
    open var autoSeparatorsInset: CGFloat = 15.0
    
    open var regions: [AAManagedRange] = [AAManagedRange]()
    
    open unowned let table: AAManagedTable
    
    public init(table: AAManagedTable, index: Int) {
        self.index = index
        self.table = table
    }
    
    // Items count
    
    open func numberOfItems(_ managedTable: AAManagedTable) -> Int {
        var res = 0
        for r in regions {
            res += r.rangeNumberOfItems(managedTable)
        }
        return res
    }
    
    // Cells
    
    open func cellHeightForItem(_ managedTable: AAManagedTable, indexPath: IndexPath) -> CGFloat {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeCellHeightForItem(managedTable, indexPath: r.index)
    }
    
    open func cellForItem(_ managedTable: AAManagedTable, indexPath: IndexPath) -> UITableViewCell {
        let r = findCell(managedTable, indexPath: indexPath)
        
        let res = r.cells.rangeCellForItem(managedTable, indexPath: r.index)
        if autoSeparators {
            if let cell = res as? AATableViewCell {
                
                // Top separator
                
                if managedTable.style == .plain {
                    
                    // Don't show for plain style
                    cell.topSeparatorVisible = false
                } else {
                    
                    // Showing only for top cell
                    cell.topSeparatorLeftInset = 0
                    cell.topSeparatorVisible = (indexPath as NSIndexPath).row == autoSeparatorTopOffset
                }
                
                // Bottom separator
                
                if (indexPath as NSIndexPath).row >= autoSeparatorTopOffset {
                    cell.bottomSeparatorVisible = true
                    
                    if managedTable.style == .plain {
                        
                        // Don't make last full line separator
                        cell.bottomSeparatorLeftInset = autoSeparatorsInset
                    } else {
                        
                        if (indexPath as NSIndexPath).row == numberOfItems(managedTable) - 1 {
                            cell.bottomSeparatorLeftInset = 0
                        } else {
                            cell.bottomSeparatorLeftInset = autoSeparatorsInset
                        }
                    }
                } else {
                    
                    // Too high
                    cell.bottomSeparatorVisible = false
                }
                
            }
        }
        return res
    }

    // Selection
    
    func canSelect(_ managedTable: AAManagedTable, indexPath: IndexPath) -> Bool {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeCanSelect(managedTable, indexPath: r.index)
    }
    
    func select(_ managedTable: AAManagedTable, indexPath: IndexPath) -> Bool {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeSelect(managedTable, indexPath: r.index)
    }
    
    // Copying
    
    func canCopy(_ managedTable: AAManagedTable, indexPath: IndexPath) -> Bool {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeCanCopy(managedTable, indexPath: r.index)
    }
    
    func copy(_ managedTable: AAManagedTable, indexPath: IndexPath) {
        let r = findCell(managedTable, indexPath: indexPath)
        r.cells.rangeCopy(managedTable, indexPath: r.index)
    }
    
    // Deletion
    
    func canDelete(_ managedTable: AAManagedTable, indexPath: IndexPath) -> Bool {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeCanDelete(managedTable, indexPath: r.index)
    }
    
    func delete(_ managedTable: AAManagedTable, indexPath: IndexPath) {
        let r = findCell(managedTable, indexPath: indexPath)
        r.cells.rangeDelete(managedTable, indexPath: r.index)
    }

    
    // Binding
    
    func bind(_ managedTable: AAManagedTable, binder: AABinder) {
        for s in regions {
            s.rangeBind(managedTable, binder: binder)
        }
    }
    
    func unbind(_ managedTable: AAManagedTable, binder: AABinder) {
        for s in regions {
            s.rangeUnbind(managedTable, binder: binder)
        }
    }

    // Private Tools
    
    fileprivate func findCell(_ managedTable: AAManagedTable, indexPath: IndexPath) -> CellSearchResult {
        var prevLength = 0
        for i in 0..<regions.count {
            let r = regions[i]
            let itemsCount = r.rangeNumberOfItems(managedTable)
            if (prevLength <= (indexPath as NSIndexPath).row && (indexPath as NSIndexPath).row < prevLength + itemsCount) {
                return CellSearchResult(cells: r, index: AARangeIndexPath(section: (indexPath as NSIndexPath).section, range: i, item: (indexPath as NSIndexPath).row - prevLength, indexPath: indexPath))
            }
            prevLength += itemsCount
        }
        fatalError("Inconsistent cells")
    }
    
    fileprivate class CellSearchResult {
        
        let cells: AAManagedRange
        let index: AARangeIndexPath
        
        init(cells: AAManagedRange, index: AARangeIndexPath) {
            self.cells = cells
            self.index = index
        }
    }
}

// Obsolete Setters

public extension AAManagedSection {
    
    public func setSeparatorsTopOffset(_ offset: Int) -> AAManagedSection {
        self.autoSeparatorTopOffset = offset
        return self
    }
    
    public func setFooterText(_ footerText: String) -> AAManagedSection {
        self.footerText = footerText
        return self
    }
    
    public func setHeaderText(_ headerText: String) -> AAManagedSection {
        self.headerText = headerText
        return self
    }
    
    public func setFooterHeight(_ footerHeight: Double) -> AAManagedSection {
        self.footerHeight = footerHeight
        return self
    }
    
    public func setHeaderHeight(_ headerHeight: Double) -> AAManagedSection {
        self.headerHeight = headerHeight
        return self
    }
}

