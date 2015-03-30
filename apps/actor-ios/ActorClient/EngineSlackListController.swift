//
//  EngineSlackListController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 11.03.15.
//  Copyright (c) 2015 Anton Bukov. All rights reserved.
//

import Foundation
import UIKit;

class EngineSlackListController: SLKTextViewController, UITableViewDelegate, UITableViewDataSource, AMDisplayList_Listener {

    private var displayList: AMBindedDisplayList!;
    private var emptyLock: Bool = true;
    
    init(isInverted:Bool) {
        super.init(tableViewStyle: UITableViewStyle.Plain);
        self.inverted = isInverted;
        self.tableView.contentInset = UIEdgeInsetsZero;
    }
    
    required init!(coder decoder: NSCoder!) {
        fatalError("Not implemented");
    }
    
    override func viewDidLoad() {
        if (self.displayList == nil) {
            self.displayList = getDisplayList()
            self.displayList.addListenerWithAMDisplayList_Listener(self)
        }
        
        dispatch_async(dispatch_get_main_queue(),{
            self.emptyLock = false
            self.tableView.reloadData()
        });
    }

    func onCollectionChanged() {
        NSLog("🇯🇵 onCollcetionChanged")
        if (self.emptyLock) {
            return
        }
        if (self.tableView != nil){
            self.tableView.reloadData()
        }
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (emptyLock) {
            return 0
        }
        
        if (displayList == nil) {
            return 0;
        }
        
        if (section != 0) {
            return 0;
        }
        
        return Int(displayList.getSize());
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var item: AnyObject? = objectAtIndexPath(indexPath)
        var cell = buildCell(tableView, cellForRowAtIndexPath:indexPath, item:item);
        bindCell(tableView, cellForRowAtIndexPath: indexPath, item: item, cell: cell);
        displayList.touchWithInt(jint(indexPath.row))
        cell.transform = tableView.transform
        return cell;
    }
    
    func objectAtIndexPath(indexPath: NSIndexPath) -> AnyObject? {
        if (displayList == nil) {
            return nil
        }
        
        return displayList.getItemWithInt(jint(indexPath.row));
    }
    
    // Abstract methods
    
    func getDisplayList() -> AMBindedDisplayList {
        fatalError("Not implemented");
    }
    
    func buildCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?)  -> UITableViewCell {
        fatalError("Not implemented");
    }
    
    func bindCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?, cell: UITableViewCell) {
        fatalError("Not implemented");
    }
}