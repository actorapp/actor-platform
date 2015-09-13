//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit
import MobileCoreServices

class ConversationViewController: ConversationBaseViewController {
    
    // MARK: -
    // MARK: Private vars
    
    private let BubbleTextIdentifier = "BubbleTextIdentifier"
    private let BubbleMediaIdentifier = "BubbleMediaIdentifier"
    private let BubbleDocumentIdentifier = "BubbleDocumentIdentifier"
    private let BubbleServiceIdentifier = "BubbleServiceIdentifier"
    private let BubbleBannerIdentifier = "BubbleBannerIdentifier"
    
    private let binder: Binder = Binder();
    
    private let titleView: UILabel = UILabel()
    private let subtitleView: UILabel = UILabel()
    private let navigationView: UIView = UIView()
    private let avatarView = BarAvatarView(frameSize: 36, type: .Rounded)
    private let backgroundView: UIView = UIView()
    
    override init(peer: ACPeer) {
        super.init(peer: peer);
        
        // Messages
        
        backgroundView.clipsToBounds = true
        backgroundView.backgroundColor = UIColor(
            patternImage:UIImage(named: "bg_foggy_birds")!.tintBgImage(MainAppTheme.bubbles.chatBgTint))
        view.insertSubview(backgroundView, atIndex: 0)

        // Text Input
        
        self.textInputbar.backgroundColor = MainAppTheme.chat.chatField
        self.textInputbar.autoHideRightButton = false;
        self.textView.placeholder = NSLocalizedString("ChatPlaceholder",comment: "Placeholder")
        self.rightButton.setTitle(NSLocalizedString("ChatSend", comment: "Send"), forState: UIControlState.Normal)
        self.rightButton.setTitleColor(MainAppTheme.chat.sendEnabled, forState: UIControlState.Normal)
        self.rightButton.setTitleColor(MainAppTheme.chat.sendDisabled, forState: UIControlState.Disabled)
        
        self.keyboardPanningEnabled = true
        
        self.registerPrefixesForAutoCompletion(["@"])
        
        self.textView.keyboardAppearance = MainAppTheme.common.isDarkKeyboard ? UIKeyboardAppearance.Dark : UIKeyboardAppearance.Light

        self.leftButton.setImage(UIImage(named: "conv_attach")!
            .tintImage(MainAppTheme.chat.attachColor)
            .imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal),
            forState: UIControlState.Normal)
        
        
        // Navigation Title
        
        navigationView.frame = CGRectMake(0, 0, 200, 44);
        navigationView.autoresizingMask = UIViewAutoresizing.FlexibleWidth;
        
        titleView.font = UIFont(name: "HelveticaNeue-Medium", size: 17)!
        titleView.adjustsFontSizeToFitWidth = false;
        titleView.textColor = Resources.PrimaryLightText
        titleView.textAlignment = NSTextAlignment.Center;
        titleView.lineBreakMode = NSLineBreakMode.ByTruncatingTail;
        titleView.autoresizingMask = UIViewAutoresizing.FlexibleWidth;
        
        subtitleView.font = UIFont.systemFontOfSize(13);
        subtitleView.adjustsFontSizeToFitWidth = true;
        subtitleView.textColor = Resources.SecondaryLightText
        subtitleView.textAlignment = NSTextAlignment.Center;
        subtitleView.lineBreakMode = NSLineBreakMode.ByTruncatingTail;
        subtitleView.autoresizingMask = UIViewAutoresizing.FlexibleWidth;
        
        navigationView.addSubview(titleView)
        navigationView.addSubview(subtitleView)
        
        self.navigationItem.titleView = navigationView;
        
        // Navigation Avatar
        
        avatarView.frame = CGRectMake(0, 0, 36, 36)
        let avatarTapGesture = UITapGestureRecognizer(target: self, action: "onAvatarTap");
        avatarTapGesture.numberOfTapsRequired = 1
        avatarTapGesture.numberOfTouchesRequired = 1
        avatarView.addGestureRecognizer(avatarTapGesture)
        
