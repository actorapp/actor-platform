//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit
import MediaPlayer
import AVKit
import AVFoundation

open class AAConversationContentController: SLKTextViewController, ARDisplayList_AppleChangeListener {

    open let peer: ACPeer
    
    fileprivate let delayLoad = false
    
    fileprivate let binder = AABinder()
    
    fileprivate var displayList: ARBindedDisplayList!
    fileprivate var isStarted: Bool = AADevice.isiPad
    fileprivate var isVisible: Bool = false
    fileprivate var isLoaded: Bool = false
    fileprivate var isLoadedAfter: Bool = false
    fileprivate var unreadIndex: Int? = nil
    fileprivate let collectionViewLayout = AAMessagesFlowLayout()
    fileprivate var prevCount: Int = 0
    fileprivate var unreadMessageId: jlong = 0
    
    fileprivate var isUpdating: Bool = false
    fileprivate var isBinded: Bool = false
    fileprivate var pendingUpdates = [ARAppleListUpdate]()
    fileprivate var readDate: jlong = 0
    fileprivate var receiveDate: jlong = 0
    
    // Audio notes
    open var voicePlayer : AAModernConversationAudioPlayer!
    open var voiceContext : AAModernViewInlineMediaContext!
    
    open var currentAudioFileId: jlong = 0
    open var voicesCache: Dictionary<jlong,Float> = Dictionary<jlong,Float>()
    
    public init(peer: ACPeer) {
        self.peer = peer
        
        super.init(collectionViewLayout: collectionViewLayout)
        
        self.collectionView.backgroundColor = UIColor.clear
        self.collectionView.alwaysBounceVertical = true
  
        for layout in AABubbles.layouters {
            self.collectionView.register(layout.cellClass(), forCellWithReuseIdentifier: layout.cellReuseId())
        }
    }
    
    public required init!(coder decoder: NSCoder!) {
        fatalError("Not implemented");
    }
    
