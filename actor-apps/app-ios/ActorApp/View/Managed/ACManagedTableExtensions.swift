//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation


// Cell automatic registration and dequeuing

private var registeredCells = "cells!"

extension ACManagedTable {
    
    private func cellTypeForClass<T where T: UITableViewCell>(cellClass: T.Type) -> String {
        
        let cellReuseId = "\(T.self)"
        var registered: ([String])! = getAssociatedObject(tableView, associativeKey: &registeredCells)
        var found = false
        if registered != nil {
            if registered.contains(cellReuseId) {
                found = true
            } else {
                registered.append(cellReuseId)
                setAssociatedObject(tableView, value: registered, associativeKey: &registeredCells)
            }
        } else {
            setAssociatedObject(tableView, value: [cellReuseId], associativeKey: &registeredCells)
        }
        
        if !found {
            tableView.registerClass(T.self, forCellReuseIdentifier: cellReuseId)
        }
        
        return cellReuseId
    }
    
    func dequeueCell<T where T: UITableViewCell>(indexPath: NSIndexPath? = nil) -> T {
        let reuseId = cellTypeForClass(T.self)
        if indexPath != nil {
            return self.tableView.dequeueReusableCellWithIdentifier(reuseId, forIndexPath: indexPath!) as! T
        } else {
            return self.tableView.dequeueReusableCellWithIdentifier(reuseId) as! T
        }
    }
}

extension ACManagedTable {
    
    func dequeueTextCell(indexPath: NSIndexPath? = nil) -> TextCell {
        return dequeueCell(indexPath)
    }
    
    func dequeueTitledCell(indexPath: NSIndexPath? = nil) -> TitledCell {
        return dequeueCell(indexPath)
    }
    
    func dequeueCommonCell(indexPath: NSIndexPath? = nil) -> CommonCell {
        return dequeueCell(indexPath)
    }
}