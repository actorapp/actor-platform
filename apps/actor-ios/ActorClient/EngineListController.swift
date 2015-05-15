//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

class EngineListController: AAViewController, UITableViewDelegate, UITableViewDataSource, AMDisplayList_AppleChangeListener {
    
    private var engineTableView: UITableView!
    private var displayList: AMBindedDisplayList!
    private var fade: Bool = false
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder);
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: NSBundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil);
    }
    
    override init() {
       super.init(nibName: nil, bundle: nil);
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
            self.displayList.addAppleListenerWithAMDisplayList_AppleChangeListener(self)
            
            if (displayList.getSize() == jint(0)) {
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
            self.displayList.initTopWithBoolean(false)
        } else {
            self.displayList.initSearchWithNSString(val, withBoolean: false)
        }
    }
    
    // Table Data Source
    
    func onCollectionChangedWithAMAppleListUpdate(modification: AMAppleListUpdate!) {
        if (self.engineTableView == nil) {
            return
        }

        self.engineTableView.beginUpdates()
        for i in 0..<modification.getChanges().size() {
            var change = modification.getChanges().getWithInt(i) as! AMChangeDescription
            switch(UInt(change.getOperationType().ordinal())) {
            case AMChangeDescription_OperationType.ADD.rawValue:
                var startIndex = Int(change.getIndex())
                var rows: NSMutableArray = []
                for ind in 0..<change.getLength() {
                    rows.addObject(NSIndexPath(forRow: Int(startIndex + ind), inSection: 0))
                }
                self.engineTableView.insertRowsAtIndexPaths(rows as [AnyObject], withRowAnimation: UITableViewRowAnimation.Automatic)
                break
            case AMChangeDescription_OperationType.UPDATE.rawValue:
                // TODO: Implement
                break
            case AMChangeDescription_OperationType.REMOVE.rawValue:
                var startIndex = Int(change.getIndex())
                var rows: NSMutableArray = []
                for ind in 0..<change.getLength() {
                    rows.addObject(NSIndexPath(forRow: Int(startIndex + ind), inSection: 0))
                }
                self.engineTableView.deleteRowsAtIndexPaths(rows as [AnyObject], withRowAnimation: UITableViewRowAnimation.Automatic)
            case AMChangeDescription_OperationType.MOVE.rawValue:
                self.engineTableView.moveRowAtIndexPath(NSIndexPath(forRow: Int(change.getIndex()), inSection: 0), toIndexPath: NSIndexPath(forRow: Int(change.getDestIndex()), inSection: 0))
                break
            default:
                break
            }
        }
        self.engineTableView.endUpdates()
//        var currentChange: AMChangeDescription? = modification.getChanges()
//        while currentChange != nil {
//            switch(UInt(currentChange!.getOperationType().ordinal())) {
//            case AMChangeDescription_OperationType.ADD.rawValue:
//                var rows = [NSIndexPath(forRow: Int(currentChange!.getIndex()), inSection: 0)]
//                self.engineTableView.insertRowsAtIndexPaths(rows, withRowAnimation: UITableViewRowAnimation.Top)
//                break
//            case AMDefferedListModification_Operation.ADD_RANGE.rawValue:
//                var rows: NSMutableArray = []
//                for i in 0..<Int(currentChange!.getLength()) {
//                    rows.addObject(NSIndexPath(forRow: Int(currentChange!.getIndex() + i), inSection: 0))
//                }
//                self.engineTableView.insertRowsAtIndexPaths(rows as [AnyObject], withRowAnimation: UITableViewRowAnimation.Middle)
//                break
//            case AMDefferedListModification_Operation.REMOVE.rawValue:
//                var rows = [NSIndexPath(forRow: Int(currentChange!.getIndex()), inSection: 0)]
//                self.engineTableView.deleteRowsAtIndexPaths(rows, withRowAnimation: UITableViewRowAnimation.Bottom)
//                break
//            case AMDefferedListModification_Operation.REMOVE_RANGE.rawValue:
//                var rows: NSMutableArray = []
//                for i in 0..<Int(currentChange!.getLength()) {
//                    rows.addObject(NSIndexPath(forRow: Int(currentChange!.getIndex() + i), inSection: 0))
//                }
//                self.engineTableView.deleteRowsAtIndexPaths(rows as [AnyObject], withRowAnimation: UITableViewRowAnimation.Middle)
//                break
//            case AMDefferedListModification_Operation.UPDATE.rawValue:
//                var rows = [NSIndexPath(forRow: Int(currentChange!.getIndex()), inSection: 0)]
//                self.engineTableView.reloadRowsAtIndexPaths(rows, withRowAnimation: UITableViewRowAnimation.None)
//                break
//            case AMDefferedListModification_Operation.UPDATE_RANGE.rawValue:
//                var rows: NSMutableArray = []
//                for i in 0..<Int(currentChange!.getLength()) {
//                    rows.addObject(NSIndexPath(forRow: Int(currentChange!.getIndex() + i), inSection: 0))
//                }
//                self.engineTableView.reloadRowsAtIndexPaths(rows as [AnyObject], withRowAnimation: UITableViewRowAnimation.None)
//                break
//            case AMDefferedListModification_Operation.MOVE.rawValue:
//                var nextIndex = currentChange!.getDestIndex()
//                if (currentChange!.getIndex() < nextIndex) {
//                    nextIndex++
//                }
//                self.engineTableView.moveRowAtIndexPath(NSIndexPath(forRow: Int(currentChange!.getIndex()), inSection: 0), toIndexPath: NSIndexPath(forRow: Int(nextIndex), inSection: 0))
//                break
//            default:
//                break
//            }
//            
//            currentChange = modification.next()
//        }
//        self.engineTableView.endUpdates()
        
        if (displayList.getSize() == jint(0)) {
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
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (displayList == nil) {
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