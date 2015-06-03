//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit
import MobileCoreServices

class AAConversationController: EngineSlackListController {
    
    // MARK: -
    // MARK: Private vars
    
    private let BubbleTextIdentifier = "BubbleTextIdentifier"
    private let BubbleMediaIdentifier = "BubbleMediaIdentifier"
    private let BubbleDocumentIdentifier = "BubbleDocumentIdentifier"
    private let BubbleServiceIdentifier = "BubbleServiceIdentifier"
    private let BubbleBannerIdentifier = "BubbleBannerIdentifier"
    
    private let titleView: UILabel = UILabel();
    private let subtitleView: UILabel = UILabel();
    private let navigationView: UIView = UIView();
    
    private let avatarView = BarAvatarView(frameSize: 36, type: AAAvatarType.Rounded)
    
    private let backgroundView: UIView = UIView()
    
    // MARK: -
    // MARK: Public vars
    
    var peer: AMPeer!;
    let binder: Binder = Binder();
    
    var unreadMessageId: jlong = 0
    
    // MARK: -
    // MARK: Constructors
    
    init(peer: AMPeer) {
        super.init(isInverted: true);
        
        // Hack for fixing top offsets
        // self.edgesForExtendedLayout = UIRectEdge.All ^ UIRectEdge.Top;
        
        self.peer = peer;
        self.tableView.separatorStyle = UITableViewCellSeparatorStyle.None;
        self.tableView.backgroundColor = UIColor.clearColor();
        self.tableView.allowsSelection = false;
        self.tableView.tableHeaderView = UIView(frame:CGRectMake(0, 0, 100, 6));
        
        self.textInputbar.backgroundColor = MainAppTheme.chat.chatField
        self.textInputbar.autoHideRightButton = false;
        self.textView.placeholder = NSLocalizedString("ChatPlaceholder",comment: "Placeholder")
        self.rightButton.titleLabel?.text = NSLocalizedString("ChatSend",comment: "Send")
        self.rightButton.setTitleColor(MainAppTheme.chat.sendEnabled, forState: UIControlState.Normal)
        self.rightButton.setTitleColor(MainAppTheme.chat.sendDisabled, forState: UIControlState.Disabled)
        
        self.keyboardPanningEnabled = true;
        
        self.textView.keyboardAppearance = MainAppTheme.common.isDarkKeyboard ? UIKeyboardAppearance.Dark : UIKeyboardAppearance.Light

        self.leftButton.setImage(UIImage(named: "conv_attach")!
            .tintImage(MainAppTheme.chat.attachColor)
            .imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal),
            forState: UIControlState.Normal)
        
        // Title
        
        navigationView.frame = CGRectMake(0, 0, 190, 44);
        navigationView.autoresizingMask = UIViewAutoresizing.FlexibleWidth;
        
        titleView.frame = CGRectMake(0, 4, 190, 20)
        titleView.font = UIFont(name: "HelveticaNeue-Medium", size: 17)!
        titleView.adjustsFontSizeToFitWidth = false;
        titleView.textColor = Resources.PrimaryLightText
        titleView.textAlignment = NSTextAlignment.Center;
        titleView.lineBreakMode = NSLineBreakMode.ByTruncatingTail;
        titleView.autoresizingMask = UIViewAutoresizing.FlexibleWidth;
        
        subtitleView.frame = CGRectMake(0, 22, 190, 20);
        subtitleView.font = UIFont.systemFontOfSize(13);
        subtitleView.adjustsFontSizeToFitWidth=false;
        subtitleView.textColor = Resources.SecondaryLightText
        subtitleView.textAlignment = NSTextAlignment.Center;
        subtitleView.lineBreakMode = NSLineBreakMode.ByTruncatingTail;
        subtitleView.autoresizingMask = UIViewAutoresizing.FlexibleWidth;
        
        navigationView.addSubview(titleView);
        navigationView.addSubview(subtitleView);
        
        self.navigationItem.titleView = navigationView;
        
        var longPressGesture = AALongPressGestureRecognizer(target: self, action: Selector("longPress:"))
        tableView.addGestureRecognizer(longPressGesture)
        
