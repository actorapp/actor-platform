//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public protocol AABindedCell {
    
    associatedtype BindData
    
    static func bindedCellHeight(_ table: AAManagedTable, item: BindData) -> CGFloat
    
    func bind(_ item: BindData, table: AAManagedTable, index: Int, totalCount: Int)
}

public protocol AABindedSearchCell {
    associatedtype BindData
    
    static func bindedCellHeight(_ item: BindData) -> CGFloat
    
    func bind(_ item: BindData, search: String?)
}

open class AABindedRows<BindCell>: NSObject, AAManagedRange, ARDisplayList_AppleChangeListener, ARDisplayList_Listener where BindCell: UITableViewCell, BindCell: AABindedCell {
    
    open var topOffset: Int = 0
    
    open var displayList: ARBindedDisplayList!
    
    open var selectAction: ((BindCell.BindData) -> Bool)?
    
    open var canEditAction: ((BindCell.BindData) -> Bool)?
    
    open var editAction: ((BindCell.BindData) -> ())?
    
    open var didBind: ((BindCell, BindCell.BindData) -> ())?
    
    open var autoHide = true
    
    open var animated = false
    
    open var differental = false
    
    fileprivate var table: AAManagedTable!
    
    fileprivate var lastItemsCount: Int = 0
    
    fileprivate let cellReuseId = "Bind:\(BindCell.self)"
    
    // Initing Table
    
    open func initTable(_ table: AAManagedTable) {
        table.tableView.register(BindCell.self, forCellReuseIdentifier: cellReuseId)
    }
    
    // Total items count
    
    open func rangeNumberOfItems(_ table: AAManagedTable) -> Int {
        return lastItemsCount
    }
    
    // Cells
    
