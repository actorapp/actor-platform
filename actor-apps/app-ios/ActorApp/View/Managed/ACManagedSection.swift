//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class ACManagedSection {
    
    var headerHeight: Double = 0
    var footerHeight: Double = 0
    
    var headerText: String? = nil
    var footerText: String? = nil
    
    let index: Int
    
    var autoSeparatorTopOffset: Int = 0
    var autoSeparatorBottomOffset: Int = 0
    var autoSeparators: Bool = false
    var autoSeparatorsInset: CGFloat = 15.0
    
    var regions: [ACManagedRange] = [ACManagedRange]()
    
    unowned let table: ACManagedTable
    
    init(table: ACManagedTable, index: Int) {
        self.index = index
        self.table = table
    }
    
    // Items count
    
    func numberOfItems(managedTable: ACManagedTable) -> Int {
        var res = 0
        for r in regions {
            res += r.rangeNumberOfItems(managedTable)
        }
        return res
    }
    
    // Cells
    
    func cellHeightForItem(managedTable: ACManagedTable, indexPath: NSIndexPath) -> CGFloat {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeCellHeightForItem(managedTable, indexPath: r.index)
    }
    
    func cellForItem(managedTable: ACManagedTable, indexPath: NSIndexPath) -> UITableViewCell {
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
    
    func canSelect(managedTable: ACManagedTable, indexPath: NSIndexPath) -> Bool {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeCanSelect(managedTable, indexPath: r.index)
    }
    
    func select(managedTable: ACManagedTable, indexPath: NSIndexPath) -> Bool {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeSelect(managedTable, indexPath: r.index)
    }
    
    // Copying
    
    func canCopy(managedTable: ACManagedTable, indexPath: NSIndexPath) -> Bool {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeCanCopy(managedTable, indexPath: r.index)
    }
    
    func copy(managedTable: ACManagedTable, indexPath: NSIndexPath) {
        let r = findCell(managedTable, indexPath: indexPath)
        r.cells.rangeCopy(managedTable, indexPath: r.index)
    }
    
    // Deletion
    
    func canDelete(managedTable: ACManagedTable, indexPath: NSIndexPath) -> Bool {
        let r = findCell(managedTable, indexPath: indexPath)
        return r.cells.rangeCanDelete(managedTable, indexPath: r.index)
    }
    
    func delete(managedTable: ACManagedTable, indexPath: NSIndexPath) {
        let r = findCell(managedTable, indexPath: indexPath)
        r.cells.rangeDelete(managedTable, indexPath: r.index)
    }

    
    // Binding
    
    func bind(managedTable: ACManagedTable, binder: Binder) {
        for s in regions {
            s.rangeBind(managedTable, binder: binder)
        }
    }
    
    func unbind(managedTable: ACManagedTable, binder: Binder) {
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

extension ACManagedSection {
    
    func setSeparatorsTopOffset(offset: Int) -> ACManagedSection {
        self.autoSeparatorTopOffset = offset
        return self
    }
    
    func setFooterText(footerText: String) -> ACManagedSection {
        self.footerText = footerText
        return self
    }
    
    func setHeaderText(headerText: String) -> ACManagedSection {
        self.headerText = headerText
        return self
    }
    
    func setFooterHeight(footerHeight: Double) -> ACManagedSection {
        self.footerHeight = footerHeight
        return self
    }
    
    func setHeaderHeight(headerHeight: Double) -> ACManagedSection {
        self.headerHeight = headerHeight
        return self
    }
}

