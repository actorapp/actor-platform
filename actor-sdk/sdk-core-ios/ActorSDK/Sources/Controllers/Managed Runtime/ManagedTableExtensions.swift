//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public extension AAManagedTable {
    
    public func dequeueCell<T>(_ indexPath: IndexPath) -> T where T: UITableViewCell {
        return self.tableView.dequeueCell(indexPath)
    }
}

public extension AAManagedTable {
    
    public func dequeueTextCell(_ indexPath: IndexPath) -> AATextCell {
        return dequeueCell(indexPath)
    }
    
    public func dequeueTitledCell(_ indexPath: IndexPath) -> AATitledCell {
        return dequeueCell(indexPath)
    }
    
    public func dequeueCommonCell(_ indexPath: IndexPath) -> AACommonCell {
        return dequeueCell(indexPath)
    }
}
