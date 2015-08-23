//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit;

class ConversationBaseViewController: SLKTextViewController, ARDisplayList_AppleChangeListener {

    private let BubbleTextIdentifier = "BubbleTextIdentifier"
    private let BubbleMediaIdentifier = "BubbleMediaIdentifier"
    private let BubbleDocumentIdentifier = "BubbleDocumentIdentifier"
    private let BubbleServiceIdentifier = "BubbleServiceIdentifier"
    private let BubbleBannerIdentifier = "BubbleBannerIdentifier"
    
    let peer: ACPeer
    
    private var displayList: ARBindedDisplayList!
    private var isStarted: Bool = isIPad
    private var isUpdating: Bool = false
    private var isVisible: Bool = false
    private var isLoaded: Bool = false
    private var isLoadedAfter: Bool = false
    private var unreadIndex: Int? = nil
    private let layout = MessagesLayout()
    private var prevCount: Int = 0
    private var unreadMessageId: jlong = 0
    
    init(peer: ACPeer) {
        self.peer = peer
        
        super.init(collectionViewLayout: layout)
        
        self.collectionView.registerClass(AABubbleTextCell.self, forCellWithReuseIdentifier: BubbleTextIdentifier)
        self.collectionView.registerClass(AABubbleMediaCell.self, forCellWithReuseIdentifier: BubbleMediaIdentifier)
        self.collectionView.registerClass(AABubbleDocumentCell.self, forCellWithReuseIdentifier: BubbleDocumentIdentifier)
        self.collectionView.registerClass(AABubbleServiceCell.self, forCellWithReuseIdentifier: BubbleServiceIdentifier)
        self.collectionView.backgroundColor = UIColor.clearColor()
        self.collectionView.alwaysBounceVertical = true
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
        
        println("viewWillAppear")
        
        self.collectionView.contentInset = UIEdgeInsets(top: 4, left: 0, bottom: 200, right: 0)
        
        isVisible = true
        
        // Hack for delaying collection view init from first animation frame
        // This dramatically speed up controller opening
        
        if (isStarted) {
            self.willUpdate()
            self.layout.beginUpdates(false, list: self.displayList.getProcessedList() as? PreprocessedList, unread: unreadMessageId)
            self.collectionView.reloadData()
            prevCount = getCount()
            self.displayList.addAppleListener(self)
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
            self.layout.beginUpdates(false, list: self.displayList.getProcessedList() as? PreprocessedList, unread: self.unreadMessageId)
            self.collectionView.reloadData()
            self.prevCount = self.getCount()
            self.displayList.addAppleListener(self)
            self.didUpdate()
        });
    }
    
    
    func buildCell(collectionView: UICollectionView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?) -> UICollectionViewCell {
        var message = (item as! ACMessage);
        var cell: AABubbleCell
        if (message.content is ACTextContent) {
            cell = collectionView.dequeueReusableCellWithReuseIdentifier(BubbleTextIdentifier, forIndexPath: indexPath) as! AABubbleTextCell
        } else if (message.content is ACPhotoContent) {
            cell = collectionView.dequeueReusableCellWithReuseIdentifier(BubbleMediaIdentifier, forIndexPath: indexPath) as! AABubbleMediaCell
        } else if (message.content is ACDocumentContent) {
            cell = collectionView.dequeueReusableCellWithReuseIdentifier(BubbleDocumentIdentifier, forIndexPath: indexPath) as! AABubbleDocumentCell
        } else if (message.content is ACServiceContent){
            cell = collectionView.dequeueReusableCellWithReuseIdentifier(BubbleServiceIdentifier, forIndexPath: indexPath) as! AABubbleServiceCell
        } else {
            cell = collectionView.dequeueReusableCellWithReuseIdentifier(BubbleTextIdentifier, forIndexPath: indexPath) as! AABubbleTextCell
        }
        cell.setConfig(peer, controller: self)
        return cell
    }
    
    func bindCell(collectionView: UICollectionView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?, cell: UICollectionViewCell) {
        var list = getProcessedList()
        var message = list!.items[indexPath.row]
        var setting = list!.cellSettings[indexPath.row]
        var bubbleCell = (cell as! AABubbleCell)
        bubbleCell.performBind(message, setting: setting, isShowNewMessages: message.rid == unreadMessageId, layoutCache: list!.layoutCache)
    }
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAtIndex section: Int) -> UIEdgeInsets {
        return UIEdgeInsetsMake(6, 0, 100, 0)
    }
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAtIndex section: Int) -> CGFloat {
        return 0
    }
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAtIndex section: Int) -> CGFloat {
        return 0
    }
    
    override func collectionView(collectionView: UICollectionView, canPerformAction action: Selector, forItemAtIndexPath indexPath: NSIndexPath, withSender sender: AnyObject!) -> Bool {
        return true
    }
    
    override func collectionView(collectionView: UICollectionView, shouldShowMenuForItemAtIndexPath indexPath: NSIndexPath) -> Bool {
        return true
    }
    
    override func collectionView(collectionView: UICollectionView, performAction action: Selector, forItemAtIndexPath indexPath: NSIndexPath, withSender sender: AnyObject!) {
        
    }
    
    func getProcessedList() -> PreprocessedList? {
        if self.displayList == nil {
            return nil
        }
        if !isStarted {
            return nil
        }
        
        return self.displayList.getProcessedList() as? PreprocessedList
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
        self.displayList.removeAppleListener(self)
    }
    
    // Model updates
    
    func displayListForController() -> ARBindedDisplayList {
        var res = Actor.getMessageDisplayList(peer)
        if (res.getListProcessor() == nil) {
            let group = peer.getPeerType().ordinal() == jint(ACPeerType.GROUP.rawValue)
            res.setListProcessor(ListProcessor(isGroup: group))
        }
        return res
    }
    
    func objectAtIndexPath(indexPath: NSIndexPath) -> AnyObject? {
        return objectAtIndex(indexPath.row)
    }
    
    func objectAtIndex(index: Int) -> AnyObject? {
        return displayList.itemWithIndex(jint(index))
    }
    
    func getCount() -> Int {
        if (isUpdating) {
            return self.prevCount
        }
        return Int(displayList.size())
    }
    
    func needFullReload(item: AnyObject?, cell: UICollectionViewCell) -> Bool {
        return false
    }
    
    func onCollectionChangedWithChanges(modification: ARAppleListUpdate!) {

        var start = CFAbsoluteTimeGetCurrent()
        
        if modification.isLoadMore {
            UIView.setAnimationsEnabled(false)
        }
        
        self.willUpdate()
        var list = self.displayList.getProcessedList() as? PreprocessedList
        self.layout.beginUpdates(modification.isLoadMore, list: list, unread: unreadMessageId)
        var changedRows = Set<Int>()
        
        if modification.nonUpdateCount() > 0 {
            isUpdating = true
            self.collectionView.performBatchUpdates({ () -> Void in
                // Removed rows
                if modification.removedCount() > 0 {
                    var rows: NSMutableArray = []
                    for i in 0..<modification.removedCount() {
                        rows.addObject(NSIndexPath(forRow: Int(modification.getRemoved(jint(i))), inSection: 0))
                    }
                    self.collectionView.deleteItemsAtIndexPaths(rows as [AnyObject])
                }
                
                // Added rows
                if modification.addedCount() > 0 {
                    var rows: NSMutableArray = []
                    for i in 0..<modification.addedCount() {
                        rows.addObject(NSIndexPath(forRow: Int(modification.getAdded(jint(i))), inSection: 0))
                    }
                    self.collectionView.insertItemsAtIndexPaths(rows as [AnyObject])
                }
                
                // Moved rows
                if modification.movedCount() > 0 {
                    for i in 0..<modification.movedCount() {
                        var mov = modification.getMoved(jint(i))
                        self.collectionView.moveItemAtIndexPath(NSIndexPath(forRow: Int(mov.getSourceIndex()), inSection: 0), toIndexPath: NSIndexPath(forRow: Int(mov.getDestIndex()), inSection: 0))
                    }
                }
                
                self.isUpdating = false
                self.prevCount = self.getCount()
            }, completion: nil)
        }
        
        var updated = [Int]()
        var updatedForce = [Int]()
        
        if modification.updatedCount() > 0 {
            for i in 0..<modification.updatedCount() {
                updated.append(Int(modification.getUpdated(i)))
            }
        }
        
        if list != nil {
            for i in 0..<list!.forceUpdated.count {
                if list!.forceUpdated[i] {
                    updatedForce.append(i)
                }
            }
            for i in 0..<list!.updated.count {
                if list!.updated[i] {
                    updated.append(i)
                }
            }
        }
        
        var forcedRows = [NSIndexPath]()
        var visibleIndexes = self.collectionView.indexPathsForVisibleItems() as! [NSIndexPath]
        for ind in updated {
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
        
        for ind in updatedForce {
            var indexPath = NSIndexPath(forRow: ind, inSection: 0)
            forcedRows.append(indexPath)
        }
        
        if (forcedRows.count > 0) {
            self.layout.beginUpdates(false, list: list, unread: unreadMessageId)
            self.collectionView.reloadItemsAtIndexPaths(forcedRows)
        }
        
        self.didUpdate()
        
        if modification.isLoadMore {
            UIView.setAnimationsEnabled(true)
        }
        
        println("collectionChanged: \(CFAbsoluteTimeGetCurrent() - start)")
    }
    
    func willUpdate() {
        isLoadedAfter = false
        if getCount() > 0 && !isLoaded {
            isLoaded = true
            isLoadedAfter = true
            
            var readState = Actor.loadFirstUnread(peer)
           
            if readState > 0 {
                for i in 0..<getCount() {
                    var ind = getCount() - 1 - i
                    var item = objectAtIndex(ind)!
                
                    if item.getSenderId() != Actor.myUid() {
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
                unreadIndex = nil
            }
        }
    }
    
    func setUnread(rid: jlong) {
        self.unreadMessageId = rid
    }
    
    func onBubbleAvatarTap(view: UIView, uid: jint) {
        var controller = UserViewController(uid: Int(uid))
        if (isIPad) {
            var navigation = AANavigationController()
            navigation.viewControllers = [controller]
            var popover = UIPopoverController(contentViewController:  navigation)
            controller.popover = popover
            popover.presentPopoverFromRect(view.bounds, inView: view, permittedArrowDirections: UIPopoverArrowDirection.Any, animated: true)
        } else {
            navigateNext(controller, removeCurrent: false)
        }
    }
    
    override func willRotateToInterfaceOrientation(toInterfaceOrientation: UIInterfaceOrientation, duration: NSTimeInterval) {
        super.willRotateToInterfaceOrientation(toInterfaceOrientation, duration: duration)
        
        dispatch_async(dispatch_get_main_queue(), { () -> Void in
            self.collectionView.collectionViewLayout.invalidateLayout()
        })
    }
}