        var tapGesture = UITapGestureRecognizer(target: self, action: Selector("tap:"))
        tableView.addGestureRecognizer(tapGesture)
        
        // Avatar
        
        avatarView.frame = CGRectMake(0, 0, 36, 36)
        var avatarTapGesture = UITapGestureRecognizer(target: self, action: "onAvatarTap");
        avatarTapGesture.numberOfTapsRequired = 1
        avatarTapGesture.numberOfTouchesRequired = 1
        avatarView.addGestureRecognizer(avatarTapGesture)
        
        var barItem = UIBarButtonItem(customView: avatarView)
        self.navigationItem.rightBarButtonItem = barItem
        
        backgroundView.clipsToBounds = true
        backgroundView.backgroundColor = UIColor(
            patternImage:UIImage(named: "bg_foggy_birds")!.tintBgImage(MainAppTheme.bubbles.chatBgTint))
        view.insertSubview(backgroundView, atIndex: 0)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
//        MSG.buildConversationVMWithPeer(peer, withDisplayList: getDisplayList(), withCallback: )
        
        textView.text = MSG.loadDraftWithPeer(peer)
        
        // Installing bindings
        if (UInt(peer.getPeerType().ordinal()) == AMPeerType.PRIVATE.rawValue) {
            let user = MSG.getUserWithUid(peer.getPeerId())
            var nameModel = user.getNameModel();
            
            binder.bind(nameModel, closure: { (value: NSString?) -> () in
                self.titleView.text = String(value!);
                self.navigationView.sizeToFit();
            })
            binder.bind(user.getAvatarModel(), closure: { (value: AMAvatar?) -> () in
                self.avatarView.bind(user.getNameModel().get(), id: user.getId(), avatar: value)
            })
            
            binder.bind(MSG.getTypingWithUid(peer.getPeerId())!, valueModel2: user.getPresenceModel()!, closure:{ (typing:JavaLangBoolean?, presence:AMUserPresence?) -> () in
                
                if (typing != nil && typing!.booleanValue()) {
                    self.subtitleView.text = MSG.getFormatter().formatTyping();
                    self.subtitleView.textColor = Resources.PrimaryLightText
                } else {
                    var stateText = MSG.getFormatter().formatPresence(presence, withSex: user.getSex())
                    self.subtitleView.text = stateText;
                    var state = UInt(presence!.getState().ordinal())
                    if (state == AMUserPresence_State.ONLINE.rawValue) {
                        self.subtitleView.textColor = Resources.PrimaryLightText
                    } else {
                        self.subtitleView.textColor = Resources.SecondaryLightText
                    }
                }
            })
        } else if (UInt(peer.getPeerType().ordinal()) == AMPeerType.GROUP.rawValue) {
            let group = MSG.getGroupWithGid(peer.getPeerId())
            var nameModel = group.getNameModel()
            
            binder.bind(nameModel, closure: { (value: NSString?) -> () in
                self.titleView.text = String(value!);
                self.navigationView.sizeToFit();
            })
            binder.bind(group.getAvatarModel(), closure: { (value: AMAvatar?) -> () in
                self.avatarView.bind(group.getNameModel().get(), id: group.getId(), avatar: value)
            })
            binder.bind(MSG.getGroupTypingWithGid(group.getId())!, valueModel2: group.getMembersModel(), valueModel3: group.getPresenceModel(), closure: { (typingValue:IOSIntArray?, members:JavaUtilHashSet?, onlineCount:JavaLangInteger?) -> () in
                if (members!.size() == 0) {
                    self.subtitleView.textColor = Resources.SecondaryLightText
                    self.subtitleView.text = NSLocalizedString("ChatNoGroupAccess", comment: "You is not member")
                } else {
                    if (typingValue != nil && typingValue!.length() > 0) {
                        self.subtitleView.textColor = Resources.PrimaryLightText
                        if (typingValue!.length() == 1) {
                            var uid = typingValue!.intAtIndex(0);
                            var user = MSG.getUserWithUid(uid)
                            self.subtitleView.text = MSG.getFormatter().formatTypingWithName(user.getNameModel().get())
                        } else {
                            self.subtitleView.text = MSG.getFormatter().formatTypingWithCount(typingValue!.length());
                        }
                    } else {
                        var membersString = MSG.getFormatter().formatGroupMembers(members!.size())
                        if (onlineCount == nil || onlineCount!.integerValue == 0) {
                            self.subtitleView.textColor = Resources.SecondaryLightText
                            self.subtitleView.text = membersString;
                        } else {
                            membersString = membersString + ", ";
                            var onlineString = MSG.getFormatter().formatGroupOnline(onlineCount!.intValue());
                            var attributedString = NSMutableAttributedString(string: (membersString + onlineString))
                            attributedString.addAttribute(NSForegroundColorAttributeName, value: Resources.PrimaryLightText, range: NSMakeRange(membersString.size(), onlineString.size()))
                            self.subtitleView.attributedText = attributedString
                        }
                    }
                }
            })
        }
        