        let barItem = UIBarButtonItem(customView: avatarView)
        self.navigationItem.rightBarButtonItem = barItem
    }
    
    required init(coder aDecoder: NSCoder!) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        textView.text = Actor.loadDraftWithPeer(peer)
        
        // Installing bindings
        if (UInt(peer.getPeerType().ordinal()) == ACPeerType.PRIVATE.rawValue) {
            let user = Actor.getUserWithUid(peer.getPeerId())
            let nameModel = user.getNameModel();
            
            binder.bind(nameModel, closure: { (value: NSString?) -> () in
                self.titleView.text = String(value!);
                self.navigationView.sizeToFit();
            })
            binder.bind(user.getAvatarModel(), closure: { (value: ACAvatar?) -> () in
                self.avatarView.bind(user.getNameModel().get(), id: user.getId(), avatar: value)
            })
            
            binder.bind(Actor.getTypingWithUid(peer.getPeerId())!, valueModel2: user.getPresenceModel()!, closure:{ (typing:JavaLangBoolean?, presence:ACUserPresence?) -> () in
                
                if (typing != nil && typing!.booleanValue()) {
                    self.subtitleView.text = Actor.getFormatter().formatTyping();
                    self.subtitleView.textColor = Resources.PrimaryLightText
                } else {
                    let stateText = Actor.getFormatter().formatPresence(presence, withSex: user.getSex())
                    self.subtitleView.text = stateText;
                    let state = UInt(presence!.getState().ordinal())
                    if (state == ACUserPresence_State.ONLINE.rawValue) {
                        self.subtitleView.textColor = Resources.PrimaryLightText
                    } else {
                        self.subtitleView.textColor = Resources.SecondaryLightText
                    }
                }
            })
        } else if (UInt(peer.getPeerType().ordinal()) == ACPeerType.GROUP.rawValue) {
            let group = Actor.getGroupWithGid(peer.getPeerId())
            let nameModel = group.getNameModel()
            
            binder.bind(nameModel, closure: { (value: NSString?) -> () in
                self.titleView.text = String(value!);
                self.navigationView.sizeToFit();
            })
            binder.bind(group.getAvatarModel(), closure: { (value: ACAvatar?) -> () in
                self.avatarView.bind(group.getNameModel().get(), id: group.getId(), avatar: value)
            })
            binder.bind(Actor.getGroupTypingWithGid(group.getId())!, valueModel2: group.getMembersModel(), valueModel3: group.getPresenceModel(), closure: { (typingValue:IOSIntArray?, members:JavaUtilHashSet?, onlineCount:JavaLangInteger?) -> () in
//                if (!group.isMemberModel().get().booleanValue()) {
//                    self.subtitleView.text = NSLocalizedString("ChatNoGroupAccess", comment: "You is not member")
//                    self.textInputbar.hidden = true
//                    return
//                } else {
//                    self.textInputbar.hidden = false
//                }
            
                if (typingValue != nil && typingValue!.length() > 0) {
                    self.subtitleView.textColor = Resources.PrimaryLightText
                    if (typingValue!.length() == 1) {
                        let uid = typingValue!.intAtIndex(0);
                        let user = Actor.getUserWithUid(uid)
                        self.subtitleView.text = Actor.getFormatter().formatTypingWithName(user.getNameModel().get())
                    } else {
                        self.subtitleView.text = Actor.getFormatter().formatTypingWithCount(typingValue!.length());
                    }
                } else {
                    var membersString = Actor.getFormatter().formatGroupMembers(members!.size())
                    if (onlineCount == nil || onlineCount!.integerValue == 0) {
                        self.subtitleView.textColor = Resources.SecondaryLightText
                        self.subtitleView.text = membersString;
                    } else {
                        membersString = membersString + ", ";
                        let onlineString = Actor.getFormatter().formatGroupOnline(onlineCount!.intValue());
                        let attributedString = NSMutableAttributedString(string: (membersString + onlineString))
                        attributedString.addAttribute(NSForegroundColorAttributeName, value: Resources.PrimaryLightText, range: NSMakeRange(membersString.length, onlineString.length))
                        self.subtitleView.attributedText = attributedString
                    }
                }
            })
        }
        
        Actor.onConversationOpenWithPeer(peer)
    }
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        backgroundView.frame = CGRect(x: 0, y: 0, width: view.bounds.width, height: view.bounds.height)
        
        titleView.frame = CGRectMake(0, 4, (navigationView.frame.width - 0), 20)
        subtitleView.frame = CGRectMake(0, 22, (navigationView.frame.width - 0), 20)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: nil, style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        Actor.onConversationClosedWithPeer(peer)
        
        (UIApplication.sharedApplication().delegate as! AppDelegate).hideBadge()
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        (UIApplication.sharedApplication().delegate as! AppDelegate).showBadge()
        
        if navigationController!.viewControllers.count > 2 {
            let firstController = navigationController!.viewControllers[0]
            let currentController = navigationController!.viewControllers[navigationController!.viewControllers.count - 1]
            navigationController!.setViewControllers([firstController, currentController], animated: false)
        }
    }
    
