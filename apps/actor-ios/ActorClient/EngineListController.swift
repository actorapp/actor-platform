//
//  EngineListController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 10.03.15.
//  Copyright (c) 2015 Anton Bukov. All rights reserved.
//

import Foundation
import UIKit

class EngineListController: UIViewController, UITableViewDelegate, UITableViewDataSource, AMDisplayList_Listener {
    
    private var engineTableView: UITableView!;
    private var displayList: AMBindedDisplayList!;
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder);
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: NSBundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil);
    }
    
    init(){
       super.init(nibName: nil, bundle: nil);
    }

    
    func bindTable(table: UITableView){
        self.engineTableView = table;
        self.engineTableView!.dataSource = self;
        self.engineTableView!.delegate = self;
    }
    
    override func viewDidLoad() {
        if (self.displayList == nil) {
            self.displayList = buildDisplayList()
            self.displayList.addListenerWithAMDisplayList_Listener(self)
            self.engineTableView.reloadData()
        }
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if (self.engineTableView != nil) {
            var selected = self.engineTableView.indexPathForSelectedRow();
            if (selected != nil){
                self.engineTableView.deselectRowAtIndexPath(selected!, animated: animated);
            }
        }
    }
    
    func filter(val: String) {
        if (val.size() == 0) {
            self.displayList.initTopWithBoolean(false)
        } else {
            self.displayList.initSearchWithNSString(val, withBoolean: false)
        }
    }
    
    // Table Data Source
    
    func onCollectionChanged() {
        if (self.engineTableView != nil){
            self.engineTableView.reloadData()
        }
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (displayList == nil) {
            return 0;
        }
        
        if (section != 0) {
            return 0;
        }
        
        return Int(displayList.getSize());
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var item: AnyObject? = objectAtIndexPath(indexPath)
        var cell = buildCell(tableView, cellForRowAtIndexPath:indexPath, item:item);
        bindCell(tableView, cellForRowAtIndexPath: indexPath, item: item, cell: cell);
        displayList.touchWithInt(jint(indexPath.row))
        return cell;
    }
    
    func objectAtIndexPath(indexPath: NSIndexPath) -> AnyObject? {
        if (displayList == nil) {
            return nil
        }
        
        return displayList.getItemWithInt(jint(indexPath.row));
    }
    
    // Abstract methods
    
    func buildDisplayList() -> AMBindedDisplayList {
        fatalError("Not implemented");
    }
    
    func buildCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?)  -> UITableViewCell {
        fatalError("Not implemented");
    }
    
    func bindCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?, cell: UITableViewCell) {
        fatalError("Not implemented");
    }
}