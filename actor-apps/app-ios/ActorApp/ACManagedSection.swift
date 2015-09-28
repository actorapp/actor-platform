//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class UASection {
    
    var headerHeight: Double = 0
    var footerHeight: Double = 0
    
    var headerText: String? = nil
    var footerText: String? = nil
    
    let index: Int
    
    var autoSeparatorTopOffset: Int = 0
    var autoSeparatorBottomOffset: Int = 0
    var autoSeparators: Bool = false
    var autoSeparatorsInset: CGFloat = 15.0
    
    unowned let managedTable: ACManagedTable
    
    var regions: [UARegion] = [UARegion]()
    
    init(managedTable: ACManagedTable, index: Int) {
        self.managedTable = managedTable
        self.index = index
    }
    
    func setSeparatorsTopOffset(offset: Int) -> UASection {
        self.autoSeparatorTopOffset = offset
        return self
    }
    
    func setFooterText(footerText: String) -> UASection {
        self.footerText = footerText
        return self
    }
    
    func setHeaderText(headerText: String) -> UASection {
        self.headerText = headerText
        return self
    }
    
    func setFooterHeight(footerHeight: Double) -> UASection {
        self.footerHeight = footerHeight
        return self
    }
    
    func setHeaderHeight(headerHeight: Double) -> UASection {
        self.headerHeight = headerHeight
        return self
    }
    
    
    func itemsCount() -> Int {
        var res = 0
        for r in regions {
            res += r.itemsCount()
        }
        return res
    }
    
    private func getRegion(indexPath: NSIndexPath) -> RegionSearchResult {
        var prevLength = 0
        for r in regions {
            if (prevLength <= indexPath.row && indexPath.row < prevLength + r.itemsCount()) {
                return RegionSearchResult(region: r, index: indexPath.row - prevLength)
            }
            prevLength += r.itemsCount()
        }
        
        fatalError("Inconsistent cell")
    }
    
    func buildCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let r = getRegion(indexPath)
        let res = r.region.buildCell(tableView, index: r.index, indexPath: indexPath)
        if autoSeparators {
            if let cell = res as? UATableViewCell {
                
                // Top separator
                // Showing only for top cell
                cell.topSeparatorLeftInset = 0
                cell.topSeparatorVisible = indexPath.row == autoSeparatorTopOffset
                
                if indexPath.row >= autoSeparatorTopOffset {
                    cell.bottomSeparatorVisible = true
                    if indexPath.row == itemsCount() - 1 {
                        cell.bottomSeparatorLeftInset = 0
                    } else {
                        cell.bottomSeparatorLeftInset = autoSeparatorsInset
                    }
                } else {
                    // too high
                    cell.bottomSeparatorVisible = false
                }
                
            }
        }
        return res
    }
    
    func cellHeight(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        let r = getRegion(indexPath)
        return r.region.cellHeight(r.index, width: tableView.bounds.width)
    }
    
    func canSelect(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        let r = getRegion(indexPath)
        return r.region.canSelect(r.index)
    }
    
    func select(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        let r = getRegion(indexPath)
        return r.region.select(r.index)
    }
    
    func canCopy(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        let r = getRegion(indexPath)
        return r.region.canCopy(r.index)
    }
    
    func copy(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) {
        let r = getRegion(indexPath)
        r.region.copy(r.index)
    }
}

extension UASection {
    func addActionCell(title: String, actionClosure: (() -> Bool)) -> UACommonCellRegion {
        return addCommonCell()
            .setContent(title)
            .setAction(actionClosure)
            .setStyle(.Action)
    }
    
    func addDangerCell(title: String, actionClosure: (() -> Bool)) -> UACommonCellRegion {
        return addCommonCell()
            .setContent(title)
            .setAction(actionClosure)
            .setStyle(.Destructive)
    }
    
    func addNavigationCell(title: String, actionClosure: (() -> Bool)) -> UACommonCellRegion {
        return addCommonCell()
            .setContent(title)
            .setAction(actionClosure)
            .setStyle(.Navigation)
    }
    
    func addCommonCell(closure: (cell: CommonCell)->()) -> UACommonCellRegion {
        let res = UACommonCellRegion(section: self, closure: closure)
        regions.append(res)
        return res
    }
    
    func addCommonCell() -> UACommonCellRegion {
        let res = UACommonCellRegion(section: self)
        regions.append(res)
        return res
    }
    
    func addTextCell(title: String, text: String) -> UATextCellRegion {
        let res = UATextCellRegion(title: title, text: text, section: self)
        regions.append(res)
        return res
    }
    
    func addTitledCell(title: String, text: String) -> UATitledCellRegion {
        let res = UATitledCellRegion(title: title, text: text, section: self)
        regions.append(res)
        return res
    }
    
    func addCustomCell(closure: (tableView:UITableView, indexPath: NSIndexPath) -> UITableViewCell) -> UACustomCellRegion {
        let res = UACustomCellRegion(section: self, closure: closure)
        regions.append(res)
        return res
    }
    
    func addCustomCells(height: CGFloat,countClosure: () -> Int, closure: (tableView:UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell) -> UACustomCellsRegion {
        let res = UACustomCellsRegion(height:height, countClosure: countClosure, closure: closure, section: self)
        regions.append(res)
        return res
    }
}