        MSG.onConversationOpenWithPeer(peer)
    }
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        backgroundView.frame = CGRect(x: 0, y: 0, width: view.bounds.width, height: view.bounds.height)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // unreadMessageId = MSG.loadLastReadState(peer)
        navigationItem.backBarButtonItem = UIBarButtonItem(title: NSLocalizedString("NavigationBack",comment: "Back button"), style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        MSG.onConversationOpenWithPeer(peer)
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        if count(navigationController!.viewControllers) > 2 {
            if let firstController = navigationController!.viewControllers[0] as? UIViewController,
                let currentController: AnyObject = navigationController!.viewControllers[count(navigationController!.viewControllers) - 1] as? AAConversationController {
                    navigationController!.setViewControllers([firstController, currentController], animated: false)
            }
        }
    }
    
    override func getAddAnimation(item: AnyObject?) -> UITableViewRowAnimation {
        var message = item as! AMMessage
        if (message.getSenderId() == MSG.myUid()) {
            return UITableViewRowAnimation.Right
        } else {
            return UITableViewRowAnimation.Left
        }
    }
    
    override func afterLoaded() {
        NSLog("afterLoaded")
        var sortState = MSG.loadLastReadState(peer)

        if (sortState == 0) {
            NSLog("lastReadMessage == 0")
            return
        }
        
        if (getCount() == 0) {
            NSLog("getCount() == 0")
            return
        }
        
        var index = -1
        unreadMessageId = 0
        for var i = getCount() - 1; i >= 0; --i {
            var item = objectAtIndex(i) as! AMMessage
            if (item.getSortDate() > sortState && item.getSenderId() != MSG.myUid()) {
                index = i
                unreadMessageId = item.getRid()
                break
            }
        }
        
        if (index < 0) {
            NSLog("Not found")
        } else {
            NSLog("Founded @\(index)")
            self.tableView.reloadData()
            self.tableView.scrollToRowAtIndexPath(NSIndexPath(forRow: Int(index), inSection: 0), atScrollPosition: UITableViewScrollPosition.Middle, animated: false)
        }
    }
    
    override func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated);
        MSG.saveDraftWithPeer(peer, withDraft: textView.text);
    }
    
    // MARK: -
    // MARK: Methods
    
    func longPress(gesture: AALongPressGestureRecognizer) {
        if gesture.state == UIGestureRecognizerState.Began {
            let point = gesture.locationInView(tableView)
            let indexPath = tableView.indexPathForRowAtPoint(point)
            if indexPath != nil {
                if let cell = tableView.cellForRowAtIndexPath(indexPath!) as? AABubbleCell {
                    if cell.bubble.superview != nil {
                        var bubbleFrame = cell.bubble.frame
                        bubbleFrame = tableView.convertRect(bubbleFrame, fromView: cell.bubble.superview)
                        if CGRectContainsPoint(bubbleFrame, point) {
                            cell.becomeFirstResponder()
                            var menuController = UIMenuController.sharedMenuController()
                            menuController.setTargetRect(bubbleFrame, inView:tableView)
                            menuController.setMenuVisible(true, animated: true)
                        }
                    }
                }
            }
        }
    }
    
    func tap(gesture: UITapGestureRecognizer) {
        if gesture.state == UIGestureRecognizerState.Ended {
            let point = gesture.locationInView(tableView)
            let indexPath = tableView.indexPathForRowAtPoint(point)
            if indexPath != nil {
                if let cell = tableView.cellForRowAtIndexPath(indexPath!) as? AABubbleCell {
                    if cell.avatarView.superview != nil {
                        var avatarFrame = cell.avatarView.frame
                        avatarFrame = tableView.convertRect(avatarFrame, fromView: cell.bubble.superview)
                        if CGRectContainsPoint(avatarFrame, point) {
                            var item = objectAtIndexPath(indexPath!) as! AMMessage;
                            navigateToUserWithId(Int(item.getSenderId()))
                            return
                        }
                    }
                    
                    var item = objectAtIndexPath(indexPath!) as! AMMessage
                    if let content = item.getContent() as? AMPhotoContent {
                        if let photoCell = cell as? AABubbleMediaCell {
                            var frame = photoCell.preview.frame
                            var touchFrame = tableView.convertRect(frame, fromView: cell.bubble.superview)
                            if CGRectContainsPoint(touchFrame, point) {
                                if let fileSource = content.getSource() as? AMFileRemoteSource {
                                    MSG.requestStateWithFileId(fileSource.getFileReference().getFileId(), withCallback: CocoaDownloadCallback(
                                    notDownloaded: { () -> () in
                                        MSG.startDownloadingWithReference(fileSource.getFileReference())
                                    }, onDownloading: { (progress) -> () in
                                        MSG.cancelDownloadingWithFileId(fileSource.getFileReference().getFileId())
                                    }, onDownloaded: { (reference) -> () in
                                        var imageInfo = JTSImageInfo()
                                        imageInfo.image = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(reference))
                                        imageInfo.referenceRect = frame
                                        imageInfo.referenceView = photoCell
                                        
                                        var previewController = JTSImageViewController(imageInfo: imageInfo, mode: JTSImageViewControllerMode.Image, backgroundStyle: JTSImageViewControllerBackgroundOptions.Blurred)
                                        previewController.showFromViewController(self, transition: JTSImageViewControllerTransition._FromOriginalPosition)
                                    }))
                                } else if let fileSource = content.getSource() as? AMFileLocalSource {
                                    MSG.requestUploadStateWithRid(item.getRid(), withCallback: CocoaUploadCallback(
                                    notUploaded: { () -> () in
                                        MSG.resumeUploadWithRid(item.getRid())
                                    }, onUploading: { (progress) -> () in
                                        MSG.pauseUploadWithRid(item.getRid())
                                    }, onUploadedClosure: { () -> () in
                                        var imageInfo = JTSImageInfo()
                                        imageInfo.image = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor()))
                                        imageInfo.referenceRect = frame
                                        imageInfo.referenceView = photoCell
                                        
                                        var previewController = JTSImageViewController(imageInfo: imageInfo, mode: JTSImageViewControllerMode.Image, backgroundStyle: JTSImageViewControllerBackgroundOptions.Blurred)
                                        previewController.showFromViewController(self, transition: JTSImageViewControllerTransition._FromOriginalPosition)
                                    }))
                                }
                            }
                        }
                    } else if let content = item.getContent() as? AMDocumentContent {
                        if let documentCell = cell as? AABubbleDocumentCell {
                            var frame = documentCell.bubble.frame
                            frame = tableView.convertRect(frame, fromView: cell.bubble.superview)
                            if CGRectContainsPoint(frame, point) {
                                if let fileSource = content.getSource() as? AMFileRemoteSource {
                                    MSG.requestStateWithFileId(fileSource.getFileReference().getFileId(), withCallback: CocoaDownloadCallback(
                                    notDownloaded: { () -> () in
                                        MSG.startDownloadingWithReference(fileSource.getFileReference())
                                    }, onDownloading: { (progress) -> () in
                                        MSG.cancelDownloadingWithFileId(fileSource.getFileReference().getFileId())
                                    }, onDownloaded: { (reference) -> () in
                                        var controller = UIDocumentInteractionController(URL: NSURL(fileURLWithPath: CocoaFiles.pathFromDescriptor(reference))!)
                                        controller.delegate = self
                                        controller.presentPreviewAnimated(true)
                                    }))
                                } else if let fileSource = content.getSource() as? AMFileLocalSource {
                                    MSG.requestUploadStateWithRid(item.getRid(), withCallback: CocoaUploadCallback(
                                    notUploaded: { () -> () in
                                        MSG.resumeUploadWithRid(item.getRid())
                                    }, onUploading: { (progress) -> () in
                                        MSG.pauseUploadWithRid(item.getRid())
                                    }, onUploadedClosure: { () -> () in
                                        var controller = UIDocumentInteractionController(URL: NSURL(fileURLWithPath: CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor()))!)
                                        controller.delegate = self
                                        controller.presentPreviewAnimated(true)
                                    }))
                                }
                            }
                        }
                    } else if let content = item.getContent() as? AMBannerContent {
                        if let bannerCell = cell as? AABubbleAdCell {
                            var frame = bannerCell.contentView.frame
                            frame = tableView.convertRect(frame, fromView: cell.contentView.superview)
                            if CGRectContainsPoint(frame, point) {
                                UIApplication.sharedApplication().openURL(NSURL(string: content.getAdUrl())!)
                            }
                        }
                    }
                }
            }
        }
    }
    
    func onAvatarTap() {
        let id = Int(peer.getPeerId())
        if (UInt(peer.getPeerType().ordinal()) == AMPeerType.PRIVATE.rawValue) {
            navigateToUserProfileWithId(id)
        } else if (UInt(peer.getPeerType().ordinal()) == AMPeerType.GROUP.rawValue) {
            let groupInfoController = AAConversationGroupInfoController(gid: id)
            groupInfoController.hidesBottomBarWhenPushed = true
            self.navigationController?.pushViewController(groupInfoController, animated: true)
        }
    }
    
    override func textWillUpdate() {
        super.textWillUpdate();

        MSG.onTypingWithPeer(peer);
    }
    
    override func didPressRightButton(sender: AnyObject!) {
        
        // Perform auto correct
        textView.refreshFirstResponder();
        
        MSG.trackTextSendWithPeer(peer)
        MSG.sendMessageWithPeer(peer, withText: textView.text, withMentions: JavaUtilArrayList())
        
        super.didPressRightButton(sender);
    }
    
    override func didPressLeftButton(sender: AnyObject!) {
        super.didPressLeftButton(sender)
        
        var actionShit = ABActionShit()
        actionShit.buttonTitles = [
            NSLocalizedString("PhotoCamera",comment: "Take Photo"),
            NSLocalizedString("PhotoLibrary",comment: "Choose Photo"),
            NSLocalizedString("SendDocument",comment: "Document")]
        actionShit.cancelButtonTitle = NSLocalizedString("AlertCancel",comment: "Cancel")
        actionShit.delegate = self
        actionShit.showWithCompletion(nil)
    }
    
    // MARK: -
    // MARK: UITableView
    
    override func buildCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?) -> UITableViewCell {
        
        var message = (item as! AMMessage);
        
        // TODO: Add Docs and Video
        if (message.getContent() is AMTextContent){
            var cell = tableView.dequeueReusableCellWithIdentifier(BubbleTextIdentifier) as! AABubbleTextCell?
            if (cell == nil) {
                cell = AABubbleTextCell(reuseId: BubbleTextIdentifier, peer: peer)
            }
            return cell!
        } else if (message.getContent() is AMPhotoContent) {
            var cell = tableView.dequeueReusableCellWithIdentifier(BubbleMediaIdentifier) as! AABubbleMediaCell?
            if (cell == nil) {
                cell = AABubbleMediaCell(reuseId: BubbleMediaIdentifier, peer: peer)
            }
            return cell!
        } else if (message.getContent() is AMDocumentContent) {
            var cell = tableView.dequeueReusableCellWithIdentifier(BubbleDocumentIdentifier) as! AABubbleDocumentCell?
            if (cell == nil) {
                cell = AABubbleDocumentCell(reuseId: BubbleDocumentIdentifier, peer: peer)
            }
            return cell!
        } else if (message.getContent() is AMServiceContent){
            var cell = tableView.dequeueReusableCellWithIdentifier(BubbleServiceIdentifier) as! AABubbleServiceCell?
            if (cell == nil) {
                cell = AABubbleServiceCell(reuseId: BubbleServiceIdentifier, peer: peer)
            }
            return cell!
        } else if (message.getContent() is AMBannerContent) {
            var cell = tableView.dequeueReusableCellWithIdentifier(BubbleBannerIdentifier) as! AABubbleAdCell?
            if (cell == nil) {
                cell = AABubbleAdCell(reuseId: BubbleServiceIdentifier, peer: peer)
            }
            return cell!
        } else {
            // Use Text bubble for unsupported
            var cell = tableView.dequeueReusableCellWithIdentifier(BubbleTextIdentifier) as! AABubbleTextCell?
            if (cell == nil) {
                cell = AABubbleTextCell(reuseId: BubbleTextIdentifier, peer: peer)
            }
            return cell!
        }
    }
    
    override func bindCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?, cell: UITableViewCell) {
        var message = item as! AMMessage
        var bubbleCell = (cell as! AABubbleCell)
        
        var preferCompact = false
        var isShowDate = true
        if (indexPath.row > 0) {
            var next =  objectAtIndex(indexPath.row - 1) as! AMMessage
            preferCompact = useCompact(message, next: next)
        }
        if (indexPath.row + 1 < getCount()) {
            var prev =  objectAtIndex(indexPath.row + 1) as! AMMessage
            isShowDate = showDate(message, prev: prev)
        }
        if (isShowDate) {
            isShowDate = true
            preferCompact = false
        }

        bubbleCell.performBind(message, isPreferCompact: preferCompact, isShowDate: isShowDate, isShowNewMessages:(unreadMessageId == message.getRid()))
    }
    
    func useCompact(source: AMMessage, next: AMMessage) -> Bool {
        if (source.getContent() is AMServiceContent) {
            if (next.getContent() is AMServiceContent) {
                return true
            }
        } else {
            if (next.getContent() is AMServiceContent) {
                return false
            }
            if (source.getSenderId() == next.getSenderId()) {
                return true
            }
        }
        
        return false
    }
    
    func showDate(source:AMMessage, prev: AMMessage) -> Bool {
        var currentDate = source.getDate() / (1000 * 60 * 60 * 24)
        var nextDate = prev.getDate() / (1000 * 60 * 60 * 24)
        return currentDate != nextDate
    }
    
    override func buildDisplayList() -> AMBindedDisplayList {
        return MSG.getMessagesGlobalListWithPeer(peer)
    }
    
    // MARK: -
    // MARK: UITableView Delegate
    
    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        var message = objectAtIndexPath(indexPath) as! AMMessage;
        
        var preferCompact = false
        var isShowDate = true
        if (indexPath.row > 0) {
            var next =  objectAtIndex(indexPath.row - 1) as! AMMessage
            preferCompact = useCompact(message, next: next)
        }
        if (indexPath.row + 1 < getCount()) {
            var prev =  objectAtIndex(indexPath.row + 1) as! AMMessage
            isShowDate = showDate(message, prev: prev)
        }
        if (isShowDate) {
            isShowDate = true
            preferCompact = false
        }
        
        let group = peer.getPeerType().ordinal() == jint(AMPeerType.GROUP.rawValue)
        return AABubbleCell.measureHeight(message, group: group, isPreferCompact: preferCompact, isShowDate: isShowDate, isShowNewMessages:(unreadMessageId == message.getRid()));
    }
    
    // MARK: -
    // MARK: Navigation
    
    private func navigateToUserWithId(id: Int) {
        navigateNext(AAConversationController(peer: AMPeer.userWithInt(jint(id))), removeCurrent: false)
    }

    private func navigateToUserProfileWithId(id: Int) {
        navigateNext(AAUserInfoController(uid: id), removeCurrent: false)
    }
}

