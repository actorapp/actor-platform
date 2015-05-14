//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

class EngineListController: AAViewController, UITableViewDelegate, UITableViewDataSource, AMDisplayList_DifferedChangeListener {
    
    private var engineTableView: UITableView!
    private var displayList: AMBindedDisplayList!
    private var applyingModification: AMDefferedListChange?
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
            self.displayList.addDifferedListenerWithAMDisplayList_DifferedChangeListener(self)
            
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
    
    func onCollectionChangedWithAMDefferedListChange(modification: AMDefferedListChange!) {
        if (self.engineTableView == nil) {
            return
        }
        
//        self.applyingModification = modification
//        
//        var currentChange: AMDefferedListModification? = modification.next()
//        
//        while currentChange != nil {
//            
//            currentChange = modification.next()
//        }
//        
//        self.applyingModification = nil
        self.engineTableView.reloadData()
        
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
    
//    func onCollectionChanged() {
//        if (self.engineTableView != nil) {
//            if (displayList.getSize() == jint(0)) {
//                if (self.engineTableView.alpha != 0) {
//                    if (fade) {
//                        UIView.animateWithDuration(0.0, animations: { () -> Void in
//                            self.engineTableView.alpha = 0
//                        })
//                    } else {
//                        self.engineTableView.alpha = 0
//                    }
//                }
//            } else {
//                if (self.engineTableView.alpha == 0){
//                    if (fade) {
//                        UIView.animateWithDuration(0.3, animations: { () -> Void in
//                            self.engineTableView.alpha = 1
//                        })
//                    } else {
//                        self.engineTableView.alpha = 1
//                    }
//                }
//            }
//            self.engineTableView.reloadData()
//        }
//    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (displayList == nil) {
            return 0;
        }
        if (applyingModification != nil) {
            return Int(applyingModification!.getCount())
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
        
        if (applyingModification != nil) {
            return applyingModification!.getItemWithInt(jint(indexPath.row))
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