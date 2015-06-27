//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit;

class ConversationMessagesController: SLKTextViewController, UICollectionViewDelegateFlowLayout, AMDisplayList_AndroidChangeListener {

    private var displayList: AMBindedDisplayList!
    private var applyingUpdate: AMAndroidListUpdate?
    private var isStarted: Bool = false
    private var isUpdating: Bool = false
    private var isVisible: Bool = false
    
    init() {
        super.init(collectionViewLayout: MessagesFlowLayout())
    }
    
    required init!(coder decoder: NSCoder!) {
        fatalError("Not implemented");
    }
    
    // Controller and UI lifecycle
    
    override func viewDidLoad() {
        if (self.displayList == nil) {
            self.displayList = displayListForController()
        }
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        isVisible = true
        
        // Hack for delaying collection view init from first animation frame
        // This dramatically speed up controller opening
        
        if (isStarted) {
            self.collectionView.reloadData()
            self.displayList.addAndroidListener(self)
            return
        }
        
        dispatch_async(dispatch_get_main_queue(),{
            // What if controller is already closed?
            if (!self.isVisible) {
                return
            }
            
            self.isStarted = true
            self.displayList.addAndroidListener(self)
            self.collectionView.reloadData()
        });
    }
    
    func buildCell(collectionView: UICollectionView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?)  -> UICollectionViewCell {
        fatalError("Not implemented")
    }
    
    func bindCell(collectionView: UICollectionView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?, cell: UICollectionViewCell) {
        fatalError("Not implemented")
    }
    
    override func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return getCount()
    }
    
    override func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        var item: AnyObject? = objectAtIndexPath(indexPath)
        var cell = buildCell(collectionView, cellForRowAtIndexPath:indexPath, item:item)
        bindCell(collectionView, cellForRowAtIndexPath: indexPath, item: item, cell: cell)
        displayList.touchWithIndex(jint(indexPath.row))
        cell.contentView.transform = collectionView.transform
//        cell.transform = collectionView.transform
        return cell
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        isVisible = false
        
        // Remove listener on exit
        self.displayList.removeAndroidListener(self)
    }
    
    // Model updates
    
    func displayListForController() -> AMBindedDisplayList {
        fatalError("Not implemented");
    }
    
    func objectAtIndexPath(indexPath: NSIndexPath) -> AnyObject? {
        return objectAtIndex(indexPath.row)
    }
    
    func objectAtIndex(index: Int) -> AnyObject? {
        if (isUpdating) {
            return applyingUpdate!.getItemWithInt(jint(index))
        }
        return displayList.itemWithIndex(jint(index))
    }
    
    func getCount() -> Int {
        if (isUpdating) {
            return Int(applyingUpdate!.getSize())
        }
        return Int(displayList.size())
    }
    
    func onCollectionChangedWithChanges(modification: AMAndroidListUpdate!) {
        isUpdating = true
        applyingUpdate = modification
        
        collectionView.performBatchUpdates({ () -> Void in
            var mod = modification.next()
            while(mod != nil) {
                switch(UInt(mod.getOperationType().ordinal())) {
                    case AMChangeDescription_OperationType.ADD.rawValue:
                        var startIndex = Int(mod.getIndex())
                        var rows = [NSIndexPath]()
                        for ind in 0..<mod.getLength() {
                            rows.append(NSIndexPath(forRow: Int(startIndex + ind), inSection: 0))
                        }
                        self.collectionView.insertItemsAtIndexPaths(rows)
                    break
                    case AMChangeDescription_OperationType.REMOVE.rawValue:
                        var startIndex = Int(mod.getIndex())
                        var rows = [NSIndexPath]()
                        for ind in 0..<mod.getLength() {
                            rows.append(NSIndexPath(forRow: Int(startIndex + ind), inSection: 0))
                        }
                        self.collectionView.deleteItemsAtIndexPaths(rows)
                    break
                    case AMChangeDescription_OperationType.MOVE.rawValue:
                        self.collectionView.moveItemAtIndexPath(NSIndexPath(forItem: Int(mod.getIndex()), inSection: 0), toIndexPath: NSIndexPath(forItem: Int(mod.getIndex()), inSection: 0))
                    break
                    case AMChangeDescription_OperationType.UPDATE.rawValue:
                        var startIndex = Int(mod.getIndex())
                        var rows = [NSIndexPath]()
                        for ind in 0..<mod.getLength() {
                            rows.append(NSIndexPath(forRow: Int(startIndex + ind), inSection: 0))
                        }
                        self.collectionView.reloadItemsAtIndexPaths(rows)
                    break
                    default:
                    break
                }
                mod = modification.next()
            }
        }, completion: nil)
        
        isUpdating = false
        applyingUpdate = nil
        
        afterUpdated()
    }
    
    func afterUpdated() {
        
    }
}