//    override func afterLoaded() {
//        NSLog("afterLoaded")
//        var sortState = MSG.loadLastReadState(peer)
//
//        if (sortState == 0) {
//            NSLog("lastReadMessage == 0")
//            return
//        }
//        
//        if (getCount() == 0) {
//            NSLog("getCount() == 0")
//            return
//        }
//        
//        var index = -1
//        unreadMessageId = 0
//        for var i = getCount() - 1; i >= 0; --i {
//            var item = objectAtIndex(i) as! AMMessage
//            if (item.getSortDate() > sortState && item.getSenderId() != MSG.myUid()) {
//                index = i
//                unreadMessageId = item.getRid()
//                break
//            }
//        }
//        
//        if (index < 0) {
//            NSLog("Not found")
//        } else {
//            NSLog("Founded @\(index)")
//            // self.tableView.reloadData()
//            // self.tableView.scrollToRowAtIndexPath(NSIndexPath(forRow: Int(index), inSection: 0), atScrollPosition: UITableViewScrollPosition.Middle, animated: false)
//        }
//    }
    
    override func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated);
        Actor.saveDraftWithPeer(peer, withDraft: textView.text);
    }
    
    // MARK: -
    // MARK: Methods
    
    func longPress(gesture: UILongPressGestureRecognizer) {
        if gesture.state == UIGestureRecognizerState.Began {
            let point = gesture.locationInView(self.collectionView)
            let indexPath = self.collectionView.indexPathForItemAtPoint(point)
            if indexPath != nil {
                if let cell = collectionView.cellForItemAtIndexPath(indexPath!) as? AABubbleCell {
                    if cell.bubble.superview != nil {
                        var bubbleFrame = cell.bubble.frame
                        bubbleFrame = collectionView.convertRect(bubbleFrame, fromView: cell.bubble.superview)
                        if CGRectContainsPoint(bubbleFrame, point) {
                            // cell.becomeFirstResponder()
                            let menuController = UIMenuController.sharedMenuController()
                            menuController.setTargetRect(bubbleFrame, inView:collectionView)
                            menuController.menuItems = [UIMenuItem(title: "Copy", action: "copy")]
                            menuController.setMenuVisible(true, animated: true)
                        }
                    }
                }
            }
        }
    }
    
    func onAvatarTap() {
        let id = Int(peer.getPeerId())
        var controller: AAViewController
        if (UInt(peer.getPeerType().ordinal()) == ACPeerType.PRIVATE.rawValue) {
            controller = UserViewController(uid: id)
        } else if (UInt(peer.getPeerType().ordinal()) == ACPeerType.GROUP.rawValue) {
            controller = GroupViewController(gid: id)
        } else {
            return
        }
        
        if (isIPad) {
            let navigation = AANavigationController()
            navigation.viewControllers = [controller]
            let popover = UIPopoverController(contentViewController:  navigation)
            controller.popover = popover
            popover.presentPopoverFromBarButtonItem(navigationItem.rightBarButtonItem!,
                permittedArrowDirections: UIPopoverArrowDirection.Up,
                animated: true)
        } else {
            navigateNext(controller, removeCurrent: false)
        }
    }
    
    override func textWillUpdate() {
        super.textWillUpdate();

        Actor.onTypingWithPeer(peer);
    }
    
    override func didPressRightButton(sender: AnyObject!) {
        Actor.trackTextSendWithPeer(peer)
        Actor.sendMessageWithMentionsDetect(peer, withText: textView.text)
        super.didPressRightButton(sender)
    }
    
    override func didPressLeftButton(sender: AnyObject!) {
        super.didPressLeftButton(sender)
        
        let hasCamera = UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera)
        let buttons = hasCamera ? ["PhotoCamera", "PhotoLibrary", "SendDocument"] : ["PhotoLibrary", "SendDocument"]
        let tapBlock = { (index: Int) -> () in
            if index == 0 || (hasCamera && index == 1) {
                let pickerController = AAImagePickerController()
                pickerController.sourceType = (hasCamera && index == 0) ?
                    UIImagePickerControllerSourceType.Camera : UIImagePickerControllerSourceType.PhotoLibrary
                pickerController.mediaTypes = [kUTTypeImage as String]
                pickerController.view.backgroundColor = MainAppTheme.list.bgColor
                pickerController.navigationBar.tintColor = MainAppTheme.navigation.barColor
                pickerController.delegate = self
                pickerController.navigationBar.tintColor = MainAppTheme.navigation.titleColor
                pickerController.navigationBar.titleTextAttributes = [NSForegroundColorAttributeName: MainAppTheme.navigation.titleColor]
                self.presentViewController(pickerController, animated: true, completion: nil)
            } else if index >= 0 {
                if #available(iOS 8.0, *) {
                    let documentPicker = UIDocumentMenuViewController(documentTypes: UTTAll as! [String], inMode: UIDocumentPickerMode.Import)
                    documentPicker.view.backgroundColor = UIColor.clearColor()
                    documentPicker.delegate = self
                    self.presentViewController(documentPicker, animated: true, completion: nil)
                } else {
                    // Fallback on earlier versions
                }
            }
        }
        
        if (isIPad) {
            showActionSheet(buttons, cancelButton: "AlertCancel", destructButton: nil, sourceView: self.leftButton, sourceRect: self.leftButton.bounds, tapClosure: tapBlock)
        } else {
            showActionSheetFast(buttons, cancelButton: "AlertCancel", tapClosure: tapBlock)
        }
    }
 
    
