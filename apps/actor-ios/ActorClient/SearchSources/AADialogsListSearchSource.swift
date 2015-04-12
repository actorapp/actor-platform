//
//  AADialogsListSearchSource.swift
//  ActorApp
//
//  Created by Danil Gontovnik on 4/9/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AADialogsListSearchSource: SearchSource {
    
    // MARK: -
    // MARK: Private vars
    
    private let DialogsListSearchCellIdentifier = "DialogsListSearchCellIdentifier"
    
    // MARK: -
    // MARK: UITableView
    
    override func buildCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?) -> UITableViewCell {
        var cell = tableView.dequeueReusableCellWithIdentifier(DialogsListSearchCellIdentifier) as! AADialogsListSearchCell?
        
        if cell == nil {
            cell = AADialogsListSearchCell(reuseIdentifier: DialogsListSearchCellIdentifier)
        }
        
        return cell!;
    }
    
    override func bindCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?, cell: UITableViewCell) {
        var searchEntity = item as! AMSearchEntity
        
        let isLast = indexPath.row == tableView.numberOfRowsInSection(indexPath.section) - 1;
        (cell as? AADialogsListSearchCell)?.bindSearchEntity(searchEntity, isLast: isLast)
    }
    
    override func buildDisplayList() -> AMBindedDisplayList {
        return MSG.buildSearchList()
    }
   
}
