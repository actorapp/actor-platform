//
//  SearchSource.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 23.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
class SearchSource: NSObject, UISearchBarDelegate, UISearchDisplayDelegate, UITableViewDataSource, AMDisplayList_Listener {
    
    private var displayList: AMBindedDisplayList!;
    private let searchDisplay: UISearchDisplayController
    
    init(searchDisplay: UISearchDisplayController){
        self.searchDisplay = searchDisplay;
        super.init()
        self.displayList = buildDisplayList()
        self.displayList.addListenerWithAMDisplayList_Listener(self)
        
        searchDisplay.searchBar.delegate = self
        searchDisplay.searchResultsDataSource = self
        searchDisplay.delegate = self
    }
    
    func close() {
        self.displayList.removeListenerWithAMDisplayList_Listener(self)
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (section != 0) {
            return 0;
        }
        
        return Int(displayList.getSize());
    }
    
    func onCollectionChanged() {
        searchDisplay.searchResultsTableView.reloadData()
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var item: AnyObject? = objectAtIndexPath(indexPath)
        var cell = buildCell(tableView, cellForRowAtIndexPath:indexPath, item:item);
        bindCell(tableView, cellForRowAtIndexPath: indexPath, item: item, cell: cell);
        displayList.touchWithInt(jint(indexPath.row))
        return cell;
    }
    
    func buildDisplayList() -> AMBindedDisplayList {
        fatalError("Not implemented");
    }
    
    func buildCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?)  -> UITableViewCell {
        fatalError("Not implemented");
    }
    
    func bindCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?, cell: UITableViewCell) {
        fatalError("Not implemented");
    }
    
    func objectAtIndexPath(indexPath: NSIndexPath) -> AnyObject? {
        return displayList.getItemWithInt(jint(indexPath.row));
    }
    
    func searchBar(searchBar: UISearchBar, textDidChange searchText: String) {
        var normalized = searchText.trim()
        if (normalized.size() > 0) {
            displayList.initSearchWithNSString(normalized, withBoolean: false)
        } else {
            displayList.initTopWithBoolean(false)
        }
    }
    
    func searchDisplayControllerWillBeginSearch(controller: UISearchDisplayController) {
        UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.Default, animated: true)
    }
    
    func searchDisplayControllerWillEndSearch(controller: UISearchDisplayController) {
        UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.LightContent, animated: true)
    }
    
    func searchDisplayController(controller: UISearchDisplayController, didShowSearchResultsTableView tableView: UITableView) {
        for v in tableView.subviews {
            if (v is UIImageView) {
                (v as! UIImageView).alpha = 0;
            }
        }
    }
}