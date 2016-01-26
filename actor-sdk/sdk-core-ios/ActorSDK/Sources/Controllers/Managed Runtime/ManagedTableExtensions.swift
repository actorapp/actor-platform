//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public extension AAManagedTable {
    
    public func dequeueCell<T where T: UITableViewCell>(indexPath: NSIndexPath) -> T {
        return self.tableView.dequeueCell(indexPath)
    }
}

public extension AAManagedTable {
    
    public func dequeueTextCell(indexPath: NSIndexPath) -> AATextCell {
        return dequeueCell(indexPath)
    }
    
    public func dequeueTitledCell(indexPath: NSIndexPath) -> AATitledCell {
        return dequeueCell(indexPath)
    }
    
    public func dequeueCommonCell(indexPath: NSIndexPath) -> AACommonCell {
        return dequeueCell(indexPath)
    }
}