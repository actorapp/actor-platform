//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class DialogsSearchSource: SearchSource {
    
    // MARK: -
    // MARK: Private vars
    
    private let DialogsListSearchCellIdentifier = "DialogsListSearchCellIdentifier"
    
    // MARK: -
    // MARK: UITableView
    
    override func buildCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?) -> UITableViewCell {
        var cell = tableView.dequeueReusableCellWithIdentifier(DialogsListSearchCellIdentifier) as! DialogsSearchCell?
        
        if cell == nil {
            cell = DialogsSearchCell(reuseIdentifier: DialogsListSearchCellIdentifier)
        }
        
        return cell!;
    }
    
    override func bindCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?, cell: UITableViewCell) {
        var searchEntity = item as! AMSearchEntity
        
        let isLast = indexPath.row == tableView.numberOfRowsInSection(indexPath.section) - 1;
        (cell as? DialogsSearchCell)?.bindSearchEntity(searchEntity, isLast: isLast)
    }
    
    override func buildDisplayList() -> AMBindedDisplayList {
        return MSG.buildSearchList()
    }
   
}
