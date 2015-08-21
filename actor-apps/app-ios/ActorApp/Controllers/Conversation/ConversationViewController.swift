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
    
    // private let badgeView = UIImageView()
    private let titleView: UILabel = UILabel()
    private let subtitleView: UILabel = UILabel()
    private let navigationView: UIView = UIView()
    
    private let avatarView = BarAvatarView(frameSize: 36, type: .Rounded)
    
    private let backgroundView: UIView = UIView()
    
    // private var layoutCache: LayoutCache!
//    private let heightCache = HeightCache()
//    
//    // MARK: -
    // MARK: Public vars
    
    let binder: Binder = Binder();
    
    var unreadMessageId: jlong = 0
    
    // MARK: -
    // MARK: Constructors
    
    override init(peer: ACPeer) {
        super.init(peer: peer);
        
        // Messages
        
        self.collectionView.registerClass(AABubbleTextCell.self, forCellWithReuseIdentifier: BubbleTextIdentifier)
        self.collectionView.registerClass(AABubbleMediaCell.self, forCellWithReuseIdentifier: BubbleMediaIdentifier)
        self.collectionView.registerClass(AABubbleDocumentCell.self, forCellWithReuseIdentifier: BubbleDocumentIdentifier)
        self.collectionView.registerClass(AABubbleServiceCell.self, forCellWithReuseIdentifier: BubbleServiceIdentifier)
        self.collectionView.backgroundColor = UIColor.clearColor()
        self.collectionView.alwaysBounceVertical = true
        
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
        
        // titleView.frame = CGRectMake((navigationView.frame.width - 200) / 2, 4, 200, 20)
        titleView.font = UIFont(name: "HelveticaNeue-Medium", size: 17)!
        titleView.adjustsFontSizeToFitWidth = false;
        titleView.textColor = Resources.PrimaryLightText
        titleView.textAlignment = NSTextAlignment.Center;
        titleView.lineBreakMode = NSLineBreakMode.ByTruncatingTail;
        titleView.autoresizingMask = UIViewAutoresizing.FlexibleWidth;
        
        // subtitleView.frame = CGRectMake(0, 22, 200, 20);
        subtitleView.font = UIFont.systemFontOfSize(13);
        subtitleView.adjustsFontSizeToFitWidth = true;
        subtitleView.textColor = Resources.SecondaryLightText
        subtitleView.textAlignment = NSTextAlignment.Center;
        subtitleView.lineBreakMode = NSLineBreakMode.ByTruncatingTail;
        subtitleView.autoresizingMask = UIViewAutoresizing.FlexibleWidth;
        
        navigationView.addSubview(titleView)
        navigationView.addSubview(subtitleView)
        
        self.navigationItem.titleView = navigationView;
        // self.navigationItem.backBarButtonItem = UIBarButtonItem(title: "back", style: UIBarButtonItemStyle.Done, target: self, action: "back")
        
        // Navigation Avatar
        
        avatarView.frame = CGRectMake(0, 0, 36, 36)
        var avatarTapGesture = UITapGestureRecognizer(target: self, action: "onAvatarTap");
        avatarTapGesture.numberOfTapsRequired = 1
        avatarTapGesture.numberOfTouchesRequired = 1
        avatarView.addGestureRecognizer(avatarTapGesture)
        
        var barItem = UIBarButtonItem(customView: avatarView)
        self.navigationItem.rightBarButtonItem = barItem
        
//        self.singleTapGesture.cancelsTouchesInView = true
        
//        var longPressGesture = AALongPressGestureRecognizer(target: self, action: Selector("longPress:"))
//        self.collectionView.addGestureRecognizer(longPressGesture)
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
            var nameModel = user.getNameModel();
            
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
                    var stateText = Actor.getFormatter().formatPresence(presence, withSex: user.getSex())
                    self.subtitleView.text = stateText;
                    var state = UInt(presence!.getState().ordinal())
                    if (state == ACUserPresence_State.ONLINE.rawValue) {
                        self.subtitleView.textColor = Resources.PrimaryLightText
                    } else {
                        self.subtitleView.textColor = Resources.SecondaryLightText
                    }
                }
            })
        } else if (UInt(peer.getPeerType().ordinal()) == ACPeerType.GROUP.rawValue) {
            let group = Actor.getGroupWithGid(peer.getPeerId())
            var nameModel = group.getNameModel()
            
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
                        var uid = typingValue!.intAtIndex(0);
                        var user = Actor.getUserWithUid(uid)
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
                        var onlineString = Actor.getFormatter().formatGroupOnline(onlineCount!.intValue());
                        var attributedString = NSMutableAttributedString(string: (membersString + onlineString))
                        attributedString.addAttribute(NSForegroundColorAttributeName, value: Resources.PrimaryLightText, range: NSMakeRange(membersString.size, onlineString.size))
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
        
        // println("\(view.bounds.width)")
        // navigationView.frame = CGRectMake(navigationView.frame.minX, navigationView.frame.minY, 200, 44)
        titleView.frame = CGRectMake(0, 4, (navigationView.frame.width - 0), 20)
        subtitleView.frame = CGRectMake(0, 22, (navigationView.frame.width - 0), 20)
        
        // badgeView.frame = CGRectMake(290 - view.bounds.width, 4, 16, 16)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // unreadMessageId = MSG.loadLastReadState(peer)
        // navigationItem.backBarButtonItem = UIBarButtonItem(customView: badgeView)
        navigationItem.backBarButtonItem = UIBarButtonItem(title: nil, style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        Actor.onConversationClosedWithPeer(peer)
        
        (UIApplication.sharedApplication().delegate as! AppDelegate).hideBadge()
        
        // badgeView.removeFromSuperview()
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        (UIApplication.sharedApplication().delegate as! AppDelegate).showBadge()
        // var window = UIApplication.sharedApplication().keyWindow!
        // badgeView.frame = CGRectMake(22, 14, 22, 22)
        // badgeView.
        // window.addSubview(badgeView)
        
        if count(navigationController!.viewControllers) > 2 {
            if let firstController = navigationController!.viewControllers[0] as? UIViewController,
                let currentController: AnyObject = navigationController!.viewControllers[count(navigationController!.viewControllers) - 1] as? ConversationViewController {
                    navigationController!.setViewControllers([firstController, currentController], animated: false)
            }
        }
    }
    
    override func setUnread(rid: jlong) {
        self.unreadMessageId = rid
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
                            var menuController = UIMenuController.sharedMenuController()
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
            var navigation = AANavigationController()
            navigation.viewControllers = [controller]
            var popover = UIPopoverController(contentViewController:  navigation)
            controller.popover = popover
            popover.presentPopoverFromBarButtonItem(navigationItem.rightBarButtonItem!,
                permittedArrowDirections: UIPopoverArrowDirection.Up,
                animated: true)
        } else {
            navigateNext(controller, removeCurrent: false)
        }
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
        
        var hasCamera = UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera)
        var buttons = hasCamera ? ["PhotoCamera", "PhotoLibrary", "SendDocument"] : ["PhotoLibrary", "SendDocument"]
        var tapBlock = { (index: Int) -> () in
            if index == 0 || (hasCamera && index == 1) {
                var pickerController = AAImagePickerController()
                pickerController.sourceType = (hasCamera && index == 0) ?
                    UIImagePickerControllerSourceType.Camera : UIImagePickerControllerSourceType.PhotoLibrary
                pickerController.mediaTypes = [kUTTypeImage]
                pickerController.view.backgroundColor = MainAppTheme.list.bgColor
                pickerController.navigationBar.tintColor = MainAppTheme.navigation.barColor
                pickerController.delegate = self
                pickerController.navigationBar.tintColor = MainAppTheme.navigation.titleColor
                pickerController.navigationBar.titleTextAttributes = [NSForegroundColorAttributeName: MainAppTheme.navigation.titleColor]
                self.presentViewController(pickerController, animated: true, completion: nil)
            } else if index >= 0 {
                var documentPicker = UIDocumentMenuViewController(documentTypes: UTTAll, inMode: UIDocumentPickerMode.Import)
                documentPicker.view.backgroundColor = UIColor.clearColor()
                documentPicker.delegate = self
                self.presentViewController(documentPicker, animated: true, completion: nil)
            }
        }
        
        if (isIPad) {
            showActionSheet(buttons, cancelButton: "AlertCancel", destructButton: nil, sourceView: self.leftButton, sourceRect: self.leftButton.bounds, tapClosure: tapBlock)
        } else {
            showActionSheetFast(buttons, cancelButton: "AlertCancel", tapClosure: tapBlock)
        }
    }
    
    override func buildCell(collectionView: UICollectionView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?) -> UICollectionViewCell {
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
    
    override func bindCell(collectionView: UICollectionView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?, cell: UICollectionViewCell) {
        var list = getProcessedList()
        var message = list!.items[indexPath.row]
        var setting = list!.cellSettings[indexPath.row]
        var bubbleCell = (cell as! AABubbleCell)
        bubbleCell.performBind(message, setting: setting, layoutCache: list!.layoutCache)
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
    
    override func onItemsAdded(indexes: [Int]) {
        var toUpdate = [Int]()
        for ind in indexes {
            if !indexes.contains(ind + 1) {
                if ind + 1 < getCount() {
                    toUpdate.append(ind + 1)
                }
            }
            if !indexes.contains(ind - 1) {
                if ind > 0 {
                    toUpdate.append(ind - 1)
                }
            }
        }
        updateRows(toUpdate)
    }
    
    override func needFullReload(item: AnyObject?, cell: UICollectionViewCell) -> Bool {
        var message = (item as! ACMessage);
        if cell is AABubbleTextCell {
            if (message.content is ACPhotoContent) {
                return true
            }
        }
        
        return false
    }
    
//    func buildCellSetting(index: Int) -> CellSetting {
//        return CellSetting(showDate: false, clenchTop: false, clenchBottom: false, showNewMessages: false)
//        
////        var current = objectAtIndex(index) as! ACMessage
////        var next: ACMessage! = index > 0 ? objectAtIndex(index - 1) as! ACMessage : nil
////        var prev: ACMessage! = index + 1 < getCount() ? objectAtIndex(index + 1) as! ACMessage : nil
////        
////        var isShowDate = true
////        var isShowDateNext = true
////        var isShowNewMessages = (unreadMessageId == current.rid)
////        var clenchTop = false
////        var clenchBottom = false
////
////        if (prev != nil) {
////            isShowDate = !areSameDate(current, prev: prev)
////            if !isShowDate {
////                clenchTop = useCompact(current, next: prev)
////            }
////        }
////        
////        if (next != nil) {
////            if areSameDate(next, prev: current) {
////                clenchBottom = useCompact(current, next: next)
////            }
////        }
////        
////        return CellSetting(showDate: isShowDate, clenchTop: clenchTop, clenchBottom: clenchBottom, showNewMessages: isShowNewMessages)
//    }
//    
//    func useCompact(source: ACMessage, next: ACMessage) -> Bool {
//        if (source.content is ACServiceContent) {
//            if (next.content is ACServiceContent) {
//                return true
//            }
//        } else {
//            if (next.content is ACServiceContent) {
//                return false
//            }
//            if (source.senderId == next.senderId) {
//                return true
//            }
//        }
//        
//        return false
//    }
//    
//    func areSameDate(source:ACMessage, prev: ACMessage) -> Bool {
//        let calendar = NSCalendar.currentCalendar()
//        
//        var currentDate = NSDate(timeIntervalSince1970: Double(source.date)/1000.0)
//        var currentDateComp = calendar.components(.CalendarUnitDay | .CalendarUnitYear | .CalendarUnitMonth, fromDate: currentDate)
//        
//        var nextDate = NSDate(timeIntervalSince1970: Double(prev.date)/1000.0)
//        var nextDateComp = calendar.components(.CalendarUnitDay | .CalendarUnitYear | .CalendarUnitMonth, fromDate: nextDate)
//
//        return (currentDateComp.year == nextDateComp.year && currentDateComp.month == nextDateComp.month && currentDateComp.day == nextDateComp.day)
//    }

    override func displayListForController() -> ARBindedDisplayList {
        var res = Actor.getMessageDisplayList(peer)
        if (res.getBackgroundProcessor() == nil) {
            var processor = BubbleBackgroundProcessor()
            res.setBackgroundProcessor(processor)
            let group = peer.getPeerType().ordinal() == jint(ACPeerType.GROUP.rawValue)
            res.setListProcessor(ListProcessor(layoutCache: processor.layoutCache, isGroup: group))
        }
        // layoutCache = (res.getBackgroundProcessor() as! BubbleBackgroundProcessor).layoutCache
        return res
    }
    
    // Completition
    
    var filteredMembers = [ACMentionFilterResult]()
    
    override func canShowAutoCompletion() -> Bool {
        if UInt(self.peer.getPeerType().ordinal()) == ACPeerType.GROUP.rawValue {
            if self.foundPrefix == "@" {
//                var group = Actor.getGroups().getWithId(jlong(self.peer.getPeerId()))
//                var members = (group.getMembersModel().get() as! JavaUtilHashSet).toArray()
//            
                var oldCount = filteredMembers.count
                filteredMembers.removeAll(keepCapacity: true)
                
                var res = Actor.findMentionsWithGid(self.peer.getPeerId(), withQuery: self.foundWord)
                for index in 0..<res.size() {
                    filteredMembers.append(res.getWithInt(index) as! ACMentionFilterResult)
                }
                
//                for index in 0..<members.length() {
//                    if let groupMember = members.objectAtIndex(UInt(index)) as? ACGroupMember,
//                        let user = Actor.getUserWithUid(groupMember.getUid()) {
//                            if user.getId() != Actor.myUid() {
//                                var isFiltered = false
//                                if self.foundWord != "" {
//                                    var nick = user.getNickModel().get()
//                                    if nick != nil && nick.hasPrefixInWords(self.foundWord) {
//                                        isFiltered = true
//                                    }
//                                    if !isFiltered {
//                                        isFiltered = user.getNameModel().get().hasPrefixInWords(self.foundWord)
//                                    }
//                                } else {
//                                    isFiltered = true
//                                }
//                            
//                                if isFiltered {
//                                    filteredMembers.append(user)
//                                }
//                            }
//                    }
//                }
                
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
        var res = AutoCompleteCell(style: UITableViewCellStyle.Default, reuseIdentifier: "user_name")
        res.bindData(filteredMembers[indexPath.row], highlightWord: foundWord)
        return res
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        var user = filteredMembers[indexPath.row]

        var postfix = " "
        if foundPrefixRange.location == 0 {
            postfix = ": "
        }
        
        acceptAutoCompletionWithString(user.getMentionString() + postfix, keepPrefix: !user.isNickname())
    }
    
    override func heightForAutoCompletionView() -> CGFloat {
        var cellHeight: CGFloat = 44.0;
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
            cell.preservesSuperviewLayoutMargins = false
        }
        
        // Explictly set your cell's layout margins
        if cell.respondsToSelector("setLayoutMargins:") {
            cell.layoutMargins = UIEdgeInsetsZero
        }
    }
}

// MARK: -
// MARK: UIDocumentPicker Delegate

extension ConversationViewController: UIDocumentPickerDelegate {
    
    func documentPicker(controller: UIDocumentPickerViewController, didPickDocumentAtURL url: NSURL) {
        var path = url.path!;
        var fileName = url.lastPathComponent
        var range = path.rangeOfString("/tmp", options: NSStringCompareOptions.allZeros, range: nil, locale: nil)
        var descriptor = path.substringFromIndex(range!.startIndex)
        NSLog("Picked file: \(descriptor)")
        Actor.trackDocumentSendWithPeer(peer)
        Actor.sendDocumentWithPeer(peer, withName: fileName, withMime: "application/octet-stream", withDescriptor: descriptor)
    }
    
}

// MARK: -
// MARK: UIImagePickerController Delegate

extension ConversationViewController: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage!, editingInfo: [NSObject : AnyObject]!) {
        MainAppTheme.navigation.applyStatusBar()
        picker.dismissViewControllerAnimated(true, completion: nil)
        Actor.trackPhotoSendWithPeer(peer)
        Actor.sendUIImage(image, peer: peer)
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [NSObject : AnyObject]) {
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
    func documentMenu(documentMenu: UIDocumentMenuViewController, didPickDocumentPicker documentPicker: UIDocumentPickerViewController) {
        documentPicker.delegate = self
        self.presentViewController(documentPicker, animated: true, completion: nil)
    }
}