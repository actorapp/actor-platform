//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit;

class EngineSlackListController: SLKTextViewController, UITableViewDelegate, UITableViewDataSource, AMDisplayList_AppleChangeListener {

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
            self.displayList.addAppleListenerWithAMDisplayList_AppleChangeListener(self)
        }
        
        dispatch_async(dispatch_get_main_queue(),{
            self.emptyLock = false
            self.tableView.reloadData()
        });
    }
    
    func onCollectionChangedWithAMAppleListUpdate(modification: AMAppleListUpdate!) {
        if (self.emptyLock) {
            return
        }
        
        self.tableView.beginUpdates()
        var hasUpdates = false
        for i in 0..<modification.getChanges().size() {
            var change = modification.getChanges().getWithInt(i) as! AMChangeDescription
            switch(UInt(change.getOperationType().ordinal())) {
            case AMChangeDescription_OperationType.ADD.rawValue:
                var startIndex = Int(change.getIndex())
                var rows: NSMutableArray = []
                for ind in 0..<change.getLength() {
                    rows.addObject(NSIndexPath(forRow: Int(startIndex + ind), inSection: 0))
                }
                self.tableView.insertRowsAtIndexPaths(rows as [AnyObject], withRowAnimation: UITableViewRowAnimation.Automatic)
                break
            case AMChangeDescription_OperationType.UPDATE.rawValue:
                // Execute in separate batch
                hasUpdates = true
                break
            case AMChangeDescription_OperationType.REMOVE.rawValue:
                var startIndex = Int(change.getIndex())
                var rows: NSMutableArray = []
                for ind in 0..<change.getLength() {
                    rows.addObject(NSIndexPath(forRow: Int(startIndex + ind), inSection: 0))
                }
                self.tableView.deleteRowsAtIndexPaths(rows as [AnyObject], withRowAnimation: UITableViewRowAnimation.Automatic)
            case AMChangeDescription_OperationType.MOVE.rawValue:
                self.tableView.moveRowAtIndexPath(NSIndexPath(forRow: Int(change.getIndex()), inSection: 0), toIndexPath: NSIndexPath(forRow: Int(change.getDestIndex()), inSection: 0))
                break
            default:
                break
            }
        }
        self.tableView.endUpdates()
        
        if (hasUpdates) {
            var visibleIndexes = self.tableView.indexPathsForVisibleRows() as! [NSIndexPath]
            for i in 0..<modification.getChanges().size() {
                var change = modification.getChanges().getWithInt(i) as! AMChangeDescription
                switch(UInt(change.getOperationType().ordinal())) {
                case AMChangeDescription_OperationType.UPDATE.rawValue:
                    var startIndex = Int(change.getIndex())
                    var rows: NSMutableArray = []
                    for ind in 0..<change.getLength() {
                        for visibleIndex in visibleIndexes {
                            if (visibleIndex.row == Int(startIndex + ind)) {
                                var item: AnyObject? = objectAtIndexPath(visibleIndex)
                                var cell = self.tableView.cellForRowAtIndexPath(visibleIndex)
                                bindCell(self.tableView, cellForRowAtIndexPath: visibleIndex, item: item, cell: cell!)
                            }
                        }
                    }
                    break
                default:
                    break
                }
            }
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
    
    func objectAtIndex(index: Int) -> AnyObject? {
        if (displayList == nil) {
            return nil
        }
        
        return displayList.getItemWithInt(jint(index));
    }
    
    func getCount() -> Int {
        if (displayList == nil) {
            return 0
        }
        
        return Int(displayList.getSize())
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