//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class UARegion {
    
    private var section: UASection
    
    init(section: UASection) {
        self.section = section
    }
    
    func itemsCount() -> Int {
        fatalError("Not implemented")
    }
    
    func cellHeight(index: Int, width: CGFloat) -> CGFloat {
        fatalError("Not implemented")
    }
    
    func buildCell(tableView: UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell {
        fatalError("Not implemented")
    }
    
    func canSelect(index: Int) -> Bool {
        return false
    }
    
    func select(index: Int) -> Bool {
        fatalError("Not implemented")
    }
    
    func canCopy(index: Int) -> Bool {
        return false
    }
    
    func copy(index: Int) {
        fatalError("Not implemented")
    }
}