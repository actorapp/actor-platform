//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit
import MediaPlayer
import AVKit
import AVFoundation

public class AAConversationContentController: SLKTextViewController, ARDisplayList_AppleChangeListener {

    public let peer: ACPeer
    
    private let delayLoad = false
    
    private let binder = AABinder()
    
    private var displayList: ARBindedDisplayList!
    private var isStarted: Bool = AADevice.isiPad
    private var isVisible: Bool = false
    private var isLoaded: Bool = false
    private var isLoadedAfter: Bool = false
    private var unreadIndex: Int? = nil
    private let collectionViewLayout = AAMessagesFlowLayout()
    private var prevCount: Int = 0
    private var unreadMessageId: jlong = 0
    
    private var isUpdating: Bool = false
    private var isBinded: Bool = false
    private var pendingUpdates = [ARAppleListUpdate]()
    private var readDate: jlong = 0
    private var receiveDate: jlong = 0
    
    // Audio notes
    public var voicePlayer : AAModernConversationAudioPlayer!
    public var voiceContext : AAModernViewInlineMediaContext!
    
    public var currentAudioFileId: jlong = 0
    public var voicesCache: Dictionary<jlong,Float> = Dictionary<jlong,Float>()
    
    public init(peer: ACPeer) {
        self.peer = peer
        
        super.init(collectionViewLayout: collectionViewLayout)
        
        self.collectionView.backgroundColor = UIColor.clearColor()
        self.collectionView.alwaysBounceVertical = true
  
        for layout in AABubbles.layouters {
            self.collectionView.registerClass(layout.cellClass(), forCellWithReuseIdentifier: layout.cellReuseId())
        }
    }
    
    public required init!(coder decoder: NSCoder!) {
        fatalError("Not implemented");
    }
    
    // Controller and UI lifecycle
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        if (self.displayList == nil) {
            self.displayList = displayListForController()
        }
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        isVisible = true
        
        // Hack for delaying collection view init from first animation frame
        // This dramatically speed up controller opening
        
