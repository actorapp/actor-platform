//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation


extension ACManagedTable {
    
    func dequeueCell<T where T: UITableViewCell>(indexPath: NSIndexPath) -> T {
        return self.tableView.dequeueCell(indexPath)
    }
}

extension ACManagedTable {
    
    func dequeueTextCell(indexPath: NSIndexPath) -> TextCell {
        return dequeueCell(indexPath)
    }
    
    func dequeueTitledCell(indexPath: NSIndexPath) -> TitledCell {
        return dequeueCell(indexPath)
    }
    
    func dequeueCommonCell(indexPath: NSIndexPath) -> CommonCell {
        return dequeueCell(indexPath)
    }
}