extension AAConversationController: UIDocumentInteractionControllerDelegate {
    func documentInteractionControllerViewControllerForPreview(controller: UIDocumentInteractionController) -> UIViewController {
        return self
    }
}

// MARK: -
// MARK: UIDocumentPicker Delegate

extension AAConversationController: UIDocumentPickerDelegate {
    
    func documentPicker(controller: UIDocumentPickerViewController, didPickDocumentAtURL url: NSURL) {
        var path = url.path!;
        var fileName = url.lastPathComponent
        var range = path.rangeOfString("/tmp", options: NSStringCompareOptions.allZeros, range: nil, locale: nil)
        var descriptor = path.substringFromIndex(range!.startIndex)
        NSLog("Picked file: \(descriptor)")
        MSG.trackDocumentSendWithPeer(peer)
        MSG.sendDocumentWithPeer(peer, withName: fileName, withMime: "application/octet-stream", withDescriptor: descriptor)
    }
    
}

// MARK: -
// MARK: UIImagePickerController Delegate

extension AAConversationController: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage!, editingInfo: [NSObject : AnyObject]!) {
        MainAppTheme.navigation.applyStatusBar()
        picker.dismissViewControllerAnimated(true, completion: nil)
        MSG.trackPhotoSendWithPeer(peer!)
        MSG.sendUIImage(image, peer: peer!)
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [NSObject : AnyObject]) {
        MainAppTheme.navigation.applyStatusBar()
        picker.dismissViewControllerAnimated(true, completion: nil)
        
        MSG.sendUIImage(info[UIImagePickerControllerOriginalImage] as! UIImage, peer: peer!)
    }
    
    func imagePickerControllerDidCancel(picker: UIImagePickerController) {
        MainAppTheme.navigation.applyStatusBar()
        picker.dismissViewControllerAnimated(true, completion: nil)
    }
    
}

