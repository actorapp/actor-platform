//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public class ACManagedSection {
    
    public var headerHeight: Double = 0
    public var footerHeight: Double = 0
    
    public var headerText: String? = nil
    public var footerText: String? = nil
    
    public let index: Int
    
    public var autoSeparatorTopOffset: Int = 0
    public var autoSeparatorBottomOffset: Int = 0
    public var autoSeparators: Bool = false
    public var autoSeparatorsInset: CGFloat = 15.0
    
    public var regions: [ACManagedRange] = [ACManagedRange]()
    
    public unowned let table: ACManagedTable
    
    public init(table: ACManagedTable, index: Int) {
        self.index = index
        self.table = table
    }
    
    // Items count
    
    public func numberOfItems(managedTable: ACManagedTable) -> Int {
        var res = 0
        for r in regions {
            res += r.rangeNumberOfItems(managedTable)
        }
        return res
    }
    
    // Cells
    
    public func cellHeightForItem(managedTable: ACManagedTable, indexPath: NSIndexPath) -> CGFloat {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeCellHeightForItem(managedTable, indexPath: r.index)
    }
    
    public func cellForItem(managedTable: ACManagedTable, indexPath: NSIndexPath) -> UITableViewCell {
        let r = findCell(managedTable, indexPath: indexPath)
        
        let res = r.cells.rangeCellForItem(managedTable, indexPath: r.index)
        if autoSeparators {
            if let cell = res as? UATableViewCell {
                
                // Top separator
                
                if managedTable.style == .Plain {
                    
                    // Don't show for plain style
                    cell.topSeparatorVisible = false
                } else {
                    
                    // Showing only for top cell
                    cell.topSeparatorLeftInset = 0
                    cell.topSeparatorVisible = indexPath.row == autoSeparatorTopOffset
                }
                
                // Bottom separator
                
                if indexPath.row >= autoSeparatorTopOffset {
                    cell.bottomSeparatorVisible = true
                    
                    if managedTable.style == .Plain {
                        
                        // Don't make last full line separator
                        cell.bottomSeparatorLeftInset = autoSeparatorsInset
                    } else {
                        
                        if indexPath.row == numberOfItems(managedTable) - 1 {
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
    
    public func canSelect(managedTable: ACManagedTable, indexPath: NSIndexPath) -> Bool {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeCanSelect(managedTable, indexPath: r.index)
    }
    
    public func select(managedTable: ACManagedTable, indexPath: NSIndexPath) -> Bool {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeSelect(managedTable, indexPath: r.index)
    }
    
    // Copying
    
    public func canCopy(managedTable: ACManagedTable, indexPath: NSIndexPath) -> Bool {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeCanCopy(managedTable, indexPath: r.index)
    }
    
    public func copy(managedTable: ACManagedTable, indexPath: NSIndexPath) {
        let r = findCell(managedTable, indexPath: indexPath)
        r.cells.rangeCopy(managedTable, indexPath: r.index)
    }
    
    // Deletion
    
    public func canDelete(managedTable: ACManagedTable, indexPath: NSIndexPath) -> Bool {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeCanDelete(managedTable, indexPath: r.index)
    }
    
    public func delete(managedTable: ACManagedTable, indexPath: NSIndexPath) {
        let r = findCell(managedTable, indexPath: indexPath)
        r.cells.rangeDelete(managedTable, indexPath: r.index)
    }

    
    // Binding
    
    public func bind(managedTable: ACManagedTable, binder: Binder) {
        for s in regions {
            s.rangeBind(managedTable, binder: binder)
        }
    }
    
    public func unbind(managedTable: ACManagedTable, binder: Binder) {
        for s in regions {
            s.rangeUnbind(managedTable, binder: binder)
        }
    }

    // Private Tools
    
    private func findCell(managedTable: ACManagedTable, indexPath: NSIndexPath) -> CellSearchResult {
        var prevLength = 0
        for i in 0..<regions.count {
            let r = regions[i]
            let itemsCount = r.rangeNumberOfItems(managedTable)
            if (prevLength <= indexPath.row && indexPath.row < prevLength + itemsCount) {
                return CellSearchResult(cells: r, index: ACRangeIndexPath(section: indexPath.section, range: i, item: indexPath.row - prevLength, indexPath: indexPath))
            }
            prevLength += itemsCount
        }
        fatalError("Inconsistent cells")
    }
    
    private class CellSearchResult {
        
        let cells: ACManagedRange
        let index: ACRangeIndexPath
        
        init(cells: ACManagedRange, index: ACRangeIndexPath) {
            self.cells = cells
            self.index = index
        }
    }
}

// Obsolete Setters

public extension ACManagedSection {
    
    public func setSeparatorsTopOffset(offset: Int) -> ACManagedSection {
        self.autoSeparatorTopOffset = offset
        return self
    }
    
    public func setFooterText(footerText: String) -> ACManagedSection {
        self.footerText = footerText
        return self
    }
    
    public func setHeaderText(headerText: String) -> ACManagedSection {
        self.headerText = headerText
        return self
    }
    
    public func setFooterHeight(footerHeight: Double) -> ACManagedSection {
        self.footerHeight = footerHeight
        return self
    }
    
    public func setHeaderHeight(headerHeight: Double) -> ACManagedSection {
        self.headerHeight = headerHeight
        return self
    }
}

