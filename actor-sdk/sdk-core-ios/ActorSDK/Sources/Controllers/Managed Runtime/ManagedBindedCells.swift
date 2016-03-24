//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public protocol AABindedCell {
    
    associatedtype BindData
    
    static func bindedCellHeight(table: AAManagedTable, item: BindData) -> CGFloat
    
    func bind(item: BindData, table: AAManagedTable, index: Int, totalCount: Int)
}

public protocol AABindedSearchCell {
    associatedtype BindData
    
    static func bindedCellHeight(item: BindData) -> CGFloat
    
    func bind(item: BindData, search: String?)
}

public class AABindedRows<BindCell where BindCell: UITableViewCell, BindCell: AABindedCell>: NSObject, AAManagedRange, ARDisplayList_AppleChangeListener, ARDisplayList_Listener {
    
    public var topOffset: Int = 0
    
    public var displayList: ARBindedDisplayList!
    
    public var selectAction: ((BindCell.BindData) -> Bool)?
    
    public var canEditAction: ((BindCell.BindData) -> Bool)?
    
    public var editAction: ((BindCell.BindData) -> ())?
    
    public var didBind: ((BindCell, BindCell.BindData) -> ())?
    
    public var autoHide = true
    
    public var animated = false
    
    public var differental = false
    
    private var table: AAManagedTable!
    
    private var lastItemsCount: Int = 0
    
    private let cellReuseId = "Bind:\(BindCell.self)"
    
    // Initing Table
    
    public func initTable(table: AAManagedTable) {
        table.tableView.registerClass(BindCell.self, forCellReuseIdentifier: cellReuseId)
    }
    
    // Total items count
    
    public func rangeNumberOfItems(table: AAManagedTable) -> Int {
        return lastItemsCount
    }
    
    // Cells
    
    public func rangeCellHeightForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        let data = displayList.itemWithIndex(jint(indexPath.item)) as! BindCell.BindData
        return BindCell.self.bindedCellHeight(table, item: data)
    }
    
    public func rangeCellForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        let data = displayList.itemWithIndex(jint(indexPath.item)) as! BindCell.BindData
        let cell = self.table.tableView.dequeueReusableCellWithIdentifier(cellReuseId, forIndexPath: indexPath.indexPath) as! BindCell
        cell.bind(data, table: table, index: indexPath.item, totalCount: lastItemsCount)
        displayList.touchWithIndex(jint(indexPath.item))
        didBind?(cell, data)
        return cell
    }
    
    // Select
    
    public func rangeCanSelect(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        
        return selectAction != nil
    }
    
    public func rangeSelect(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        
        return selectAction!(displayList.itemWithIndex(jint(indexPath.item)) as! BindCell.BindData)
    }
    
    // Delete
    
    public func rangeCanDelete(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        if canEditAction != nil {
            return canEditAction!(displayList.itemWithIndex(jint(indexPath.item)) as! BindCell.BindData)
        }
        return false
    }
    
    public func rangeDelete(table: AAManagedTable, indexPath: AARangeIndexPath) {
        editAction!(displayList.itemWithIndex(jint(indexPath.item)) as! BindCell.BindData)
    }
    
    // Binding
    
    public func rangeBind(table: AAManagedTable, binder: AABinder) {
        
        self.table = table

        if differental {
            displayList.addAppleListener(self)
        } else {
            displayList.addListener(self)
        }
        
        lastItemsCount = Int(displayList.size())
        
        updateVisibility()
    }
    
    @objc public func onCollectionChanged() {
        
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
                    
                    if index.section == 0 && index.row >= topOffset {
                        let data = displayList.itemWithIndex(jint(index.row - topOffset)) as! BindCell.BindData
                        (cell as! BindCell).bind(data, table: table, index: index.row - topOffset, totalCount: Int(displayList.size()))
                    }
                }
            }
        }
        
        updateVisibility()
    }
    
    @objc public func onCollectionChangedWithChanges(modification: ARAppleListUpdate!) {
        
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
                animationType = UITableViewRowAnimation.Automatic
            } else {
                animationType = UITableViewRowAnimation.None
            }
            
            if modification.nonUpdateCount() > 0 {
                tableView.beginUpdates()
                
                // Removed rows
                if modification.removedCount() > 0 {
                    var rows = [NSIndexPath]()
                    for i in 0..<modification.removedCount() {
                        rows.append(NSIndexPath(forRow: Int(modification.getRemoved(jint(i))) + topOffset, inSection: section))
                    }
                    tableView.deleteRowsAtIndexPaths(rows, withRowAnimation: animationType)
                }
                
                // Added rows
                if modification.addedCount() > 0 {
                    var rows = [NSIndexPath]()
                    for i in 0..<modification.addedCount() {
                        rows.append(NSIndexPath(forRow: Int(modification.getAdded(jint(i)) + topOffset), inSection: section))
                    }
                    tableView.insertRowsAtIndexPaths(rows, withRowAnimation: animationType)
                }
                
                // Moved rows
                if modification.movedCount() > 0 {
                    for i in 0..<modification.movedCount() {
                        let mov = modification.getMoved(jint(i))
                        tableView.moveRowAtIndexPath(NSIndexPath(forRow: Int(mov.getSourceIndex()) + topOffset, inSection: section), toIndexPath: NSIndexPath(forRow: Int(mov.getDestIndex()) + topOffset, inSection: section))
                    }
                }
                
                tableView.endUpdates()
            }
            
            // Updated rows
            if modification.updatedCount() > 0 {
                let visibleIndexes = tableView.indexPathsForVisibleRows!
                for i in 0..<modification.updatedCount() {
                    for visibleIndex in visibleIndexes {
                        if (visibleIndex.row == Int(modification.getUpdated(jint(i))) + topOffset && visibleIndex.section == section) {
                            
                            // Need to rebind manually because we need to keep cell reference same
                            if let item = displayList.itemWithIndex(jint(visibleIndex.row - topOffset)) as? BindCell.BindData,
                                let cell = tableView.cellForRowAtIndexPath(visibleIndex) as? BindCell {
                                    
                                    cell.bind(item, table: table, index: visibleIndex.row, totalCount: Int(displayList.size()))
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
                        
                        if index.section == 0 && index.row >= topOffset {
                            let data = displayList.itemWithIndex(jint(index.row - topOffset)) as! BindCell.BindData
                            (cell as! BindCell).bind(data, table: table, index: index.row - topOffset, totalCount: Int(displayList.size()))
                        }
                    }
                }
            }
        }
        
        updateVisibility()
    }
    
    public func updateVisibility() {
        if autoHide {
            if displayList.size() == 0 {
                table.hideTable()
            } else {
                table.showTable()
            }
        }
    }
    
    public func rangeUnbind(table: AAManagedTable, binder: AABinder) {
        
        self.table = nil
        
        if differental {
            displayList.removeAppleListener(self)
        } else {
            displayList.removeListener(self)
        }
    }
    
    public func filter(text: String) {
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


public extension AAManagedSection {
    
    public func binded<T where T: UITableViewCell, T: AABindedCell>(@noescape closure: (r: AABindedRows<T>) -> ()) -> AABindedRows<T> {
        
        let topOffset = numberOfItems(self.table)
        
        let r = AABindedRows<T>()
        r.topOffset = topOffset
        regions.append(r)
        closure(r: r)
        r.checkInstallation()
        r.initTable(self.table)
        return r
    }
}