extension AAConversationController: UIDocumentMenuDelegate {
    func documentMenu(documentMenu: UIDocumentMenuViewController, didPickDocumentPicker documentPicker: UIDocumentPickerViewController) {
        documentPicker.delegate = self
        self.presentViewController(documentPicker, animated: true, completion: nil)
    }
}

//extension AAConversationController: UIDocumentPickerDelegate {
//    
//}

// MARK: -
// MARK: ABActionShit Delegate

extension AAConversationController: ABActionShitDelegate {
    func actionShit(actionShit: ABActionShit!, clickedButtonAtIndex buttonIndex: Int) {
        if (buttonIndex == 0 || buttonIndex == 1) {
            var pickerController = AAImagePickerController()
            pickerController.sourceType = (buttonIndex == 0 ? UIImagePickerControllerSourceType.Camera : UIImagePickerControllerSourceType.PhotoLibrary)
            pickerController.mediaTypes = [kUTTypeImage]
            pickerController.view.backgroundColor = MainAppTheme.list.bgColor
            pickerController.navigationBar.tintColor = MainAppTheme.navigation.barColor
            pickerController.delegate = self
            pickerController.navigationBar.tintColor = MainAppTheme.navigation.titleColor
            pickerController.navigationBar.titleTextAttributes = [NSForegroundColorAttributeName: MainAppTheme.navigation.titleColor]
            self.presentViewController(pickerController, animated: true, completion: nil)
        } else if (buttonIndex == 2) {
            var documentPicker = UIDocumentMenuViewController(documentTypes: UTTAll, inMode: UIDocumentPickerMode.Import)
            documentPicker.view.backgroundColor = UIColor.clearColor()
            documentPicker.delegate = self
            self.presentViewController(documentPicker, animated: true, completion: nil)
        }
    }
}

// MARK: -
// MARK: BarAvatarView

class BarAvatarView : AAAvatarView {
    
    override init(frameSize: Int, type: AAAvatarType) {
        super.init(frameSize: frameSize, type: type)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func alignmentRectInsets() -> UIEdgeInsets {
        return UIEdgeInsets(top: 0, left: 36, bottom: 0, right: 8)
    }
}