//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

class EngineListController: AAViewController, UITableViewDelegate, UITableViewDataSource, ARDisplayList_AppleChangeListener {
    
    private var engineTableView: UITableView!
    private var displayList: ARBindedDisplayList!
    private var fade: Bool = false
    private var contentSection: Int = 0
    
    init(contentSection: Int, nibName nibNameOrNil: String?, bundle nibBundleOrNil: NSBundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil);
        self.contentSection = contentSection;
    }
    
    init(contentSection: Int) {
        super.init(nibName: nil, bundle: nil);
        self.contentSection = contentSection;
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    
    func bindTable(table: UITableView, fade: Bool){
        self.fade = fade
        self.engineTableView = table;
        self.engineTableView!.dataSource = self;
        self.engineTableView!.delegate = self;
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        if (self.displayList == nil) {
            self.displayList = buildDisplayList()
        }
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        self.displayList.addAppleListener(self)
        self.engineTableView.reloadData()
        if (displayList.size() == jint(0)) {
            self.engineTableView.alpha = 0
        } else {
            self.engineTableView.alpha = 1
        }
        
        let selected = self.engineTableView.indexPathForSelectedRow;
        if (selected != nil){
            self.engineTableView.deselectRowAtIndexPath(selected!, animated: animated);
        }
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        self.displayList.removeAppleListener(self)
    }
    
    func filter(val: String) {
        if (val.length == 0) {
            self.displayList.initTopWithRefresh(false)
        } else {
            self.displayList.initSearchWithQuery(val, withRefresh: false)
        }
    }
    
    // Table Data Source
    
    func onCollectionChangedWithChanges(modification: ARAppleListUpdate!) {
        if (self.engineTableView == nil) {
            return
        }
        
        if (modification.isLoadMore) {
            UIView.setAnimationsEnabled(false)
        }
        
        if modification.nonUpdateCount() > 0 {
            self.engineTableView.beginUpdates()
            
            // Removed rows
            if modification.removedCount() > 0 {
                var rows = [NSIndexPath]()
                for i in 0..<modification.removedCount() {
                    rows.append(NSIndexPath(forRow: Int(modification.getRemoved(jint(i))), inSection: contentSection))
                }
                self.engineTableView.deleteRowsAtIndexPaths(rows, withRowAnimation: UITableViewRowAnimation.Automatic)
            }
            
            // Added rows
            if modification.addedCount() > 0 {
                var rows = [NSIndexPath]()
                for i in 0..<modification.addedCount() {
                    rows.append(NSIndexPath(forRow: Int(modification.getAdded(jint(i))), inSection: contentSection))
                }
                self.engineTableView.insertRowsAtIndexPaths(rows, withRowAnimation: UITableViewRowAnimation.Automatic)
            }
            
            // Moved rows
            if modification.movedCount() > 0 {
                for i in 0..<modification.movedCount() {
                    let mov = modification.getMoved(jint(i))
                    self.engineTableView.moveRowAtIndexPath(NSIndexPath(forRow: Int(mov.getSourceIndex()), inSection: contentSection), toIndexPath: NSIndexPath(forRow: Int(mov.getDestIndex()), inSection: contentSection))
                }
            }
 
            self.engineTableView.endUpdates()
        }
        
        // Updated rows
        if modification.updatedCount() > 0 {
            let visibleIndexes = self.engineTableView.indexPathsForVisibleRows!
            for i in 0..<modification.updatedCount() {
                for visibleIndex in visibleIndexes {
                    if (visibleIndex.row == Int(modification.getUpdated(jint(i))) && visibleIndex.section == contentSection) {
                        // Need to rebind manually because we need to keep cell reference same
                        let item: AnyObject? = objectAtIndexPath(visibleIndex)
                        let cell = self.engineTableView.cellForRowAtIndexPath(visibleIndex)
                        bindCell(self.engineTableView, cellForRowAtIndexPath: visibleIndex, item: item, cell: cell!)
                    }
                }
            }
        }
        
        if (displayList.size() == jint(0)) {
            if (self.engineTableView.alpha != 0) {
                if (fade) {
                    UIView.animateWithDuration(0.0, animations: { () -> Void in
                        self.engineTableView.alpha = 0
                    })
                } else {
                    self.engineTableView.alpha = 0
                }
            }
        } else {
            if (self.engineTableView.alpha == 0){
                if (fade) {
                    UIView.animateWithDuration(0.3, animations: { () -> Void in
                        self.engineTableView.alpha = 1
                    })
                } else {
                    self.engineTableView.alpha = 1
                }
            }
        }
        
        if (modification.isLoadMore) {
            UIView.setAnimationsEnabled(true)
        }
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (displayList == nil) {
            return 0;
        }
        
        return Int(displayList.size());
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let item: AnyObject? = objectAtIndexPath(indexPath)
        let cell = buildCell(tableView, cellForRowAtIndexPath:indexPath, item:item);
        bindCell(tableView, cellForRowAtIndexPath: indexPath, item: item, cell: cell);
        displayList.touchWithIndex(jint(indexPath.row))
        return cell;
    }
    
    func objectAtIndexPath(indexPath: NSIndexPath) -> AnyObject? {
        if (displayList == nil) {
            return nil
        }
        
        return displayList.itemWithIndex(jint(indexPath.row));
    }
    
    // Abstract methods
    
    func buildDisplayList() -> ARBindedDisplayList {
        fatalError("Not implemented");
    }
    
    func buildCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?)  -> UITableViewCell {
        fatalError("Not implemented");
    }
    
    func bindCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?, cell: UITableViewCell) {
        fatalError("Not implemented");
    }
}