    open func rangeCellHeightForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        let data = displayList.item(with: jint(indexPath.item)) as! BindCell.BindData
        return BindCell.self.bindedCellHeight(table, item: data)
    }
    
    open func rangeCellForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        let data = displayList.item(with: jint(indexPath.item)) as! BindCell.BindData
        let cell = self.table.tableView.dequeueReusableCell(withIdentifier: cellReuseId, for: indexPath.indexPath as IndexPath) as! BindCell
        cell.bind(data, table: table, index: indexPath.item, totalCount: lastItemsCount)
        displayList.touch(with: jint(indexPath.item))
        didBind?(cell, data)
        return cell
    }
    
    // Select
    
    open func rangeCanSelect(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        
        return selectAction != nil
    }
    
    open func rangeSelect(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        
        return selectAction!(displayList.item(with: jint(indexPath.item)) as! BindCell.BindData)
    }
    
    // Delete
    
    open func rangeCanDelete(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        if canEditAction != nil {
            return canEditAction!(displayList.item(with: jint(indexPath.item)) as! BindCell.BindData)
        }
        return false
    }
    
    open func rangeDelete(_ table: AAManagedTable, indexPath: AARangeIndexPath) {
        editAction!(displayList.item(with: jint(indexPath.item)) as! BindCell.BindData)
    }
    
    // Binding
    
    open func rangeBind(_ table: AAManagedTable, binder: AABinder) {
        
        self.table = table

        if differental {
            displayList.addAppleListener(self)
        } else {
            displayList.add(self)
        }
        
        lastItemsCount = Int(displayList.size())
        
        updateVisibility()
    }
    
    @objc open func onCollectionChanged() {
        
        let oldCount = lastItemsCount
        lastItemsCount = Int(displayList.size())

        if oldCount != lastItemsCount {
            table.tableView.reloadData()
        } else {
            if let indexes = table.tableView.indexPathsForVisibleRows {
                let cells = table.tableView.visibleCells
                
                for i in 0..<indexes.count {
                    let index = indexes[i]
                    let cell = cells[i]
                    
                    if (index as NSIndexPath).section == 0 && (index as NSIndexPath).row >= topOffset {
                        let data = displayList.item(with: jint((index as NSIndexPath).row - topOffset)) as! BindCell.BindData
                        (cell as! BindCell).bind(data, table: table, index: (index as NSIndexPath).row - topOffset, totalCount: Int(displayList.size()))
                    }
                }
            }
        }
        
        updateVisibility()
    }
    
    @objc open func onCollectionChanged(withChanges modification: ARAppleListUpdate!) {
        
        let tableView = self.table.tableView
        let section = 0
        
        let useAnimatedUpdate = (modification.nonUpdateCount() <= 1)
        
        if useAnimatedUpdate {
            
            lastItemsCount = Int(displayList.size())
            
            if (modification.isLoadMore || !animated) {
                UIView.setAnimationsEnabled(false)
            }
            
            let animationType: UITableViewRowAnimation
            if animated && !modification.isLoadMore {
                animationType = UITableViewRowAnimation.automatic
            } else {
                animationType = UITableViewRowAnimation.none
            }
            
            if modification.nonUpdateCount() > 0 {
                tableView.beginUpdates()
                
                // Removed rows
                if modification.removedCount() > 0 {
                    var rows = [IndexPath]()
                    for i in 0..<modification.removedCount() {
                        rows.append(IndexPath(row: Int(modification.getRemoved(jint(i))) + topOffset, section: section))
                    }
                    tableView.deleteRows(at: rows, with: animationType)
                }
                
                // Added rows
                if modification.addedCount() > 0 {
                    var rows = [IndexPath]()
                    for i in 0..<modification.addedCount() {
                        rows.append(IndexPath(row: Int(modification.getAdded(jint(i)) + topOffset), section: section))
                    }
                    tableView.insertRows(at: rows, with: animationType)
                }
                
                // Moved rows
                if modification.movedCount() > 0 {
                    for i in 0..<modification.movedCount() {
                        let mov = modification.getMoved(jint(i))
                        tableView.moveRow(at: IndexPath(row: Int((mov?.getSourceIndex())!) + topOffset, section: section), to: IndexPath(row: Int((mov?.getDestIndex())!) + topOffset, section: section))
                    }
                }
                
                tableView.endUpdates()
            }
            
            // Updated rows
            if modification.updatedCount() > 0 {
                let visibleIndexes = tableView.indexPathsForVisibleRows!
                for i in 0..<modification.updatedCount() {
                    for visibleIndex in visibleIndexes {
                        if ((visibleIndex as NSIndexPath).row == Int(modification.getUpdated(jint(i))) + topOffset && (visibleIndex as NSIndexPath).section == section) {
                            
                            // Need to rebind manually because we need to keep cell reference same
                            if let item = displayList.item(with: jint((visibleIndex as NSIndexPath).row - topOffset)) as? BindCell.BindData,
                                let cell = tableView.cellForRow(at: visibleIndex) as? BindCell {
                                    
                                    cell.bind(item, table: table, index: (visibleIndex as NSIndexPath).row, totalCount: Int(displayList.size()))
                            }
                        }
                    }
                }
            }
            
            if (modification.isLoadMore || !animated) {
                UIView.setAnimationsEnabled(true)
            }
        } else {
            let oldCount = lastItemsCount
            lastItemsCount = Int(displayList.size())
            
            if oldCount != lastItemsCount {
                table.tableView.reloadData()
            } else {
                if let indexes = table.tableView.indexPathsForVisibleRows {
                    let cells = table.tableView.visibleCells
                    
                    for i in 0..<indexes.count {
                        let index = indexes[i]
                        let cell = cells[i]
                        
                        if (index as NSIndexPath).section == 0 && (index as NSIndexPath).row >= topOffset {
                            let data = displayList.item(with: jint((index as NSIndexPath).row - topOffset)) as! BindCell.BindData
                            (cell as! BindCell).bind(data, table: table, index: (index as NSIndexPath).row - topOffset, totalCount: Int(displayList.size()))
                        }
                    }
                }
            }
        }
        
        updateVisibility()
    }
    
    open func updateVisibility() {
        if autoHide {
            if displayList.size() == 0 {
                table.hideTable()
            } else {
                table.showTable()
            }
        }
    }
    
    open func rangeUnbind(_ table: AAManagedTable, binder: AABinder) {
        
        self.table = nil
        
        if differental {
            displayList.removeAppleListener(self)
        } else {
            displayList.remove(self)
        }
    }
    
    open func filter(_ text: String) {
        if (text.length == 0) {
            self.displayList.initTop(withRefresh: false)
        } else {
            self.displayList.initSearch(withQuery: text, withRefresh: false)
        }
    }
    
    fileprivate func checkInstallation() {
        if displayList == nil {
            fatalError("Display list not set!")
        }
    }
}


public extension AAManagedSection {
    
    public func binded<T>(_ closure: (_ r: AABindedRows<T>) -> ()) -> AABindedRows<T> where T: UITableViewCell, T: AABindedCell {
        
        let topOffset = numberOfItems(self.table)
        
        let r = AABindedRows<T>()
        r.topOffset = topOffset
        regions.append(r)
        closure(r)
        r.checkInstallation()
        r.initTable(self.table)
        return r
    }
}