//    override func onItemsAdded(indexes: [Int]) {
//        var toUpdate = [Int]()
//        for ind in indexes {
//            if !indexes.contains(ind + 1) {
//                if ind + 1 < getCount() {
//                    toUpdate.append(ind + 1)
//                }
//            }
//            if !indexes.contains(ind - 1) {
//                if ind > 0 {
//                    toUpdate.append(ind - 1)
//                }
//            }
//        }
//        updateRows(toUpdate)
//    }
    
    override func needFullReload(item: AnyObject?, cell: UICollectionViewCell) -> Bool {
        let message = (item as! ACMessage);
        if cell is AABubbleTextCell {
            if (message.content is ACPhotoContent) {
                return true
            }
        }
        
        return false
    }
    
    // Completition
    
    var filteredMembers = [ACMentionFilterResult]()
    
    override func canShowAutoCompletion() -> Bool {
        if UInt(self.peer.getPeerType().ordinal()) == ACPeerType.GROUP.rawValue {
            if self.foundPrefix == "@" {

                let oldCount = filteredMembers.count
                filteredMembers.removeAll(keepCapacity: true)
                
                let res = Actor.findMentionsWithGid(self.peer.getPeerId(), withQuery: self.foundWord)
                for index in 0..<res.size() {
                    filteredMembers.append(res.getWithInt(index) as! ACMentionFilterResult)
                }
                
                if oldCount == filteredMembers.count {
                    self.autoCompletionView.reloadData()
                }
                
                return filteredMembers.count > 0
            }
            
            return false
        }
        
        return false
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return filteredMembers.count
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let res = AutoCompleteCell(style: UITableViewCellStyle.Default, reuseIdentifier: "user_name")
        res.bindData(filteredMembers[indexPath.row], highlightWord: foundWord)
        return res
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let user = filteredMembers[indexPath.row]

        var postfix = " "
        if foundPrefixRange.location == 0 {
            postfix = ": "
        }
        
        acceptAutoCompletionWithString(user.getMentionString() + postfix, keepPrefix: !user.isNickname())
    }
    
    override func heightForAutoCompletionView() -> CGFloat {
        let cellHeight: CGFloat = 44.0;
        return cellHeight * CGFloat(filteredMembers.count)
    }
    
    override func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell,
        forRowAtIndexPath indexPath: NSIndexPath)
    {
        // Remove separator inset
        if cell.respondsToSelector("setSeparatorInset:") {
            cell.separatorInset = UIEdgeInsetsZero
        }
        
        // Prevent the cell from inheriting the Table View's margin settings
        if cell.respondsToSelector("setPreservesSuperviewLayoutMargins:") {
            if #available(iOS 8.0, *) {
                cell.preservesSuperviewLayoutMargins = false
            } else {
                // Fallback on earlier versions
            }
        }
        
        // Explictly set your cell's layout margins
        if cell.respondsToSelector("setLayoutMargins:") {
            if #available(iOS 8.0, *) {
                cell.layoutMargins = UIEdgeInsetsZero
            } else {
                // Fallback on earlier versions
            }
        }
    }
}