        if delayLoad {
            
            if (isStarted) {
                tryBind()
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
                
                UIView.animateWithDuration(0.6, animations: { () -> Void in self.collectionView.alpha = 1 }, completion: { (comp) -> Void in })
                
                self.tryBind()
            })
        } else {
            self.isStarted = true
            
            UIView.animateWithDuration(0.6, animations: { () -> Void in self.collectionView.alpha = 1 }, completion: { (comp) -> Void in })
            
            tryBind()
        }
    }
    
    private func tryBind() {
        if !self.isBinded {
            
            self.binder.bind(Actor.getConversationVMWithACPeer(peer).getReadDate()) { (val: JavaLangLong!) in
                
                let nReadDate = val!.longLongValue()
                let oReadDate = self.readDate
                self.readDate = nReadDate
                
                if self.isBinded && nReadDate != oReadDate {
                    self.messageStatesUpdated(oReadDate, end: nReadDate)
                }
            }
            self.binder.bind(Actor.getConversationVMWithACPeer(peer).getReceiveDate()) { (val: JavaLangLong!) in
                
                let nReceiveDate = val!.longLongValue()
                let oReceiveDate = self.receiveDate
                self.receiveDate = nReceiveDate
                
                if self.isBinded && nReceiveDate != oReceiveDate {
                    self.messageStatesUpdated(oReceiveDate, end: nReceiveDate)
                }
            }
            
            self.isBinded = true
            self.willUpdate()
            self.collectionViewLayout.beginUpdates(false, list: self.displayList.getProcessedList() as? AAPreprocessedList, unread: self.unreadMessageId)
            self.collectionView.reloadData()
            self.prevCount = self.getCount()
            self.displayList.addAppleListener(self)
            self.didUpdate()
        }
    }
    
    
    public func buildCell(collectionView: UICollectionView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let list = getProcessedList()
        let layout = list!.layouts[indexPath.row]
        let cell = collectionView.dequeueReusableCellWithReuseIdentifier(layout.layouter.cellReuseId(), forIndexPath: indexPath)
        (cell as! AABubbleCell).setConfig(peer, controller: self)
        return cell
    }
    
    public func bindCell(collectionView: UICollectionView, cellForRowAtIndexPath indexPath: NSIndexPath, cell: UICollectionViewCell) {
        let list = getProcessedList()
        let message = list!.items[indexPath.row]
        let setting = list!.cellSettings[indexPath.row]
        let layout = list!.layouts[indexPath.row]
        let bubbleCell = (cell as! AABubbleCell)
        let isShowNewMessages = message.rid == unreadMessageId
        bubbleCell.performBind(message, receiveDate: receiveDate, readDate: readDate, setting: setting, isShowNewMessages: isShowNewMessages, layout: layout)
    }
    
    public func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAtIndex section: Int) -> UIEdgeInsets {
        return UIEdgeInsetsMake(6, 0, 100, 0)
    }
    
    public func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAtIndex section: Int) -> CGFloat {
        return 0
    }
    
    public func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAtIndex section: Int) -> CGFloat {
        return 0
    }
    
    public override func collectionView(collectionView: UICollectionView, canPerformAction action: Selector, forItemAtIndexPath indexPath: NSIndexPath, withSender sender: AnyObject!) -> Bool {
        return true
    }
    
    public override func collectionView(collectionView: UICollectionView, shouldShowMenuForItemAtIndexPath indexPath: NSIndexPath) -> Bool {
        return true
    }
    
    public override func collectionView(collectionView: UICollectionView, performAction action: Selector, forItemAtIndexPath indexPath: NSIndexPath, withSender sender: AnyObject!) {
        
    }
    
    func getProcessedList() -> AAPreprocessedList? {
        if self.displayList == nil {
            return nil
        }
        if !isStarted {
            return nil
        }
        
        return self.displayList.getProcessedList() as? AAPreprocessedList
    }
    
    public override func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return isStarted ? getCount() : 0
    }
    
    public override func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell = buildCell(collectionView, cellForRowAtIndexPath: indexPath)
        bindCell(collectionView, cellForRowAtIndexPath: indexPath, cell: cell)
        displayList.touchWithIndex(jint(indexPath.row))
        return cell
    }
    
    public override func collectionView(collectionView: UICollectionView, didUnhighlightItemAtIndexPath indexPath: NSIndexPath) {
        let cell = collectionView.cellForItemAtIndexPath(indexPath) as! AABubbleCell
        cell.updateView()
    }

    public override func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        let cell = collectionView.cellForItemAtIndexPath(indexPath) as! AABubbleCell
        cell.updateView()
    }
    
    public override func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated)
        
        isVisible = false
        
        if isBinded {
            isBinded = false
            
            // Remove listener on exit
            self.displayList.removeAppleListener(self)
            
            // Unbinding read/receive states
            self.binder.unbindAll()
        }
    }
    
    // Model updates
    
    func displayListForController() -> ARBindedDisplayList {
        let res = Actor.getMessageDisplayList(peer)
        if (res.getListProcessor() == nil) {
            res.setListProcessor(AAListProcessor(peer: peer))
        }
        return res
    }
    
    public func objectAtIndexPath(indexPath: NSIndexPath) -> AnyObject? {
        return objectAtIndex(indexPath.row)
    }
    
    public func objectAtIndex(index: Int) -> AnyObject? {
        return displayList.itemWithIndex(jint(index))
    }
    
    public func getCount() -> Int {
        if (isUpdating) {
            return self.prevCount
        }
        return Int(displayList.size())
    }
    
    public func onCollectionChangedWithChanges(modification: ARAppleListUpdate!) {
        
//        if isUpdating {
//            pendingUpdates.append(modification)
//            return
//        }
        
        if modification.isLoadMore {
            UIView.setAnimationsEnabled(false)
        }
        
//        NSLog("👮🏻 onCollectionChanged called was: \(prevCount)")
        
        self.willUpdate()
//        NSLog("👮🏻 willUpdate called")
        
        let list = self.displayList.getProcessedList() as? AAPreprocessedList
        
        var isAppliedList = false
        
        if modification.nonUpdateCount() > 0 {
            
            isUpdating = true
//            NSLog("👮🏻 batch updates")
            self.collectionView.performBatchUpdates({ () -> Void in
                
//                NSLog("👮🏻 batch started")
                
                // Removed rows
                if modification.removedCount() > 0 {
                    var rows = [NSIndexPath]()
                    for i in 0..<modification.removedCount() {
                        let removedRow = Int(modification.getRemoved(jint(i)))
                        rows.append(NSIndexPath(forRow: removedRow, inSection: 0))
//                        NSLog("👮🏻 removed \(removedRow)")
                    }
                    self.collectionView.deleteItemsAtIndexPaths(rows)
                }
                
                // Added rows
                if modification.addedCount() > 0 {
                    var rows = [NSIndexPath]()
                    for i in 0..<modification.addedCount() {
                        let insertedRow = Int(modification.getAdded(jint(i)))
                        rows.append(NSIndexPath(forRow: insertedRow, inSection: 0))
//                        print("👮🏻 inserted \(insertedRow)")
                    }
                    
                    self.collectionView.insertItemsAtIndexPaths(rows)
                }
                
                // Moved rows
                if modification.movedCount() > 0 {
                    for i in 0..<modification.movedCount() {
                        let mov = modification.getMoved(jint(i))
                        let sourceRow = Int(mov.getSourceIndex())
                        let destRow = Int(mov.getDestIndex())
                        self.collectionView.moveItemAtIndexPath(NSIndexPath(forRow: sourceRow, inSection: 0), toIndexPath: NSIndexPath(forRow: destRow, inSection: 0))
//                        NSLog("👮🏻 moved \(sourceRow) -> \(destRow)")
                    }
                }
                
                self.isUpdating = false
                self.prevCount = self.getCount()
                self.collectionViewLayout.beginUpdates(modification.isLoadMore, list: list, unread: self.unreadMessageId)
                isAppliedList = true
//                NSLog("👮🏻 batch updates:end \(self.prevCount)")
            }, completion: { (b) -> Void in
//                NSLog("👮🏻 batch updates:completion")
            })
//            NSLog("👮🏻 batch updates:after")
        }
        
        if !isAppliedList {
            self.collectionViewLayout.beginUpdates(modification.isLoadMore, list: list, unread: self.unreadMessageId)
            isAppliedList = true
        }
        
        var updated = [Int]()
        var updatedForce = [Int]()
        
        if modification.updatedCount() > 0 {
            for i in 0..<modification.updatedCount() {
                
                let updIndex = Int(modification.getUpdated(i))
                // Is forced update not marking as required for soft update
                if (list!.forceUpdated[updIndex]) {
                    continue
                }
                updated.append(updIndex)
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
                    // If already in list
                    if updated.contains(i) {
                       continue
                    }
                    updated.append(i)
                }
            }
        }
        
        var forcedRows = [NSIndexPath]()
        let visibleIndexes = self.collectionView.indexPathsForVisibleItems()
        for ind in updated {
            let indexPath = NSIndexPath(forRow: ind, inSection: 0)
            if visibleIndexes.contains(indexPath) {
                let cell = self.collectionView.cellForItemAtIndexPath(indexPath)
                self.bindCell(self.collectionView, cellForRowAtIndexPath: indexPath, cell: cell!)
            }
        }
        
        for ind in updatedForce {
            let indexPath = NSIndexPath(forRow: ind, inSection: 0)
            forcedRows.append(indexPath)
        }
        
        if (forcedRows.count > 0) {
            self.collectionViewLayout.beginUpdates(false, list: list, unread: unreadMessageId)
            self.collectionView.performBatchUpdates({ () -> Void in
                self.collectionView.reloadItemsAtIndexPaths(forcedRows)
            }, completion: nil)
        }
        
        self.didUpdate()
