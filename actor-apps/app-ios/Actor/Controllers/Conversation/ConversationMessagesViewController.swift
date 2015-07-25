//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit;

class ConversationBaseViewController: SLKTextViewController, MessagesLayoutDelegate, AMDisplayList_AndroidChangeListener {

    private var displayList: AMBindedDisplayList!
    private var applyingUpdate: AMAndroidListUpdate?
    private var isStarted: Bool = isIPad
    private var isUpdating: Bool = false
    private var isVisible: Bool = false
    private var isLoaded: Bool = false
    private var isLoadedAfter: Bool = false
    private var unreadIndex: Int? = nil
    private let layout = MessagesLayout()
    let peer: AMPeer
    
    init(peer: AMPeer) {
        self.peer = peer
        
        super.init(collectionViewLayout: layout)
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
        
        self.collectionView.contentInset = UIEdgeInsets(top: 4, left: 0, bottom: 200, right: 0)
        
        isVisible = true
        
        // Hack for delaying collection view init from first animation frame
        // This dramatically speed up controller opening
        
        if (isStarted) {
            self.willUpdate()
            self.collectionView.reloadData()
            self.displayList.addAndroidListener(self)
            self.didUpdate()
            return
        } else {
            self.collectionView.alpha = 0
        }
        
        dispatch_async(dispatch_get_main_queue(),{
            // What if controller is already closed?
            if (!self.isVisible) {
                return
            }
            
            self.isStarted = true
            UIView.animateWithDuration(0.15, animations: { () -> Void in
                self.collectionView.alpha = 1
            })
            
            self.willUpdate()
            self.collectionView.reloadData()
            self.displayList.addAndroidListener(self)
            self.didUpdate()
        });
    }
    
    func buildCell(collectionView: UICollectionView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?)  -> UICollectionViewCell {
        fatalError("Not implemented")
    }
    
    func bindCell(collectionView: UICollectionView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?, cell: UICollectionViewCell) {
        fatalError("Not implemented")
    }
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, gravityForItemAtIndexPath indexPath: NSIndexPath) -> MessageGravity {
        fatalError("Not implemented")
    }
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, idForItemAtIndexPath indexPath: NSIndexPath) -> Int64 {
        fatalError("Not implemented")
    }
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAtIndexPath indexPath: NSIndexPath) -> CGSize {
        fatalError("Not implemented")
    }
    
    override func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return isStarted ? getCount() : 0
    }
    
    override func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        var item: AnyObject? = objectAtIndexPath(indexPath)
        var cell = buildCell(collectionView, cellForRowAtIndexPath:indexPath, item:item)
        bindCell(collectionView, cellForRowAtIndexPath: indexPath, item: item, cell: cell)
        displayList.touchWithIndex(jint(indexPath.row))
//        cell.contentView.transform = collectionView.transform
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
    
    func needFullReload(item: AnyObject?, cell: UICollectionViewCell) -> Bool {
        return false
    }
    
    func onItemsAdded(indexes: [Int]) {
        
    }
    
    func onItemsRemoved(indexes: [Int]) {
        
    }
    
    func onItemsUpdated(indexes: [Int]) {
        
    }
    
    func onItemMoved(fromIndex: Int, toIndex: Int) {
        
    }
    
    override func willRotateToInterfaceOrientation(toInterfaceOrientation: UIInterfaceOrientation, duration: NSTimeInterval) {
        super.willRotateToInterfaceOrientation(toInterfaceOrientation, duration: duration)

        dispatch_async(dispatch_get_main_queue(), { () -> Void in
            self.collectionView.collectionViewLayout.invalidateLayout()
        })
    }
    
    func onCollectionChangedWithChanges(modification: AMAndroidListUpdate!) {
        
        self.willUpdate()
        
        isUpdating = true
        applyingUpdate = modification
        
        if modification.isLoadMore() {
            UIView.setAnimationsEnabled(false)
        }
        self.layout.beginUpdates(modification.isLoadMore())
        
        self.collectionView.performBatchUpdates({ () -> Void in
            var mod = modification.next()
            println("doUpdate \(mod)")
            while(mod != nil) {
                switch(UInt(mod.getOperationType().ordinal())) {
                case AMChangeDescription_OperationType.ADD.rawValue:
                    var startIndex = Int(mod.getIndex())
                    var rows = [NSIndexPath]()
                    var indexes = [Int]()
                    for ind in 0..<mod.getLength() {
                        indexes.append(Int(startIndex + ind))
                        rows.append(NSIndexPath(forRow: Int(startIndex + ind), inSection: 0))
                    }
                    self.collectionView.insertItemsAtIndexPaths(rows)
                    // self.onItemsAdded(indexes)
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
                    self.collectionView.moveItemAtIndexPath(NSIndexPath(forItem: Int(mod.getIndex()), inSection: 0), toIndexPath: NSIndexPath(forItem: Int(mod.getDestIndex()), inSection: 0))
                    break
                case AMChangeDescription_OperationType.UPDATE.rawValue:
                    var rows = [Int]()
                    var startIndex = Int(mod.getIndex())
                    for ind in 0..<mod.getLength() {
                        rows.append(Int(startIndex + ind))
                    }
                    self.updateRows(rows)
                    break
                default:
                    break
                }
                mod = modification.next()
            }
            }, completion: nil)
        if modification.isLoadMore() {
            UIView.setAnimationsEnabled(true)
        }
        
        isUpdating = false
        applyingUpdate = nil
        
        self.didUpdate()
    }
    
    func updateRows(indexes: [Int]) {
        var forcedRows = [NSIndexPath]()
        
        var visibleIndexes = self.collectionView.indexPathsForVisibleItems() as! [NSIndexPath]
        for ind in indexes {
            var indexPath = NSIndexPath(forRow: ind, inSection: 0)
            if visibleIndexes.contains(indexPath) {
                var cell = self.collectionView.cellForItemAtIndexPath(indexPath)
                var item: AnyObject? = self.objectAtIndexPath(indexPath)
                if !self.needFullReload(item, cell: cell!) {
                    self.bindCell(self.collectionView, cellForRowAtIndexPath: indexPath, item: item, cell: cell!)
                    continue
                }
            }
            
            forcedRows.append(indexPath)
        }
        
        if (forcedRows.count > 0) {
            self.layout.beginUpdates(false)
            self.collectionView.reloadItemsAtIndexPaths(forcedRows)
            // self.layout.endUpdates()
        }
    }
    
    func willUpdate() {
        isLoadedAfter = false
        if getCount() > 0 && !isLoaded {
            isLoaded = true
            isLoadedAfter = true
            
            var readState = MSG.loadLastReadState(peer)
            
            if readState > 0 {
                for i in 0..<getCount() {
                    var ind = getCount() - 1 - i
                    var item = objectAtIndex(ind)!
                
                    if item.getSenderId() != MSG.myUid() {
                        if readState < item.getSortDate() {
                            unreadIndex = ind
                            setUnread(item.getRid())
                            break
                        }
                    }
                }
            }
        }
    }
    
    func didUpdate() {
        if isLoadedAfter {
            if unreadIndex != nil {
                self.collectionView.scrollToItemAtIndexPath(NSIndexPath(forItem: unreadIndex!, inSection: 0), atScrollPosition: UICollectionViewScrollPosition.CenteredVertically, animated: false)
            }
        }
    }
    
    func setUnread(rid: jlong) {
        
    }
}