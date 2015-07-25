//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

class EngineListController: AAViewController, UITableViewDelegate, UITableViewDataSource, AMDisplayList_AppleChangeListener {
    
    private var engineTableView: UITableView!
    private var displayList: AMBindedDisplayList!
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
            self.displayList.addAppleListener(self)
            self.engineTableView.reloadData()
            if (displayList.size() == jint(0)) {
                self.engineTableView.alpha = 0
            } else {
                self.engineTableView.alpha = 1
            }
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
            self.displayList.initTopWithRefresh(false)
        } else {
            self.displayList.initSearchWithQuery(val, withRefresh: false)
        }
    }
    
    // Table Data Source
    
    func onCollectionChangedWithChanges(modification: AMAppleListUpdate!) {
        if (self.engineTableView == nil) {
            return
        }
        
        if (modification.isLoadMore()) {
            UIView.setAnimationsEnabled(false)
        }
        
        // Apply other changes
        self.engineTableView.beginUpdates()
        var hasUpdates = false
        for i in 0..<modification.size() {
            var change = modification.changeAt(i)
            switch(UInt(change.getOperationType().ordinal())) {
            case AMChangeDescription_OperationType.ADD.rawValue:
                var startIndex = Int(change.getIndex())
                var rows: NSMutableArray = []
                for ind in 0..<change.getLength() {
                    rows.addObject(NSIndexPath(forRow: Int(startIndex + ind), inSection: contentSection))
                }
                self.engineTableView.insertRowsAtIndexPaths(rows as [AnyObject], withRowAnimation: UITableViewRowAnimation.Automatic)
                break
            case AMChangeDescription_OperationType.UPDATE.rawValue:
                // Execute in separate batch
                hasUpdates = true
                break
            case AMChangeDescription_OperationType.REMOVE.rawValue:
                var startIndex = Int(change.getIndex())
                var rows: NSMutableArray = []
                for ind in 0..<change.getLength() {
                    rows.addObject(NSIndexPath(forRow: Int(startIndex + ind), inSection: contentSection))
                }
                self.engineTableView.deleteRowsAtIndexPaths(rows as [AnyObject], withRowAnimation: UITableViewRowAnimation.Automatic)
            case AMChangeDescription_OperationType.MOVE.rawValue:
                self.engineTableView.moveRowAtIndexPath(NSIndexPath(forRow: Int(change.getIndex()), inSection: contentSection), toIndexPath: NSIndexPath(forRow: Int(change.getDestIndex()), inSection: contentSection))
                break
            default:
                break
            }
        }
        self.engineTableView.endUpdates()
        
        // Apply cell change
        if (hasUpdates) {
            var visibleIndexes = self.engineTableView.indexPathsForVisibleRows() as! [NSIndexPath]
            for i in 0..<modification.size() {
                var change = modification.changeAt(i)
                switch(UInt(change.getOperationType().ordinal())) {
                case AMChangeDescription_OperationType.UPDATE.rawValue:
                    var startIndex = Int(change.getIndex())
                    var rows: NSMutableArray = []
                    for ind in 0..<change.getLength() {
                        for visibleIndex in visibleIndexes {
                            if (visibleIndex.row == Int(startIndex + ind) && visibleIndex.section == contentSection) {
                                // Need to rebind manually because we need to keep cell reference same
                                var item: AnyObject? = objectAtIndexPath(visibleIndex)
                                var cell = self.engineTableView.cellForRowAtIndexPath(visibleIndex)
                                bindCell(self.engineTableView, cellForRowAtIndexPath: visibleIndex, item: item, cell: cell!)
                            }
                        }
                    }
                    break
                default:
                    break
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
        
        if (modification.isLoadMore()) {
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
        var item: AnyObject? = objectAtIndexPath(indexPath)
        var cell = buildCell(tableView, cellForRowAtIndexPath:indexPath, item:item);
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