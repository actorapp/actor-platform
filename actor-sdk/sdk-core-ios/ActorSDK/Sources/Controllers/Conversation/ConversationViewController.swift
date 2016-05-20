//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit
import MobileCoreServices
import AddressBook
import AddressBookUI

public class ConversationViewController:
    AAConversationContentController,
    UIDocumentMenuDelegate,
    UIDocumentPickerDelegate,
    UIImagePickerControllerDelegate,
    UINavigationControllerDelegate,
    AALocationPickerControllerDelegate,
    ABPeoplePickerNavigationControllerDelegate,
    AAAudioRecorderDelegate,
    AAConvActionSheetDelegate,
    AAStickersKeyboardDelegate {
    
    // Data binder
    private let binder = AABinder()
    
    // Internal state
    // Members for autocomplete
    var filteredMembers = [ACMentionFilterResult]()
    let content: ACPage!
    var appStyle: ActorStyle { get { return ActorSDK.sharedActor().style } }
    
    
    //
    // Views
    //
    
    private let titleView: UILabel = UILabel()
    private let subtitleView: UILabel = UILabel()
    private let navigationView: UIView = UIView()
    private let avatarView = AABarAvatarView()
    private let backgroundView = UIImageView()
    private var audioButton: UIButton = UIButton()
    private var voiceRecorderView : AAVoiceRecorderView!
    
    
    //
    // Stickers
    //
    
    private var stickersView: AAStickersKeyboard!
    private var stickersButton : UIButton!
    private var stickersOpen = false
    
    
    //
    // Audio Recorder
    //
    
    public var audioRecorder: AAAudioRecorder!
    
    
    //
    // Mode
    //
    
    private var textMode:Bool!
    private var micOn: Bool! = true
    

    
    ////////////////////////////////////////////////////////////
    // MARK: - Init
    ////////////////////////////////////////////////////////////
    
    required override public init(peer: ACPeer) {
        
        // Data
        
        self.content = ACAllEvents_Chat_viewWithACPeer_(peer)
        
        // Create controller
        
        super.init(peer: peer)
        
        
        //
        // Background
        //
        
        backgroundView.clipsToBounds = true
        backgroundView.contentMode = .ScaleAspectFill
        backgroundView.backgroundColor = appStyle.chatBgColor
        
        // Custom background if available
        if let bg = Actor.getSelectedWallpaper() {
            if bg.startsWith("local:") {
                backgroundView.image = UIImage.bundled(bg.skip(6))
            } else {
                let path = CocoaFiles.pathFromDescriptor(bg.skip(5))
                backgroundView.image = UIImage(contentsOfFile:path)
            }
        }
        view.insertSubview(backgroundView, atIndex: 0)
        
        
        //
        // slk settings
        //
        self.bounces = false
        self.keyboardPanningEnabled = true
        self.registerPrefixesForAutoCompletion(["@"])
        
        
        //
        // Text Input
        //
        self.textInputbar.backgroundColor = appStyle.chatInputFieldBgColor
        self.textInputbar.autoHideRightButton = false;
        self.textInputbar.translucent = false
        
        
        //
        // Text view
        //
        self.textView.placeholder = AALocalized("ChatPlaceholder")
        self.textView.keyboardAppearance = ActorSDK.sharedActor().style.isDarkApp ? .Dark : .Light
        
        
        //
        // Add stickers button
        //
        self.stickersButton = UIButton(type: UIButtonType.System)
        self.stickersButton.tintColor = UIColor.lightGrayColor().colorWithAlphaComponent(0.5)
        self.stickersButton.setImage(UIImage.bundled("sticker_button"), forState: UIControlState.Normal)
        self.stickersButton.addTarget(self, action: #selector(ConversationViewController.changeKeyboard), forControlEvents: UIControlEvents.TouchUpInside)
        self.textInputbar.addSubview(stickersButton)
        
        
        //
        // Check text for set right button
        //
        let checkText = Actor.loadDraftWithPeer(peer)!.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet())
        
        if (checkText.isEmpty) {
            
            self.textMode = false
            
            self.rightButton.tintColor = appStyle.chatSendColor
            self.rightButton.setImage(UIImage.tinted("aa_micbutton", color: appStyle.chatAttachColor), forState: UIControlState.Normal)
            self.rightButton.setTitle("", forState: UIControlState.Normal)
            self.rightButton.enabled = true
            
            self.rightButton.layoutIfNeeded()
            
            self.rightButton.addTarget(self, action: #selector(ConversationViewController.beginRecord(_:event:)), forControlEvents: UIControlEvents.TouchDown)
            self.rightButton.addTarget(self, action: #selector(ConversationViewController.mayCancelRecord(_:event:)), forControlEvents: UIControlEvents.TouchDragInside.union(UIControlEvents.TouchDragOutside))
            self.rightButton.addTarget(self, action: #selector(ConversationViewController.finishRecord(_:event:)), forControlEvents: UIControlEvents.TouchUpInside.union(UIControlEvents.TouchCancel).union(UIControlEvents.TouchUpOutside))
            
        } else {
            
            self.textMode = true
            
            self.stickersButton.hidden = true
            
            self.rightButton.setTitle(AALocalized("ChatSend"), forState: UIControlState.Normal)
            self.rightButton.setTitleColor(appStyle.chatSendColor, forState: UIControlState.Normal)
            self.rightButton.setTitleColor(appStyle.chatSendDisabledColor, forState: UIControlState.Disabled)
            self.rightButton.setImage(nil, forState: UIControlState.Normal)
            self.rightButton.enabled = true
            
            self.rightButton.layoutIfNeeded()
        }
        
        
        //
        // Voice Recorder
        //
        self.audioRecorder = AAAudioRecorder()
        self.audioRecorder.delegate = self
        
        self.leftButton.setImage(UIImage.tinted("conv_attach", color: appStyle.chatAttachColor), forState: UIControlState.Normal)
        
        
        //
        // Navigation Title
        //
        
        navigationView.frame = CGRectMake(0, 0, 200, 44)
        navigationView.autoresizingMask = UIViewAutoresizing.FlexibleWidth
        
        titleView.font = UIFont.mediumSystemFontOfSize(17)
        titleView.adjustsFontSizeToFitWidth = false
        titleView.textAlignment = NSTextAlignment.Center
        titleView.lineBreakMode = NSLineBreakMode.ByTruncatingTail
        titleView.autoresizingMask = UIViewAutoresizing.FlexibleWidth
        titleView.textColor = appStyle.navigationTitleColor
        
        subtitleView.font = UIFont.systemFontOfSize(13)
        subtitleView.adjustsFontSizeToFitWidth = true
        subtitleView.textAlignment = NSTextAlignment.Center
        subtitleView.lineBreakMode = NSLineBreakMode.ByTruncatingTail
        subtitleView.autoresizingMask = UIViewAutoresizing.FlexibleWidth
        
        navigationView.addSubview(titleView)
        navigationView.addSubview(subtitleView)
        
        self.navigationItem.titleView = navigationView
        
        
        //
        // Navigation Avatar
        //
        avatarView.frame = CGRectMake(0, 0, 36, 36)
        avatarView.viewDidTap = onAvatarTap
        
        let barItem = UIBarButtonItem(customView: avatarView)
        let isBot: Bool
        if (peer.isPrivate) {
            isBot = Bool(Actor.getUserWithUid(peer.peerId).isBot())
        } else {
            isBot = false
        }
        if (ActorSDK.sharedActor().enableCalls && !isBot && peer.isPrivate) {
            let callButtonView = AACallButton(image: UIImage.bundled("ic_call_outline_22")?.tintImage(ActorSDK.sharedActor().style.navigationTintColor))
            callButtonView.viewDidTap = onCallTap
            let callButtonItem = UIBarButtonItem(customView: callButtonView)
            self.navigationItem.rightBarButtonItems = [barItem, callButtonItem]
        } else {
            self.navigationItem.rightBarButtonItems = [barItem]
        }
    }
    
    required public init(coder aDecoder: NSCoder!) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override public func viewDidLoad() {
        super.viewDidLoad()
        
        self.voiceRecorderView = AAVoiceRecorderView(frame: CGRectMake(0,0,self.view.frame.size.width-30,44))
        self.voiceRecorderView.hidden = true
        self.voiceRecorderView.binedController = self
        self.textInputbar.addSubview(self.voiceRecorderView)
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
        
        let frame = CGRectMake(0, 0, self.view.frame.size.width, 216)
        self.stickersView = AAStickersKeyboard(frame: frame)
        self.stickersView.delegate = self
        
        NSNotificationCenter.defaultCenter().addObserver(
            self,
            selector: #selector(ConversationViewController.updateStickersStateOnCloseKeyboard),
            name: SLKKeyboardWillHideNotification,
            object: nil)
    }
    
    public override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        self.stickersButton.frame = CGRectMake(self.view.frame.size.width-67, 12, 20, 20)
    }
    
    ////////////////////////////////////////////////////////////
    // MARK: - Lifecycle
    ////////////////////////////////////////////////////////////
    
    override public func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        // Installing bindings
        if (peer.peerType.ordinal() == ACPeerType.PRIVATE().ordinal()) {

            let user = Actor.getUserWithUid(peer.peerId)
            let nameModel = user.getNameModel()
            let blockStatus = user.isBlockedModel().get().booleanValue()
            
            binder.bind(nameModel, closure: { (value: NSString?) -> () in
                self.titleView.text = String(value!)
                self.navigationView.sizeToFit()
            })
            binder.bind(user.getAvatarModel(), closure: { (value: ACAvatar?) -> () in
                self.avatarView.bind(user.getNameModel().get(), id: Int(user.getId()), avatar: value)
            })
            
            binder.bind(Actor.getTypingWithUid(peer.peerId), valueModel2: user.getPresenceModel(), closure:{ (typing:JavaLangBoolean?, presence:ACUserPresence?) -> () in
                
                if (typing != nil && typing!.booleanValue()) {
                    self.subtitleView.text = Actor.getFormatter().formatTyping()
                    self.subtitleView.textColor = self.appStyle.navigationSubtitleActiveColor
                } else {
                    if (user.isBot().boolValue) {
                        self.subtitleView.text = "bot"
                        self.subtitleView.textColor = self.appStyle.userOnlineNavigationColor
                    } else {
                        let stateText = Actor.getFormatter().formatPresence(presence, withSex: user.getSex())
                        self.subtitleView.text = stateText;
                        let state = presence!.state.ordinal()
                        if (state == ACUserPresence_State.ONLINE().ordinal()) {
                            self.subtitleView.textColor = self.appStyle.userOnlineNavigationColor
                        } else {
                            self.subtitleView.textColor = self.appStyle.userOfflineNavigationColor
                        }
                    }
                }
            })
            
            //
            //Unblock User
            //
            
            if(blockStatus){
                
                let unblockActionSheet = AAUnblockActionSheet()
                unblockActionSheet.delegate = self
                unblockActionSheet.presentInController(self)
            
            }
        
        } else if (peer.peerType.ordinal() == ACPeerType.GROUP().ordinal()) {
            let group = Actor.getGroupWithGid(peer.peerId)
            let nameModel = group.getNameModel()
            
            binder.bind(nameModel, closure: { (value: NSString?) -> () in
                self.titleView.text = String(value!);
                self.navigationView.sizeToFit();
            })
            binder.bind(group.getAvatarModel(), closure: { (value: ACAvatar?) -> () in
                self.avatarView.bind(group.getNameModel().get(), id: Int(group.getId()), avatar: value)
            })
            binder.bind(Actor.getGroupTypingWithGid(group.getId()), valueModel2: group.getMembersModel(), valueModel3: group.getPresenceModel(), closure: { (typingValue:IOSIntArray?, members:JavaUtilHashSet?, onlineCount:JavaLangInteger?) -> () in
                if (!group.isMemberModel().get().booleanValue()) {
                    self.subtitleView.text = AALocalized("ChatNoGroupAccess")
                    self.subtitleView.textColor = self.appStyle.navigationSubtitleColor
                    self.setTextInputbarHidden(true, animated: true)
                    return
                } else {
                    self.setTextInputbarHidden(false, animated: false)
                }
                
                if (typingValue != nil && typingValue!.length() > 0) {
                    self.subtitleView.textColor = self.appStyle.navigationSubtitleActiveColor
                    if (typingValue!.length() == 1) {
                        let uid = typingValue!.intAtIndex(0);
                        let user = Actor.getUserWithUid(uid)
                        self.subtitleView.text = Actor.getFormatter().formatTypingWithName(user.getNameModel().get())
                    } else {
                        self.subtitleView.text = Actor.getFormatter().formatTypingWithCount(typingValue!.length());
                    }
                } else {
                    var membersString = Actor.getFormatter().formatGroupMembers(members!.size())
                    self.subtitleView.textColor = self.appStyle.navigationSubtitleColor
                    if (onlineCount == nil || onlineCount!.integerValue == 0) {
                        self.subtitleView.text = membersString;
                    } else {
                        membersString = membersString + ", ";
                        let onlineString = Actor.getFormatter().formatGroupOnline(onlineCount!.intValue());
                        let attributedString = NSMutableAttributedString(string: (membersString + onlineString))
                        attributedString.addAttribute(NSForegroundColorAttributeName, value: self.appStyle.userOnlineNavigationColor, range: NSMakeRange(membersString.length, onlineString.length))
                        self.subtitleView.attributedText = attributedString
                    }
                }
            })
        }
        
        Actor.onConversationOpenWithPeer(peer)
        ActorSDK.sharedActor().trackPageVisible(content)
        
        
        if textView.isFirstResponder() == false {
            textView.resignFirstResponder()
        }
        
        textView.text = Actor.loadDraftWithPeer(peer)
        
    }
    
    override public func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        backgroundView.frame = view.bounds
        
        titleView.frame = CGRectMake(0, 4, (navigationView.frame.width - 0), 20)
        subtitleView.frame = CGRectMake(0, 22, (navigationView.frame.width - 0), 20)
        
        stickersView.frame = CGRectMake(0, 0, self.view.frame.size.width, 216)
    }
    
    override public func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        if navigationController!.viewControllers.count > 2 {
            let firstController = navigationController!.viewControllers[0]
            let currentController = navigationController!.viewControllers[navigationController!.viewControllers.count - 1]
            navigationController!.setViewControllers([firstController, currentController], animated: false)
        }
        
        if !AADevice.isiPad {
            AANavigationBadge.showBadge()
        }
    }
    
    override public func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        Actor.onConversationClosedWithPeer(peer)
        ActorSDK.sharedActor().trackPageHidden(content)

        if !AADevice.isiPad {
            AANavigationBadge.hideBadge()
        }
        
        // Closing keyboard
        self.textView.resignFirstResponder()
    }

    override public func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated)
        
        Actor.saveDraftWithPeer(peer, withDraft: textView.text)
        
        // Releasing bindings
        binder.unbindAll()
    }

    ////////////////////////////////////////////////////////////
    // MARK: - Chat avatar tap
    ////////////////////////////////////////////////////////////
    
    func onAvatarTap() {
        let id = Int(peer.peerId)
        var controller: AAViewController!
        if (peer.peerType.ordinal() == ACPeerType.PRIVATE().ordinal()) {
            controller = ActorSDK.sharedActor().delegate.actorControllerForUser(id)
            if controller == nil {
                controller = AAUserViewController(uid: id)
            }
        } else if (peer.peerType.ordinal() == ACPeerType.GROUP().ordinal()) {
            controller = ActorSDK.sharedActor().delegate.actorControllerForGroup(id)
            if controller == nil {
                controller = AAGroupViewController(gid: id)
            }
        } else {
            return
        }
        
        if (AADevice.isiPad) {
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
    
    func onCallTap() {
        if (self.peer.isGroup) {
            execute(ActorSDK.sharedActor().messenger.doCallWithGid(self.peer.peerId))
        } else if (self.peer.isPrivate) {
            execute(ActorSDK.sharedActor().messenger.doCallWithUid(self.peer.peerId))
        }
    }
    
    ////////////////////////////////////////////////////////////
    // MARK: - Text bar actions
    ////////////////////////////////////////////////////////////
    
    override public func textDidUpdate(animated: Bool) {
        super.textDidUpdate(animated)
        Actor.onTypingWithPeer(peer)
        checkTextInTextView()
    }
    
    func checkTextInTextView() {
        
        let text = self.textView.text.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet())
        self.rightButton.enabled = true
        
        //change button's
        
        if !text.isEmpty && textMode == false {
            
            self.rightButton.removeTarget(self, action: #selector(ConversationViewController.beginRecord(_:event:)), forControlEvents: UIControlEvents.TouchDown)
            self.rightButton.removeTarget(self, action: #selector(ConversationViewController.mayCancelRecord(_:event:)), forControlEvents: UIControlEvents.TouchDragInside.union(UIControlEvents.TouchDragOutside))
            self.rightButton.removeTarget(self, action: #selector(ConversationViewController.finishRecord(_:event:)), forControlEvents: UIControlEvents.TouchUpInside.union(UIControlEvents.TouchCancel).union(UIControlEvents.TouchUpOutside))
            
            self.rebindRightButton()
            
            self.stickersButton.hidden = true
            
            self.rightButton.setTitle(AALocalized("ChatSend"), forState: UIControlState.Normal)
            self.rightButton.setTitleColor(appStyle.chatSendColor, forState: UIControlState.Normal)
            self.rightButton.setTitleColor(appStyle.chatSendDisabledColor, forState: UIControlState.Disabled)
            self.rightButton.setImage(nil, forState: UIControlState.Normal)
            
            self.rightButton.layoutIfNeeded()
            self.textInputbar.layoutIfNeeded()
            
            self.textMode = true
            
        } else if (text.isEmpty && textMode == true) {
            
            self.rightButton.addTarget(self, action: #selector(ConversationViewController.beginRecord(_:event:)), forControlEvents: UIControlEvents.TouchDown)
            self.rightButton.addTarget(self, action: #selector(ConversationViewController.mayCancelRecord(_:event:)), forControlEvents: UIControlEvents.TouchDragInside.union(UIControlEvents.TouchDragOutside))
            self.rightButton.addTarget(self, action: #selector(ConversationViewController.finishRecord(_:event:)), forControlEvents: UIControlEvents.TouchUpInside.union(UIControlEvents.TouchCancel).union(UIControlEvents.TouchUpOutside))
            
            self.stickersButton.hidden = false
            
                
            self.rightButton.tintColor = appStyle.chatAttachColor
            self.rightButton.setImage(UIImage.bundled("aa_micbutton"), forState: UIControlState.Normal)
            self.rightButton.setTitle("", forState: UIControlState.Normal)
            self.rightButton.enabled = true
            
            
            self.rightButton.layoutIfNeeded()
            self.textInputbar.layoutIfNeeded()
            
            self.textMode = false
            
        }
        
    }
    
    ////////////////////////////////////////////////////////////
    // MARK: - Right/Left button pressed
    ////////////////////////////////////////////////////////////
    
    override public func didPressRightButton(sender: AnyObject!) {
        if !self.textView.text.isEmpty {
            Actor.sendMessageWithMentionsDetect(peer, withText: textView.text)
            super.didPressRightButton(sender)
        }
    }
    
    override public func didPressLeftButton(sender: AnyObject!) {
        super.didPressLeftButton(sender)
        
        self.textInputbar.textView.resignFirstResponder()
        
        self.rightButton.layoutIfNeeded()
        
        let actionSheet = AAConvActionSheet()
        actionSheet.delegate = self
        actionSheet.presentInController(self)
    }
 
    ////////////////////////////////////////////////////////////
    // MARK: - Completition
    ////////////////////////////////////////////////////////////
    
    override public func didChangeAutoCompletionPrefix(prefix: String!, andWord word: String!) {
        if self.peer.peerType.ordinal() == ACPeerType.GROUP().ordinal() {
            if prefix == "@" {
                
                let oldCount = filteredMembers.count
                filteredMembers.removeAll(keepCapacity: true)
                
                let res = Actor.findMentionsWithGid(self.peer.peerId, withQuery: word)
                for index in 0..<res.size() {
                    filteredMembers.append(res.getWithInt(index) as! ACMentionFilterResult)
                }
                
                if oldCount == filteredMembers.count {
                    self.autoCompletionView.reloadData()
                }
                
                dispatchOnUi { () -> Void in
                    self.showAutoCompletionView(self.filteredMembers.count > 0)
                }
                return
            }
        }
        
        dispatchOnUi { () -> Void in
            self.showAutoCompletionView(false)
        }
    }
    
    ////////////////////////////////////////////////////////////
    // MARK: - TableView for completition
    ////////////////////////////////////////////////////////////
    
    override public func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return filteredMembers.count
    }
    
    override public func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let res = AAAutoCompleteCell(style: UITableViewCellStyle.Default, reuseIdentifier: "user_name")
        res.bindData(filteredMembers[indexPath.row], highlightWord: foundWord)
        return res
    }
    
    override public func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let user = filteredMembers[indexPath.row]

        var postfix = " "
        if foundPrefixRange.location == 0 {
            postfix = ": "
        }
        
        acceptAutoCompletionWithString(user.mentionString + postfix, keepPrefix: !user.isNickname)
    }
    
    override public func heightForAutoCompletionView() -> CGFloat {
        let cellHeight: CGFloat = 44.0;
        return cellHeight * CGFloat(filteredMembers.count)
    }
    
    override public func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        cell.separatorInset = UIEdgeInsetsZero
        cell.preservesSuperviewLayoutMargins = false
        cell.layoutMargins = UIEdgeInsetsZero
    }

    ////////////////////////////////////////////////////////////
    // MARK: - Picker
    ////////////////////////////////////////////////////////////
    
    public func actionSheetPickCamera() {
        pickImage(.Camera)
    }
    
    public func actionSheetPickGallery() {
        pickImage(.PhotoLibrary)
    }
    
    public func actionSheetPickContact() {
        let pickerController = ABPeoplePickerNavigationController()
        pickerController.peoplePickerDelegate = self
        self.presentViewController(pickerController, animated: true, completion: nil)
    }
    
    public func actionSheetPickLocation() {
        let pickerController = AALocationPickerController()
        pickerController.delegate = self
        self.presentViewController(AANavigationController(rootViewController:pickerController), animated: true, completion: nil)
    }
    
    public func actionSheetPickedImages(images: [UIImage]) {
        for i in images {
            Actor.sendUIImage(i, peer: peer)
        }
    }
    
    public func actionSheetPickDocument() {
        let documentPicker = UIDocumentMenuViewController(documentTypes: UTTAll as! [String], inMode: UIDocumentPickerMode.Import)
        documentPicker.view.backgroundColor = UIColor.clearColor()
        documentPicker.delegate = self
        self.presentViewController(documentPicker, animated: true, completion: nil)
    }
    
    public func actionSheetUnblockContact() {
        self.executePromise(Actor.unblockUser(Actor.getUserWithUid(peer.peerId).getId()))
    }
    
    ////////////////////////////////////////////////////////////
    // MARK: - Document picking
    ////////////////////////////////////////////////////////////
    
    public func documentMenu(documentMenu: UIDocumentMenuViewController, didPickDocumentPicker documentPicker: UIDocumentPickerViewController) {
        documentPicker.delegate = self
        self.presentViewController(documentPicker, animated: true, completion: nil)
    }
    
    public func documentPicker(controller: UIDocumentPickerViewController, didPickDocumentAtURL url: NSURL) {
        
        // Loading path and file name
        let path = url.path!
        let fileName = url.lastPathComponent
        
        // Check if file valid or directory
        var isDir : ObjCBool = false
        if !NSFileManager.defaultManager().fileExistsAtPath(path, isDirectory: &isDir) {
            // Not exists
            return
        }
        
        // Destination file
        let descriptor = "/tmp/\(NSUUID().UUIDString)"
        let destPath = CocoaFiles.pathFromDescriptor(descriptor)
        
        if isDir {
            
            // Zipping contents and sending
            execute(AATools.zipDirectoryCommand(path, to: destPath)) { (val) -> Void in
                Actor.sendDocumentWithPeer(self.peer, withName: fileName, withMime: "application/zip", withDescriptor: descriptor)
            }
        } else {
            
            // Sending file itself
            execute(AATools.copyFileCommand(path, to: destPath)) { (val) -> Void in
                Actor.sendDocumentWithPeer(self.peer, withName: fileName, withMime: "application/octet-stream", withDescriptor: descriptor)
            }
        }
    }
    
    
    ////////////////////////////////////////////////////////////
    // MARK: - Image picking
    ////////////////////////////////////////////////////////////
    
    func pickImage(source: UIImagePickerControllerSourceType) {
        let pickerController = AAImagePickerController()
        pickerController.sourceType = source
        pickerController.mediaTypes = [kUTTypeImage as String,kUTTypeMovie as String]

        pickerController.delegate = self

        self.presentViewController(pickerController, animated: true, completion: nil)
    }
    
    public func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage, editingInfo: [String : AnyObject]?) {
        picker.dismissViewControllerAnimated(true, completion: nil)
        Actor.sendUIImage(image, peer: peer)
    }
    
    public func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject]) {
        picker.dismissViewControllerAnimated(true, completion: nil)
        
        if let image = info[UIImagePickerControllerOriginalImage] as? UIImage {
            Actor.sendUIImage(image, peer: peer)
            
        } else {
            Actor.sendVideo(info[UIImagePickerControllerMediaURL] as! NSURL, peer: peer)
        }
        
    }
    
    public func imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion: nil)
    }
    
    ////////////////////////////////////////////////////////////
    // MARK: - Location picking
    ////////////////////////////////////////////////////////////

    public func locationPickerDidCancelled(controller: AALocationPickerController) {
        controller.dismissViewControllerAnimated(true, completion: nil)
    }
    
    public func locationPickerDidPicked(controller: AALocationPickerController, latitude: Double, longitude: Double) {
        Actor.sendLocationWithPeer(self.peer, withLongitude: JavaLangDouble(double: longitude), withLatitude: JavaLangDouble(double: latitude), withStreet: nil, withPlace: nil)
        controller.dismissViewControllerAnimated(true, completion: nil)
    }
    
    ////////////////////////////////////////////////////////////
    // MARK: - Contact picking
    ////////////////////////////////////////////////////////////
    
    public func peoplePickerNavigationController(peoplePicker: ABPeoplePickerNavigationController, didSelectPerson person: ABRecord) {
        
        // Dismissing picker
        
        peoplePicker.dismissViewControllerAnimated(true, completion: nil)
        
        // Names
        
        let name = ABRecordCopyCompositeName(person)?.takeRetainedValue() as String?
        
        // Avatar
        
        var jAvatarImage: String? = nil
        let hasAvatarImage = ABPersonHasImageData(person)
        if (hasAvatarImage) {
            let imgData = ABPersonCopyImageDataWithFormat(person, kABPersonImageFormatOriginalSize).takeRetainedValue()
            let image = UIImage(data: imgData)?.resizeSquare(90, maxH: 90)
            if (image != nil) {
                let thumbData = UIImageJPEGRepresentation(image!, 0.55)
                jAvatarImage = thumbData?.base64EncodedStringWithOptions(NSDataBase64EncodingOptions())
            }
        }
        
        // Phones
        
        let jPhones = JavaUtilArrayList()
        let phoneNumbers: ABMultiValueRef = ABRecordCopyValue(person, kABPersonPhoneProperty).takeRetainedValue()
        let phoneCount = ABMultiValueGetCount(phoneNumbers)
        for i in 0 ..< phoneCount {
            let phone = (ABMultiValueCopyValueAtIndex(phoneNumbers, i).takeRetainedValue() as! String).trim()
            jPhones.addWithId(phone)
        }
        
        
        // Email
        let jEmails = JavaUtilArrayList()
        let emails: ABMultiValueRef = ABRecordCopyValue(person, kABPersonEmailProperty).takeRetainedValue()
        let emailsCount = ABMultiValueGetCount(emails)
        for i in 0 ..< emailsCount {
            let email = (ABMultiValueCopyValueAtIndex(emails, i).takeRetainedValue() as! String).trim()
            if (email.length > 0) {
                jEmails.addWithId(email)
            }
        }

        // Sending
        
        Actor.sendContactWithPeer(self.peer, withName: name!, withPhones: jPhones, withEmails: jEmails, withPhoto: jAvatarImage)
    }
    
    
    ////////////////////////////////////////////////////////////
    // MARK: -
    // MARK: Audio recording statments + send
    ////////////////////////////////////////////////////////////
    
    func onAudioRecordingStarted() {
        print("onAudioRecordingStarted\n")
        stopAudioRecording()
        
        // stop voice player when start recording
        if (self.voicePlayer?.playbackPosition() != 0.0) {
            self.voicePlayer?.audioPlayerStopAndFinish()
        }
        
        audioRecorder.delegate = self
        audioRecorder.start()
    }
    
    func onAudioRecordingFinished() {
        print("onAudioRecordingFinished\n")
        
        audioRecorder.finish { (path: String!, duration: NSTimeInterval) -> Void in
            
            if (nil == path) {
                print("onAudioRecordingFinished: empty path")
                return
            }
            
            NSLog("onAudioRecordingFinished: %@ [%lfs]", path, duration)
            let range = path.rangeOfString("/tmp", options: NSStringCompareOptions(), range: nil, locale: nil)
            let descriptor = path.substringFromIndex(range!.startIndex)
            NSLog("Audio Recording file: \(descriptor)")

            Actor.sendAudioWithPeer(self.peer, withName: NSString.localizedStringWithFormat("%@.ogg", NSUUID().UUIDString) as String,
                withDuration: jint(duration*1000), withDescriptor: descriptor)
        }
        audioRecorder.cancel()
    }
    
    public func audioRecorderDidStartRecording() {
        self.voiceRecorderView.recordingStarted()
        
    }
    
    func onAudioRecordingCancelled() {
        stopAudioRecording()
    }
    
    func stopAudioRecording() {
        if (audioRecorder != nil) {
            audioRecorder.delegate = nil
            audioRecorder.cancel()
        }
    }
    
    func beginRecord(button:UIButton,event:UIEvent) {
        
        self.voiceRecorderView.startAnimation()
        
        self.voiceRecorderView.hidden = false
        self.stickersButton.hidden = true
        
        let touches : Set<UITouch> = event.touchesForView(button)!
        let touch = touches.first!
        let location = touch.locationInView(button)
        
        self.voiceRecorderView.trackTouchPoint = location
        self.voiceRecorderView.firstTouchPoint = location
        
        
        self.onAudioRecordingStarted()
    }
    
    func mayCancelRecord(button:UIButton,event:UIEvent) {
        
        let touches : Set<UITouch> = event.touchesForView(button)!
        let touch = touches.first!
        let currentLocation = touch.locationInView(button)
        
        if (currentLocation.x < self.rightButton.frame.origin.x) {
            
            if (self.voiceRecorderView.trackTouchPoint.x > currentLocation.x) {
                self.voiceRecorderView.updateLocation(currentLocation.x - self.voiceRecorderView.trackTouchPoint.x,slideToRight: false)
            } else {
                self.voiceRecorderView.updateLocation(currentLocation.x - self.voiceRecorderView.trackTouchPoint.x,slideToRight: true)
            }

        }
        
        self.voiceRecorderView.trackTouchPoint = currentLocation
        
        if ((self.voiceRecorderView.firstTouchPoint.x - self.voiceRecorderView.trackTouchPoint.x) > 120) {
            //cancel
            
            self.voiceRecorderView.hidden = true
            self.stickersButton.hidden = false
            self.stopAudioRecording()
            self.voiceRecorderView.recordingStoped()
            button.cancelTrackingWithEvent(event)
            
            closeRecorderAnimation()
            
        }
        
    }
    
    func closeRecorderAnimation() {
        
        let leftButtonFrame = self.leftButton.frame
        leftButton.frame.origin.x = -100
        
        let textViewFrame = self.textView.frame
        textView.frame.origin.x = textView.frame.origin.x + 500
        
        let stickerViewFrame = self.stickersButton.frame
        stickersButton.frame.origin.x = self.stickersButton.frame.origin.x + 500
        
        UIView.animateWithDuration(1.5, delay: 0.0, usingSpringWithDamping: 0.5, initialSpringVelocity: 1.0, options: UIViewAnimationOptions.CurveLinear, animations: { () -> Void in
            
            self.leftButton.frame = leftButtonFrame
            self.textView.frame = textViewFrame
            self.stickersButton.frame = stickerViewFrame
            
            }, completion: { (complite) -> Void in
                
                // animation complite
                
        })
        
    }
    
    func finishRecord(button:UIButton,event:UIEvent) {
        closeRecorderAnimation()
        self.voiceRecorderView.hidden = true
        self.stickersButton.hidden = false
        self.onAudioRecordingFinished()
        self.voiceRecorderView.recordingStoped()
    }
    
    ////////////////////////////////////////////////////////////
    // MARK: - Stickers actions
    ////////////////////////////////////////////////////////////
    
    func updateStickersStateOnCloseKeyboard() {
        self.stickersOpen = false
        self.stickersButton.setImage(UIImage.bundled("sticker_button"), forState: UIControlState.Normal)
        self.textInputbar.textView.inputView = nil
    }
    
    func changeKeyboard() {
        if self.stickersOpen == false {
            // self.stickersView.loadStickers()
            
            self.textInputbar.textView.inputView = self.stickersView
            self.textInputbar.textView.inputView?.opaque = false
            self.textInputbar.textView.inputView?.backgroundColor = UIColor.clearColor()
            self.textInputbar.textView.refreshFirstResponder()
            self.textInputbar.textView.refreshInputViews()
            self.textInputbar.textView.becomeFirstResponder()
            
            self.stickersButton.setImage(UIImage.bundled("keyboard_button"), forState: UIControlState.Normal)
            
            self.stickersOpen = true
        } else {
            self.textInputbar.textView.inputView = nil
            
            self.textInputbar.textView.refreshFirstResponder()
            self.textInputbar.textView.refreshInputViews()
            self.textInputbar.textView.becomeFirstResponder()
            
            self.stickersButton.setImage(UIImage.bundled("sticker_button"), forState: UIControlState.Normal)
            
            self.stickersOpen = false
        }
        self.textInputbar.layoutIfNeeded()
        self.view.layoutIfNeeded()
    }
    
    public func stickerDidSelected(keyboard: AAStickersKeyboard, sticker: ACSticker) {
        Actor.sendStickerWithPeer(self.peer, withSticker: sticker)
    }
}

class AABarAvatarView : AAAvatarView {
    
//    override init(frameSize: Int, type: AAAvatarType) {
//        super.init(frameSize: frameSize, type: type)
//    }
//    
//    required init(coder aDecoder: NSCoder) {
//        fatalError("init(coder:) has not been implemented")
//    }
    
    override func alignmentRectInsets() -> UIEdgeInsets {
        return UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 8)
    }
}

class AACallButton: UIImageView {
    override init(image: UIImage?) {
        super.init(image: image)
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func alignmentRectInsets() -> UIEdgeInsets {
        return UIEdgeInsets(top: 0, left: -2, bottom: 0, right: 0)
    }
}
