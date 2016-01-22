//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAManagedSection {
    
    public var headerHeight: Double = 0
    public var footerHeight: Double = 0
    
    public var headerText: String? = nil
    public var footerText: String? = nil
    
    public let index: Int
    
    public var autoSeparatorTopOffset: Int = 0
    public var autoSeparatorBottomOffset: Int = 0
    public var autoSeparators: Bool = false
    public var autoSeparatorsInset: CGFloat = 15.0
    
    public var regions: [AAManagedRange] = [AAManagedRange]()
    
    public unowned let table: AAManagedTable
    
    public init(table: AAManagedTable, index: Int) {
        self.index = index
        self.table = table
    }
    
    // Items count
    
    public func numberOfItems(managedTable: AAManagedTable) -> Int {
        var res = 0
        for r in regions {
            res += r.rangeNumberOfItems(managedTable)
        }
        return res
    }
    
    // Cells
    
    public func cellHeightForItem(managedTable: AAManagedTable, indexPath: NSIndexPath) -> CGFloat {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeCellHeightForItem(managedTable, indexPath: r.index)
    }
    
    public func cellForItem(managedTable: AAManagedTable, indexPath: NSIndexPath) -> UITableViewCell {
        let r = findCell(managedTable, indexPath: indexPath)
        
        let res = r.cells.rangeCellForItem(managedTable, indexPath: r.index)
        if autoSeparators {
            if let cell = res as? AATableViewCell {
                
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
    
    func canSelect(managedTable: AAManagedTable, indexPath: NSIndexPath) -> Bool {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeCanSelect(managedTable, indexPath: r.index)
    }
    
    func select(managedTable: AAManagedTable, indexPath: NSIndexPath) -> Bool {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeSelect(managedTable, indexPath: r.index)
    }
    
    // Copying
    
    func canCopy(managedTable: AAManagedTable, indexPath: NSIndexPath) -> Bool {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeCanCopy(managedTable, indexPath: r.index)
    }
    
    func copy(managedTable: AAManagedTable, indexPath: NSIndexPath) {
        let r = findCell(managedTable, indexPath: indexPath)
        r.cells.rangeCopy(managedTable, indexPath: r.index)
    }
    
    // Deletion
    
    func canDelete(managedTable: AAManagedTable, indexPath: NSIndexPath) -> Bool {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeCanDelete(managedTable, indexPath: r.index)
    }
    
    func delete(managedTable: AAManagedTable, indexPath: NSIndexPath) {
        let r = findCell(managedTable, indexPath: indexPath)
        r.cells.rangeDelete(managedTable, indexPath: r.index)
    }

    
    // Binding
    
    func bind(managedTable: AAManagedTable, binder: AABinder) {
        for s in regions {
            s.rangeBind(managedTable, binder: binder)
        }
    }
    
    func unbind(managedTable: AAManagedTable, binder: AABinder) {
        for s in regions {
            s.rangeUnbind(managedTable, binder: binder)
        }
    }

    // Private Tools
    
    private func findCell(managedTable: AAManagedTable, indexPath: NSIndexPath) -> CellSearchResult {
        var prevLength = 0
        for i in 0..<regions.count {
            let r = regions[i]
            let itemsCount = r.rangeNumberOfItems(managedTable)
            if (prevLength <= indexPath.row && indexPath.row < prevLength + itemsCount) {
                return CellSearchResult(cells: r, index: AARangeIndexPath(section: indexPath.section, range: i, item: indexPath.row - prevLength, indexPath: indexPath))
            }
            prevLength += itemsCount
        }
        fatalError("Inconsistent cells")
    }
    
    private class CellSearchResult {
        
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
    
    public func setSeparatorsTopOffset(offset: Int) -> AAManagedSection {
        self.autoSeparatorTopOffset = offset
        return self
    }
    
    public func setFooterText(footerText: String) -> AAManagedSection {
        self.footerText = footerText
        return self
    }
    
    public func setHeaderText(headerText: String) -> AAManagedSection {
        self.headerText = headerText
        return self
    }
    
    public func setFooterHeight(footerHeight: Double) -> AAManagedSection {
        self.footerHeight = footerHeight
        return self
    }
    
    public func setHeaderHeight(headerHeight: Double) -> AAManagedSection {
        self.headerHeight = headerHeight
        return self
    }
}