    // Controller and UI lifecycle
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        if (self.displayList == nil) {
            self.displayList = displayListForController()
        }
    }
    
    open override func viewWillAppear(_ animated: Bool) {
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
            
            DispatchQueue.main.async(execute: {
                // What if controller is already closed?
                if (!self.isVisible) {
                    return
                }
                
                self.isStarted = true
                
                UIView.animate(withDuration: 0.6, animations: { () -> Void in self.collectionView.alpha = 1 }, completion: { (comp) -> Void in })
                
                self.tryBind()
            })
        } else {
            self.isStarted = true
            
            UIView.animate(withDuration: 0.6, animations: { () -> Void in self.collectionView.alpha = 1 }, completion: { (comp) -> Void in })
            
            tryBind()
        }
    }
    
    fileprivate func tryBind() {
        if !self.isBinded {
            
            self.binder.bind(Actor.getConversationVM(with: peer).getReadDate()) { (val: JavaLangLong?) in
                
                let nReadDate = val!.longLongValue()
                let oReadDate = self.readDate
                self.readDate = nReadDate
                
                if self.isBinded && nReadDate != oReadDate {
                    self.messageStatesUpdated(oReadDate, end: nReadDate)
                }
            }
            self.binder.bind(Actor.getConversationVM(with: peer).getReceiveDate()) { (val: JavaLangLong?) in
                
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
    
    
    open func buildCell(_ collectionView: UICollectionView, cellForRowAtIndexPath indexPath: IndexPath) -> UICollectionViewCell {
        let list = getProcessedList()
        let layout = list!.layouts[(indexPath as NSIndexPath).row]
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: layout.layouter.cellReuseId(), for: indexPath)
        (cell as! AABubbleCell).setConfig(peer, controller: self)
        return cell
    }
    
    open func bindCell(_ collectionView: UICollectionView, cellForRowAtIndexPath indexPath: IndexPath, cell: UICollectionViewCell) {
        let list = getProcessedList()
        let message = list!.items[(indexPath as NSIndexPath).row]
        let setting = list!.cellSettings[(indexPath as NSIndexPath).row]
        let layout = list!.layouts[(indexPath as NSIndexPath).row]
        let bubbleCell = (cell as! AABubbleCell)
        let isShowNewMessages = message.rid == unreadMessageId
        bubbleCell.performBind(message, receiveDate: receiveDate, readDate: readDate, setting: setting, isShowNewMessages: isShowNewMessages, layout: layout)
    }
    
    open func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAtIndex section: Int) -> UIEdgeInsets {
        return UIEdgeInsetsMake(6, 0, 100, 0)
    }
    
    open func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAtIndex section: Int) -> CGFloat {
        return 0
    }
    
    open func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAtIndex section: Int) -> CGFloat {
        return 0
    }
    
    open override func collectionView(_ collectionView: UICollectionView, canPerformAction action: Selector, forItemAt indexPath: IndexPath, withSender sender: Any!) -> Bool {
        return true
    }
    
    open override func collectionView(_ collectionView: UICollectionView, shouldShowMenuForItemAt indexPath: IndexPath) -> Bool {
        return true
    }
    
    open override func collectionView(_ collectionView: UICollectionView, performAction action: Selector, forItemAt indexPath: IndexPath, withSender sender: Any!) {
        
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
    
    open override func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return isStarted ? getCount() : 0
    }
    
    open override func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = buildCell(collectionView, cellForRowAtIndexPath: indexPath)
        bindCell(collectionView, cellForRowAtIndexPath: indexPath, cell: cell)
        displayList.touch(with: jint((indexPath as NSIndexPath).row))
        return cell
    }
    
    open override func collectionView(_ collectionView: UICollectionView, didUnhighlightItemAt indexPath: IndexPath) {
        let cell = collectionView.cellForItem(at: indexPath) as! AABubbleCell
        cell.updateView()
    }

    open override func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let cell = collectionView.cellForItem(at: indexPath) as! AABubbleCell
        cell.updateView()
    }
    
    open override func viewDidDisappear(_ animated: Bool) {
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
        if (res?.getProcessor() == nil) {
            res?.setListProcessor(AAListProcessor(peer: peer))
        }
        return res!
    }
    
    open func objectAtIndexPath(_ indexPath: IndexPath) -> AnyObject? {
        return objectAtIndex((indexPath as NSIndexPath).row)
    }
    
    open func objectAtIndex(_ index: Int) -> AnyObject? {
        return displayList.item(with: jint(index)) as AnyObject?
    }
    
    open func getCount() -> Int {
        if (isUpdating) {
            return self.prevCount
        }
        return Int(displayList.size())
    }
    
    open func onCollectionChanged(withChanges modification: ARAppleListUpdate!) {
        
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
                    var rows = [IndexPath]()
                    for i in 0..<modification.removedCount() {
                        let removedRow = Int(modification.getRemoved(jint(i)))
                        rows.append(IndexPath(row: removedRow, section: 0))
//                        NSLog("👮🏻 removed \(removedRow)")
                    }
                    self.collectionView.deleteItems(at: rows)
                }
                
                // Added rows
                if modification.addedCount() > 0 {
                    var rows = [IndexPath]()
                    for i in 0..<modification.addedCount() {
                        let insertedRow = Int(modification.getAdded(jint(i)))
                        rows.append(IndexPath(row: insertedRow, section: 0))
//                        print("👮🏻 inserted \(insertedRow)")
                    }
                    
                    self.collectionView.insertItems(at: rows)
                }
                
                // Moved rows
                if modification.movedCount() > 0 {
                    for i in 0..<modification.movedCount() {
                        let mov = modification.getMoved(jint(i))
                        let sourceRow = Int((mov?.getSourceIndex())!)
                        let destRow = Int((mov?.getDestIndex())!)
                        self.collectionView.moveItem(at: IndexPath(row: sourceRow, section: 0), to: IndexPath(row: destRow, section: 0))
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
        
        var forcedRows = [IndexPath]()
        let visibleIndexes = self.collectionView.indexPathsForVisibleItems
        for ind in updated {
            let indexPath = IndexPath(row: ind, section: 0)
            if visibleIndexes.contains(indexPath) {
                let cell = self.collectionView.cellForItem(at: indexPath)
                self.bindCell(self.collectionView, cellForRowAtIndexPath: indexPath, cell: cell!)
            }
        }
        
        for ind in updatedForce {
            let indexPath = IndexPath(row: ind, section: 0)
            forcedRows.append(indexPath)
        }
        
        if (forcedRows.count > 0) {
            self.collectionViewLayout.beginUpdates(false, list: list, unread: unreadMessageId)
            self.collectionView.performBatchUpdates({ () -> Void in
                self.collectionView.reloadItems(at: forcedRows)
            }, completion: nil)
        }
        
        self.didUpdate()
//        NSLog("👮🏻 didUpdate Called")
        
        if modification.isLoadMore {
            UIView.setAnimationsEnabled(true)
        }
    }
    
    fileprivate func completeUpdates(_ modification: ARAppleListUpdate!) {
        
    }
    
    fileprivate func messageStatesUpdated(_ start: jlong, end: jlong) {
        let visibleIndexes = self.collectionView.indexPathsForVisibleItems
        for ind in visibleIndexes {
            if let obj = objectAtIndex((ind as NSIndexPath).row) {
                if obj.senderId == Actor.myUid() && obj.sortDate >= start && obj.sortDate <= end {
                    let cell = self.collectionView.cellForItem(at: ind)
                    self.bindCell(self.collectionView, cellForRowAtIndexPath: ind, cell: cell!)
                }
            }
        }
    }
    
    open func willUpdate() {
        isLoadedAfter = false
        if getCount() > 0 && !isLoaded {
            isLoaded = true
            isLoadedAfter = true
            
            let readState = Actor.loadLastMessageDate(peer)
           
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
    
    open func didUpdate() {
        if isLoadedAfter {
            if unreadIndex != nil {
                self.collectionView.scrollToItem(at: IndexPath(item: unreadIndex!, section: 0), at: UICollectionViewScrollPosition.bottom, animated: false)
                unreadIndex = nil
            }
        }
    }
    
    open func onBubbleAvatarTap(_ view: UIView, uid: jint) {
        var controller: AAViewController! = ActorSDK.sharedActor().delegate.actorControllerForUser(Int(uid))
        if controller == nil {
            controller = AAUserViewController(uid: Int(uid))
        }
        if (AADevice.isiPad) {
            let navigation = AANavigationController()
            navigation.viewControllers = [controller]
            let popover = UIPopoverController(contentViewController:  navigation)
            controller.popover = popover
            popover.present(from: view.bounds, in: view, permittedArrowDirections: UIPopoverArrowDirection.any, animated: true)
        } else {
            navigateNext(controller, removeCurrent: false)
        }
    }
    
    open override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to: size, with: coordinator)
        
        DispatchQueue.main.async(execute: { () -> Void in
//            self.collectionView.collectionViewLayout.invalidateLayout()
            self.collectionView.performBatchUpdates(nil, completion: nil)
        })
    }
    
    ///////////////////////
    // MARK: - audio play
    ///////////////////////
    
    
    func playVoiceFromPath(_ path:String,fileId:jlong,position:Float) {
        
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
    
    func playVideoFromPath(_ path:String) {
        
        let player = AVPlayer(url: URL(fileURLWithPath: path))
        let playerController = AVPlayerViewController()
        playerController.player = player
        self.present(playerController, animated: true) {
            player.play()
        }
        
    }
    
}