// MARK: -
// MARK: UIDocumentPicker Delegate

extension ConversationViewController: UIDocumentPickerDelegate {
    
    @available(iOS 8.0, *)
    func documentPicker(controller: UIDocumentPickerViewController, didPickDocumentAtURL url: NSURL) {
        let path = url.path!;
        let fileName = url.lastPathComponent
        let range = path.rangeOfString("/tmp", options: NSStringCompareOptions(), range: nil, locale: nil)
        let descriptor = path.substringFromIndex(range!.startIndex)
        NSLog("Picked file: \(descriptor)")
        Actor.trackDocumentSendWithPeer(peer)
        Actor.sendDocumentWithPeer(peer, withName: fileName, withMime: "application/octet-stream", withDescriptor: descriptor)
    }
    
}

// MARK: -
// MARK: UIImagePickerController Delegate

extension ConversationViewController: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage, editingInfo: [String : AnyObject]?) {
        MainAppTheme.navigation.applyStatusBar()
        picker.dismissViewControllerAnimated(true, completion: nil)
        Actor.trackPhotoSendWithPeer(peer)
        Actor.sendUIImage(image, peer: peer)
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject]) {
        MainAppTheme.navigation.applyStatusBar()
        picker.dismissViewControllerAnimated(true, completion: nil)
        
        Actor.sendUIImage(info[UIImagePickerControllerOriginalImage] as! UIImage, peer: peer)
    }
    
    func imagePickerControllerDidCancel(picker: UIImagePickerController) {
        MainAppTheme.navigation.applyStatusBar()
        picker.dismissViewControllerAnimated(true, completion: nil)
    }
    
}

extension ConversationViewController: UIDocumentMenuDelegate {
    @available(iOS 8.0, *)
    func documentMenu(documentMenu: UIDocumentMenuViewController, didPickDocumentPicker documentPicker: UIDocumentPickerViewController) {
        documentPicker.delegate = self
        self.presentViewController(documentPicker, animated: true, completion: nil)
    }
}