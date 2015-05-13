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
    
    private let titleView: UILabel = UILabel();
    private let subtitleView: UILabel = UILabel();
    private let navigationView: UIView = UIView();
    
    private let avatarView = BarAvatarView(frameSize: 36, type: AAAvatarType.Rounded)
    
    private let backgroundView: UIView = UIView()
    
    // MARK: -
    // MARK: Public vars
    
    var peer: AMPeer!;
    let binder: Binder = Binder();
    
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
        
        textView.text = MSG.loadDraft(peer)
        
        // Installing bindings
        if (UInt(peer.getPeerType().ordinal()) == AMPeerType.PRIVATE.rawValue) {
            let user = MSG.getUsers().getWithLong(jlong(peer.getPeerId())) as! AMUserVM;
            var nameModel = user.getName() as AMValueModel;
            
            binder.bind(nameModel, closure: { (value: NSString?) -> () in
                self.titleView.text = String(value!);
                self.navigationView.sizeToFit();
            })
            binder.bind(user.getAvatar(), closure: { (value: AMAvatar?) -> () in
                self.avatarView.bind(user.getName().get() as! String, id: user.getId(), avatar: value)
            })
            
            binder.bind(MSG.getTypingWithInt(peer.getPeerId())!, valueModel2: user.getPresence()!, closure:{ (typing:JavaLangBoolean?, presence:AMUserPresence?) -> () in
                
                if (typing != nil && typing!.booleanValue()) {
                    self.subtitleView.text = MSG.getFormatter().formatTyping();
                    self.subtitleView.textColor = Resources.PrimaryLightText
                } else {
                    var stateText = MSG.getFormatter().formatPresenceWithAMUserPresence(presence, withAMSexEnum: user.getSex())
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
            let group = MSG.getGroups().getWithLong(jlong(peer.getPeerId())) as! AMGroupVM;
            var nameModel = group.getName() as AMValueModel;
            
            binder.bind(nameModel, closure: { (value: NSString?) -> () in
                self.titleView.text = String(value!);
                self.navigationView.sizeToFit();
            })
            binder.bind(group.getAvatar(), closure: { (value: AMAvatar?) -> () in
                self.avatarView.bind(group.getName().get() as! String, id: group.getId(), avatar: value)
            })
            binder.bind(MSG.getGroupTypingWithInt(group.getId())!, valueModel2: group.getMembers(), valueModel3: group.getPresence(), closure: { (typingValue:IOSIntArray?, members:JavaUtilHashSet?, onlineCount:JavaLangInteger?) -> () in
                if (members!.size() == 0) {
                    self.subtitleView.textColor = Resources.SecondaryLightText
                    self.subtitleView.text = NSLocalizedString("ChatNoGroupAccess", comment: "You is not member")
                } else {
                    if (typingValue!.length() > 0) {
                        self.subtitleView.textColor = Resources.PrimaryLightText
                        if (typingValue!.length() == 1) {
                            var uid = typingValue!.intAtIndex(0);
                            var user = MSG.getUsers().getWithLong(jlong(uid)) as! AMUserVM;
                            self.subtitleView.text = MSG.getFormatter().formatTypingWithNSString(user.getName().get() as!String)
                        } else {
                            self.subtitleView.text = MSG.getFormatter().formatTypingWithInt(typingValue!.length());
                        }
                    } else {
                        var membersString = MSG.getFormatter().formatGroupMembersWithInt(members!.size())
                        if (onlineCount == nil || onlineCount!.integerValue == 0) {
                            self.subtitleView.textColor = Resources.SecondaryLightText
                            self.subtitleView.text = membersString;
                        } else {
                            membersString = membersString + ", ";
                            var onlineString = MSG.getFormatter().formatGroupOnlineWithInt(onlineCount!.intValue());
                            var attributedString = NSMutableAttributedString(string: (membersString + onlineString))
                            attributedString.addAttribute(NSForegroundColorAttributeName, value: Resources.PrimaryLightText, range: NSMakeRange(membersString.size(), onlineString.size()))
                            self.subtitleView.attributedText = attributedString
                        }
                    }
                }
            })
        }
        
        MSG.onConversationOpen(peer)
    }
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        backgroundView.frame = CGRect(x: 0, y: 0, width: view.bounds.width, height: view.bounds.height)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        navigationItem.backBarButtonItem = UIBarButtonItem(title: NSLocalizedString("NavigationBack",comment: "Back button"), style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        MSG.onConversationClosed(peer)
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
    
    override func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated);
        MSG.saveDraft(peer, withText: textView.text);
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
                        if let fileSource = content.getSource() as? AMFileRemoteSource {
                            if let photoCell = cell as? AABubbleMediaCell {
                                let frame = photoCell.preview.frame
                                
                                MSG.requestStateWithLong(fileSource.getFileReference().getFileId(),
                                    withAMFileCallback: CocoaDownloadCallback(
                                        notDownloaded: { () -> () in
                                        MSG.startDownloadingWithAMFileReference(fileSource.getFileReference())
                                    }, onDownloading: { (progress) -> () in
                                        MSG.cancelDownloadingWithLong(fileSource.getFileReference().getFileId())
                                    }, onDownloaded: { (reference) -> () in
                                        var imageInfo = JTSImageInfo()
                                        imageInfo.image = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(reference))
                                        imageInfo.referenceRect = frame
                                        imageInfo.referenceView = photoCell
                                        
                                        var previewController = JTSImageViewController(imageInfo: imageInfo, mode: JTSImageViewControllerMode.Image, backgroundStyle: JTSImageViewControllerBackgroundOptions.Blurred)
                                        previewController.showFromViewController(self, transition: JTSImageViewControllerTransition._FromOriginalPosition)
                                    }))
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
        MSG.onTyping(peer);
    }
    
    override func didPressRightButton(sender: AnyObject!) {
        
        // Perform auto correct
        textView.refreshFirstResponder();
        
        MSG.trackTextSendWithAMPeer(peer)
        MSG.sendMessage(peer, withText: textView.text);
        
        super.didPressRightButton(sender);
    }
    
    override func didPressLeftButton(sender: AnyObject!) {
        super.didPressLeftButton(sender)
        
        var actionShit = ABActionShit()
        actionShit.buttonTitles = [NSLocalizedString("PhotoCamera",comment: "Take Photo"), NSLocalizedString("PhotoLibrary",comment: "Choose Photo")]
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
            
        } else if (message.getContent() is AMServiceContent){
            var cell = tableView.dequeueReusableCellWithIdentifier(BubbleServiceIdentifier) as! AABubbleServiceCell?
            if (cell == nil) {
                cell = AABubbleServiceCell(reuseId: BubbleServiceIdentifier, peer: peer)
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
        if (indexPath.row + 1 < getDisplayList().getSize()) {
            var prev =  objectAtIndex(indexPath.row + 1) as! AMMessage
            isShowDate = showDate(message, prev: prev)
        }
        if (isShowDate) {
            isShowDate = true
            preferCompact = false
        }

        bubbleCell.performBind(message, isPreferCompact: preferCompact, isShowDate: isShowDate)
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
    
    override func getDisplayList() -> AMBindedDisplayList {
        return MSG.getMessagesGlobalListWithAMPeer(peer)
    }
    
    // MARK: -
    // MARK: UITableView Delegate
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        var message = objectAtIndexPath(indexPath) as! AMMessage;
        
        if let document = message.getContent() as? AMDocumentContent {
        
        }
    }
    
    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        var message = objectAtIndexPath(indexPath) as! AMMessage;
        
        var preferCompact = false
        var isShowDate = true
        if (indexPath.row > 0) {
            var next =  objectAtIndex(indexPath.row - 1) as! AMMessage
            preferCompact = useCompact(message, next: next)
        }
        if (indexPath.row + 1 < getDisplayList().getSize()) {
            var prev =  objectAtIndex(indexPath.row + 1) as! AMMessage
            isShowDate = showDate(message, prev: prev)
        }
        if (isShowDate) {
            isShowDate = true
            preferCompact = false
        }
        
        let group = peer.getPeerType().ordinal() == jint(AMPeerType.GROUP.rawValue)
        return AABubbleCell.measureHeight(message, group: group, isPreferCompact: preferCompact, isShowDate: isShowDate);
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

// MARK: -
// MARK: UIDocumentPicker Delegate

extension AAConversationController: UIDocumentPickerDelegate {
    
    func documentPicker(controller: UIDocumentPickerViewController, didPickDocumentAtURL url: NSURL) {
        var path = url.path;
        
        // TODO: Implement
    }
    
}

// MARK: -
// MARK: UIImagePickerController Delegate

extension AAConversationController: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage!, editingInfo: [NSObject : AnyObject]!) {
        MainAppTheme.navigation.applyStatusBar()
        picker.dismissViewControllerAnimated(true, completion: nil)
        MSG.trackPhotoSendWithAMPeer(peer!)
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