//        NSLog("👮🏻 didUpdate Called")
        
        if modification.isLoadMore {
            UIView.setAnimationsEnabled(true)
        }
    }
    
    private func completeUpdates(modification: ARAppleListUpdate!) {
        
    }
    
    private func messageStatesUpdated(start: jlong, end: jlong) {
        let visibleIndexes = self.collectionView.indexPathsForVisibleItems()
        for ind in visibleIndexes {
            if let obj = objectAtIndex(ind.row) {
                if obj.senderId == Actor.myUid() && obj.sortDate >= start && obj.sortDate <= end {
                    let cell = self.collectionView.cellForItemAtIndexPath(ind)
                    self.bindCell(self.collectionView, cellForRowAtIndexPath: ind, cell: cell!)
                }
            }
        }
    }
    
    public func willUpdate() {
        isLoadedAfter = false
        if getCount() > 0 && !isLoaded {
            isLoaded = true
            isLoadedAfter = true
            
            let readState = Actor.loadFirstUnread(peer)
           
            if readState > 0 {
                for i in 0..<getCount() {
                    let ind = getCount() - 1 - i
                    let item = objectAtIndex(ind) as! ACMessage
                
                    if item.senderId != Actor.myUid() {
                        if readState < item.sortDate {
                            unreadIndex = ind
                            unreadMessageId = item.rid
                            break
                        }
                    }
                }
            }
        }
    }
    
    public func didUpdate() {
        if isLoadedAfter {
            if unreadIndex != nil {
                self.collectionView.scrollToItemAtIndexPath(NSIndexPath(forItem: unreadIndex!, inSection: 0), atScrollPosition: UICollectionViewScrollPosition.Bottom, animated: false)
                unreadIndex = nil
            }
        }
    }
    
    public func onBubbleAvatarTap(view: UIView, uid: jint) {
        var controller: AAViewController! = ActorSDK.sharedActor().delegate.actorControllerForUser(Int(uid))
        if controller == nil {
            controller = AAUserViewController(uid: Int(uid))
        }
        if (AADevice.isiPad) {
            let navigation = AANavigationController()
            navigation.viewControllers = [controller]
            let popover = UIPopoverController(contentViewController:  navigation)
            controller.popover = popover
            popover.presentPopoverFromRect(view.bounds, inView: view, permittedArrowDirections: UIPopoverArrowDirection.Any, animated: true)
        } else {
            navigateNext(controller, removeCurrent: false)
        }
    }
    
    public override func viewWillTransitionToSize(size: CGSize, withTransitionCoordinator coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransitionToSize(size, withTransitionCoordinator: coordinator)
        
        dispatch_async(dispatch_get_main_queue(), { () -> Void in
//            self.collectionView.collectionViewLayout.invalidateLayout()
            self.collectionView.performBatchUpdates(nil, completion: nil)
        })
    }
    
    ///////////////////////
    // MARK: - audio play
    ///////////////////////
    
    
    func playVoiceFromPath(path:String,fileId:jlong,position:Float) {
        
        if (self.currentAudioFileId != fileId) {
            
            self.voicePlayer?.stop()
            self.voicePlayer?.audioPlayerStopAndFinish()
            
            self.voicesCache[self.currentAudioFileId] = 0.0
            
            self.voicePlayer = AAModernConversationAudioPlayer(filePath:path)
            self.voiceContext = self.voicePlayer.inlineMediaContext()
            
            self.voicePlayer?.play()
            
            self.currentAudioFileId = fileId
            
        } else {
            
            
            if (position == 0.0  || position == 0) {
                
                self.voicePlayer = AAModernConversationAudioPlayer(filePath:path)
                self.voiceContext = self.voicePlayer.inlineMediaContext()
                
                self.voicePlayer?.play()
                
            } else {
                
                if self.voicePlayer?.isPaused() == false {
                    self.voicePlayer?.pause()
                } else {
                    self.voicePlayer?.play()
                }
                
            }
            
        }
        
    }
    
    func playVideoFromPath(path:String) {
        
        let player = AVPlayer(URL: NSURL(fileURLWithPath: path))
        let playerController = AVPlayerViewController()
        playerController.player = player
        self.presentViewController(playerController, animated: true) {
            player.play()
        }
        
    }
    
}