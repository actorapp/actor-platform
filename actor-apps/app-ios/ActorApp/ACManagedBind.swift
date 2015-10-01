//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

protocol ACBindedCell {
    
    typealias BindData
    
    static func bindedCellHeight(table: ACManagedTable, item: BindData) -> CGFloat
    
    func bind(item: BindData, table: ACManagedTable, index: Int, totalCount: Int)
}

protocol ACBindedSearchCell {
    typealias BindData
    
    static func bindedCellHeight(item: BindData) -> CGFloat
    
    func bind(item: BindData, search: String?)
}

class ACBindedRows<BindCell where BindCell: UITableViewCell, BindCell: ACBindedCell>: NSObject, ACManagedRange, ARDisplayList_AppleChangeListener {
    
    var displayList: ARBindedDisplayList!
    
    var selectAction: ((BindCell.BindData) -> Bool)?
    
    var canEditAction: ((BindCell.BindData) -> Bool)?
    
    var editAction: ((BindCell.BindData) -> ())?
    
    var didBind: ((BindCell, BindCell.BindData) -> ())?
    
    var autoHide = true
    
    private var table: ACManagedTable!
    
    // Total items count
    
    func rangeNumberOfItems(table: ACManagedTable) -> Int {
        return Int(displayList.size())
    }
    
    // Cells
    
    func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        let data = displayList.itemWithIndex(jint(indexPath.item)) as! BindCell.BindData
        return BindCell.self.bindedCellHeight(table, item: data)
    }
    
    func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
        let data = displayList.itemWithIndex(jint(indexPath.item)) as! BindCell.BindData
        let cell = table.dequeueCell(indexPath.indexPath) as BindCell
        cell.bind(data, table: table, index: indexPath.item, totalCount: rangeNumberOfItems(table))
        displayList.touchWithIndex(jint(indexPath.item))
        didBind?(cell, data)
        return cell
    }
    
    // Select
    
    func rangeCanSelect(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        
        return selectAction != nil
    }
    
    func rangeSelect(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        
        return selectAction!(displayList.itemWithIndex(jint(indexPath.item)) as! BindCell.BindData)
    }
    
    // Delete
    
    func rangeCanDelete(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        if canEditAction != nil {
            return canEditAction!(displayList.itemWithIndex(jint(indexPath.item)) as! BindCell.BindData)
        }
        return false
    }
    
    func rangeDelete(table: ACManagedTable, indexPath: ACRangeIndexPath) {
        editAction!(displayList.itemWithIndex(jint(indexPath.item)) as! BindCell.BindData)
    }
    
    // Binding
    
    func rangeBind(table: ACManagedTable, binder: Binder) {
        
        self.table = table

        displayList.addAppleListener(self)
        
        updateVisibility()
    }
    
    @objc func onCollectionChangedWithChanges(modification: ARAppleListUpdate!) {
        
        let tableView = self.table.tableView
        let section = 0
        
        if (modification.isLoadMore) {
            UIView.setAnimationsEnabled(false)
        }
        
        if modification.nonUpdateCount() > 0 {
            tableView.beginUpdates()
            
            // Removed rows
            if modification.removedCount() > 0 {
                var rows = [NSIndexPath]()
                for i in 0..<modification.removedCount() {
                    rows.append(NSIndexPath(forRow: Int(modification.getRemoved(jint(i))), inSection: section))
                }
                tableView.deleteRowsAtIndexPaths(rows, withRowAnimation: UITableViewRowAnimation.Automatic)
            }
            
            // Added rows
            if modification.addedCount() > 0 {
                var rows = [NSIndexPath]()
                for i in 0..<modification.addedCount() {
                    rows.append(NSIndexPath(forRow: Int(modification.getAdded(jint(i))), inSection: section))
                }
                tableView.insertRowsAtIndexPaths(rows, withRowAnimation: UITableViewRowAnimation.Automatic)
            }
            
            // Moved rows
            if modification.movedCount() > 0 {
                for i in 0..<modification.movedCount() {
                    let mov = modification.getMoved(jint(i))
                    tableView.moveRowAtIndexPath(NSIndexPath(forRow: Int(mov.getSourceIndex()), inSection: section), toIndexPath: NSIndexPath(forRow: Int(mov.getDestIndex()), inSection: section))
                }
            }
            
            tableView.endUpdates()
        }
        
        // Updated rows
        if modification.updatedCount() > 0 {
            let visibleIndexes = tableView.indexPathsForVisibleRows!
            for i in 0..<modification.updatedCount() {
                for visibleIndex in visibleIndexes {
                    if (visibleIndex.row == Int(modification.getUpdated(jint(i))) && visibleIndex.section == section) {

                        // Need to rebind manually because we need to keep cell reference same
                        if let item = displayList.itemWithIndex(jint(visibleIndex.row)) as? BindCell.BindData,
                            let cell = tableView.cellForRowAtIndexPath(visibleIndex) as? BindCell {
                                
                            cell.bind(item, table: table, index: visibleIndex.row, totalCount: Int(displayList.size()))
                        }
                    }
                }
            }
        }
        
        updateVisibility()
        
        if (modification.isLoadMore) {
            UIView.setAnimationsEnabled(true)
        }
    }
    
    func updateVisibility() {
        if autoHide {
            if displayList.size() == 0 {
                table.hideTable()
            } else {
                table.showTable()
            }
        }
    }
    
    func rangeUnbind(table: ACManagedTable, binder: Binder) {
        
        self.table = nil
        
        displayList.removeAppleListener(self)
    }
    
    func filter(text: String) {
        if (text.length == 0) {
            self.displayList.initTopWithRefresh(false)
        } else {
            self.displayList.initSearchWithQuery(text, withRefresh: false)
        }
    }
    
    private func checkInstallation() {
        if displayList == nil {
            fatalError("Display list not set!")
        }
    }
}


extension ACManagedSection {
    
    func binded<T where T: UITableViewCell, T: ACBindedCell>(closure: (r: ACBindedRows<T>) -> ()) -> ACBindedRows<T> {
        let r = ACBindedRows<T>()
        regions.append(r)
        closure(r: r)
        r.checkInstallation()
        return r
    }
}
