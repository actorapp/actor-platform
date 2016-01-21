//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit
import MobileCoreServices
import AddressBook
import AddressBookUI
import SVProgressHUD

class ConversationViewController: AAConversationContentController, UIDocumentMenuDelegate, UIDocumentPickerDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate, AALocationPickerControllerDelegate,
    ABPeoplePickerNavigationControllerDelegate,AAAudioRecorderDelegate {
    
    // Data binder
    private let binder = AABinder()
    
    // Internal state
    // Members for autocomplete
    var filteredMembers = [ACMentionFilterResult]()
    let content: ACPage!
    var appStyle: ActorStyle { get { return ActorSDK.sharedActor().style } }
    
    // States
    
    private var micOn: Bool! = true
    
    // Views
    private let titleView: UILabel = UILabel()
    private let subtitleView: UILabel = UILabel()
    private let navigationView: UIView = UIView()
    private let avatarView = AABarAvatarView(frameSize: 36, type: .Rounded)
    private let backgroundView = UIImageView()
    private var audioButton: UIButton = UIButton()
    private var actionSheet: AAConvActionSheet!
    private var voiceRecorderView : AAVoiceRecorderView!
    private var voiceButton : UIButton!
    
    // Stickers
    
    private var stickersView: AAStickersView!
    private var stickersButton : UIButton!
    private var stickersOpen = false
    
    var audioRecorder: AAAudioRecorder!
    
    // MARK: - Init
    
    override init(peer: ACPeer) {
        
        // Data
        
        self.content = ACAllEvents_Chat_viewWithACPeer_(peer)
        
        // Create controller
        
        super.init(peer: peer)
        
        // Background
        
        backgroundView.contentMode = .ScaleAspectFill
        backgroundView.clipsToBounds = true
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
        
        // slk settings
        self.bounces = false
        
        
        // Text Input
        
        self.textInputbar.backgroundColor = appStyle.chatInputFieldBgColor
        self.textInputbar.autoHideRightButton = false;
        self.textInputbar.translucent = false
        
        // Text view placeholder
        self.textView.placeholder = AALocalized("ChatPlaceholder")
        
        
        self.rightButton.tintColor = appStyle.chatSendColor
        self.rightButton.setImage(UIImage.tinted("aa_micbutton", color: appStyle.chatAttachColor), forState: UIControlState.Normal)
        self.rightButton.setTitle("", forState: UIControlState.Normal)
        self.rightButton.enabled = true
        self.rightButton.layoutIfNeeded()
        
        self.rightButton.addTarget(self, action: "beginRecord:event:", forControlEvents: UIControlEvents.TouchDown)
        self.rightButton.addTarget(self, action: "mayCancelRecord:event:", forControlEvents: UIControlEvents.TouchDragInside.union(UIControlEvents.TouchDragOutside))
        self.rightButton.addTarget(self, action: "finishRecord:event:", forControlEvents: UIControlEvents.TouchUpInside.union(UIControlEvents.TouchCancel).union(UIControlEvents.TouchUpOutside))
        
        // voice recorder delegate
        self.audioRecorder = AAAudioRecorder()
        self.audioRecorder.delegate = self

        
        //add stickers button
        self.stickersButton = UIButton(type: UIButtonType.System)
        self.stickersButton.tintColor = UIColor.lightGrayColor().colorWithAlphaComponent(0.5)
        self.stickersButton.setImage(UIImage.bundled("sticker_button"), forState: UIControlState.Normal)
        self.stickersButton.frame = CGRectMake(self.view.frame.size.width-67, 12, 20, 20)
        self.stickersButton.addTarget(self, action: "changeKeyboard", forControlEvents: UIControlEvents.TouchUpInside)
        
        self.textInputbar.addSubview(stickersButton)
        
        self.keyboardPanningEnabled = true
        
        self.registerPrefixesForAutoCompletion(["@"])
        
        self.textView.keyboardAppearance = ActorSDK.sharedActor().style.isDarkApp ? .Dark : .Light

        self.leftButton.setImage(UIImage.tinted("conv_attach", color: appStyle.chatAttachColor), forState: UIControlState.Normal)
        
        // Navigation Title
        
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
        
        // Navigation Avatar
        
        avatarView.frame = CGRectMake(0, 0, 36, 36)
        let avatarTapGesture = UITapGestureRecognizer(target: self, action: "onAvatarTap")
        avatarTapGesture.numberOfTapsRequired = 1
        avatarTapGesture.numberOfTouchesRequired = 1
        avatarView.addGestureRecognizer(avatarTapGesture)
        
        let barItem = UIBarButtonItem(customView: avatarView)
        self.navigationItem.rightBarButtonItem = barItem
    }
    
    required init(coder aDecoder: NSCoder!) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Lifecycle
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.voiceRecorderView = AAVoiceRecorderView(frame: CGRectMake(0,0,self.view.frame.size.width-30,44))
        self.voiceRecorderView.hidden = true
        self.voiceRecorderView.binedController = self
        self.textInputbar.addSubview(self.voiceRecorderView)
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
        
        let frame = CGRectMake(0, 0, self.view.frame.size.width, 216)
        self.stickersView = AAStickersView(frame: frame, convContrller: self)
        
        
        NSNotificationCenter.defaultCenter().addObserver(
            self,
            selector: "updateStickersStateOnCloseKeyboard",
            name: SLKKeyboardWillHideNotification,
            object: nil)
        
        
        
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        // Installing bindings
        if (UInt(peer.peerType.ordinal()) == ACPeerType.PRIVATE.rawValue) {
            let user = Actor.getUserWithUid(peer.peerId)
            let nameModel = user.getNameModel();
            
            binder.bind(nameModel, closure: { (value: NSString?) -> () in
                self.titleView.text = String(value!);
                self.navigationView.sizeToFit();
            })
            binder.bind(user.getAvatarModel(), closure: { (value: ACAvatar?) -> () in
                self.avatarView.bind(user.getNameModel().get(), id: user.getId(), avatar: value)
            })
            
            binder.bind(Actor.getTypingWithUid(peer.peerId)!, valueModel2: user.getPresenceModel()!, closure:{ (typing:JavaLangBoolean?, presence:ACUserPresence?) -> () in
                
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
                        let state = UInt(presence!.state.ordinal())
                        if (state == ACUserPresence_State.ONLINE.rawValue) {
                            self.subtitleView.textColor = self.appStyle.userOnlineNavigationColor
                        } else {
                            self.subtitleView.textColor = self.appStyle.userOfflineNavigationColor
                        }
                    }
                }
            })
        } else if (UInt(peer.peerType.ordinal()) == ACPeerType.GROUP.rawValue) {
            let group = Actor.getGroupWithGid(peer.peerId)
            let nameModel = group.getNameModel()
            
            binder.bind(nameModel, closure: { (value: NSString?) -> () in
                self.titleView.text = String(value!);
                self.navigationView.sizeToFit();
            })
            binder.bind(group.getAvatarModel(), closure: { (value: ACAvatar?) -> () in
                self.avatarView.bind(group.getNameModel().get(), id: group.getId(), avatar: value)
            })
            binder.bind(Actor.getGroupTypingWithGid(group.getId())!, valueModel2: group.getMembersModel(), valueModel3: group.getPresenceModel(), closure: { (typingValue:IOSIntArray?, members:JavaUtilHashSet?, onlineCount:JavaLangInteger?) -> () in
                if (!group.isMemberModel().get().booleanValue()) {
                    self.subtitleView.text = AALocalized("ChatNoGroupAccess")
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
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        backgroundView.frame = CGRect(x: 0, y: 0, width: view.bounds.width, height: view.bounds.height)
        
        titleView.frame = CGRectMake(0, 4, (navigationView.frame.width - 0), 20)
        subtitleView.frame = CGRectMake(0, 22, (navigationView.frame.width - 0), 20)
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        //self.audioButton.frame = textView.frame
        
        if navigationController!.viewControllers.count > 2 {
            let firstController = navigationController!.viewControllers[0]
            let currentController = navigationController!.viewControllers[navigationController!.viewControllers.count - 1]
            navigationController!.setViewControllers([firstController, currentController], animated: false)
        }
        
        if !AADevice.isiPad {
            AANavigationBadge.showBadge()
        }
        
        // action sheet
        
        self.actionSheet = AAConvActionSheet(maxSelected: 9, weakSuperIn: self)
        
        self.navigationController?.view.addSubview(self.actionSheet)
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        Actor.onConversationClosedWithPeer(peer)
        ActorSDK.sharedActor().trackPageHidden(content)

        if !AADevice.isiPad {
            AANavigationBadge.hideBadge()
        }
        
        // Closing keyboard
        self.textView.resignFirstResponder()
    }

    override func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated)
        Actor.saveDraftWithPeer(peer, withDraft: textView.text)
        
        // Releasing bindings
        binder.unbindAll()
    
        self.actionSheet.removeFromSuperview()
        self.actionSheet = nil
    }

    // Chat avatar tap
    
    func onAvatarTap() {
        let id = Int(peer.peerId)
        var controller: AAViewController!
        if (UInt(peer.peerType.ordinal()) == ACPeerType.PRIVATE.rawValue) {
            controller = ActorSDK.sharedActor().delegate.actorControllerForUser(id)
            if controller == nil {
                controller = AAUserViewController(uid: id)
            }
        } else if (UInt(peer.peerType.ordinal()) == ACPeerType.GROUP.rawValue) {
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
    
    // MARK: - Text bar actions
    
    override func textWillUpdate() {
        super.textWillUpdate();

        Actor.onTypingWithPeer(peer);
    }
    
    
    
    override func textDidUpdate(animated: Bool) {
        super.textDidUpdate(animated)
        
        let text = self.textView.text.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet())
        self.rightButton.enabled = true
        
        //change button
        
        
        if !text.isEmpty {
            
            self.rightButton.removeTarget(self, action: "beginRecord:event:", forControlEvents: UIControlEvents.TouchDown)
            self.rightButton.removeTarget(self, action: "mayCancelRecord:event:", forControlEvents: UIControlEvents.TouchDragInside.union(UIControlEvents.TouchDragOutside))
            self.rightButton.removeTarget(self, action: "finishRecord:event:", forControlEvents: UIControlEvents.TouchUpInside.union(UIControlEvents.TouchCancel).union(UIControlEvents.TouchUpOutside))
            
            self.rebindRightButton()
            
            self.stickersButton.hidden = true
            
            self.rightButton.setTitle(AALocalized("ChatSend"), forState: UIControlState.Normal)
            self.rightButton.setTitleColor(appStyle.chatSendColor, forState: UIControlState.Normal)
            self.rightButton.setTitleColor(appStyle.chatSendDisabledColor, forState: UIControlState.Disabled)
            self.rightButton.setImage(nil, forState: UIControlState.Normal)
            
            if self.micOn == true {
                self.micOn = false
            }
            
        } else {
            
            self.rightButton.addTarget(self, action: "beginRecord:event:", forControlEvents: UIControlEvents.TouchDown)
            self.rightButton.addTarget(self, action: "mayCancelRecord:event:", forControlEvents: UIControlEvents.TouchDragInside.union(UIControlEvents.TouchDragOutside))
            self.rightButton.addTarget(self, action: "finishRecord:event:", forControlEvents: UIControlEvents.TouchUpInside.union(UIControlEvents.TouchCancel).union(UIControlEvents.TouchUpOutside))
            
            self.stickersButton.hidden = false
            if(self.micOn == false){
                
                self.rightButton.tintColor = appStyle.chatAttachColor
                self.rightButton.setImage(UIImage.bundled("aa_micbutton"), forState: UIControlState.Normal)
                self.rightButton.setTitle("", forState: UIControlState.Normal)
                self.rightButton.enabled = true
                
            }
            
        }
        
        self.textInputbar.layoutIfNeeded()
        self.rightButton.layoutIfNeeded()
        
    }
    
    // MARK: - right button
    
    override func didPressRightButton(sender: AnyObject!) {
        
        if !self.textView.text.isEmpty {
            
            Actor.sendMessageWithMentionsDetect(peer, withText: textView.text)
            super.didPressRightButton(sender)
            
        }

    }
    
    override func didPressLeftButton(sender: AnyObject!) {
        super.didPressLeftButton(sender)
        
        self.textInputbar.textView.resignFirstResponder()
        
        self.actionSheet.showAnimation()
        
        self.rightButton.layoutIfNeeded()
    }
 
    //MARK: - Completition
    
    
    override func didChangeAutoCompletionPrefix(prefix: String!, andWord word: String!) {
        if UInt(self.peer.peerType.ordinal()) == ACPeerType.GROUP.rawValue {
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
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return filteredMembers.count
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let res = AAAutoCompleteCell(style: UITableViewCellStyle.Default, reuseIdentifier: "user_name")
        res.bindData(filteredMembers[indexPath.row], highlightWord: foundWord)
        return res
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let user = filteredMembers[indexPath.row]

        var postfix = " "
        if foundPrefixRange.location == 0 {
            postfix = ": "
        }
        
        acceptAutoCompletionWithString(user.mentionString + postfix, keepPrefix: !user.isNickname)
    }
    
    override func heightForAutoCompletionView() -> CGFloat {
        let cellHeight: CGFloat = 44.0;
        return cellHeight * CGFloat(filteredMembers.count)
    }
    
    override func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        cell.separatorInset = UIEdgeInsetsZero
        cell.preservesSuperviewLayoutMargins = false
        cell.layoutMargins = UIEdgeInsetsZero
    }
    
    // Document picking
    
    func pickDocument() {
        let documentPicker = UIDocumentMenuViewController(documentTypes: UTTAll as! [String], inMode: UIDocumentPickerMode.Import)
        documentPicker.view.backgroundColor = UIColor.clearColor()
        documentPicker.delegate = self
        self.presentViewController(documentPicker, animated: true, completion: nil)
    }
    
    func documentMenu(documentMenu: UIDocumentMenuViewController, didPickDocumentPicker documentPicker: UIDocumentPickerViewController) {
        documentPicker.delegate = self
        self.presentViewController(documentPicker, animated: true, completion: nil)
    }
    
    func documentPicker(controller: UIDocumentPickerViewController, didPickDocumentAtURL url: NSURL) {
        
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
    
    // Image picking
    
    func pickImage(source: UIImagePickerControllerSourceType) {
        let pickerController = AAImagePickerController()
        pickerController.sourceType = source
        pickerController.mediaTypes = [kUTTypeImage as String,kUTTypeMovie as String]
        
        //[kUTTypeImage as String,kUTTypeMovie as String]

//        // Style controller bg
//        pickerController.view.backgroundColor = appStyle.vcBgColor
//        
//        // Style navigation bar
//        pickerController.navigationBar.barTintColor = appStyle.navigationBgColor
//        pickerController.navigationBar.tintColor = appStyle.navigationTintColor
//        pickerController.navigationBar.titleTextAttributes = [NSForegroundColorAttributeName: appStyle.navigationTitleColor]

        pickerController.delegate = self

        self.presentViewController(pickerController, animated: true, completion: nil)
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage, editingInfo: [String : AnyObject]?) {
        picker.dismissViewControllerAnimated(true, completion: nil)
        Actor.sendUIImage(image, peer: peer)
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject]) {
        picker.dismissViewControllerAnimated(true, completion: nil)
        
        if let image = info[UIImagePickerControllerOriginalImage] as? UIImage {
            Actor.sendUIImage(image, peer: peer)
            
        } else {
            Actor.sendVideo(info[UIImagePickerControllerMediaURL] as! NSURL, peer: peer)
        }
        
    }
    
    func imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion: nil)
    }
    
    func sendImageFromActionSheet(image:UIImage) {
        Actor.sendUIImage(image, peer: peer)
    }
    
    // Location picking
    
    func pickLocation() {
        let pickerController = AALocationPickerController()
        pickerController.delegate = self
        self.presentViewController(AANavigationController(rootViewController:pickerController), animated: true, completion: nil)
    }
    
    func locationPickerDidCancelled(controller: AALocationPickerController) {
        controller.dismissViewControllerAnimated(true, completion: nil)
    }
    
    func locationPickerDidPicked(controller: AALocationPickerController, latitude: Double, longitude: Double) {
        Actor.sendLocationWithPeer(self.peer, withLongitude: JavaLangDouble(double: longitude), withLatitude: JavaLangDouble(double: latitude), withStreet: nil, withPlace: nil)
        controller.dismissViewControllerAnimated(true, completion: nil)
    }
    
    func pickContact() {
        let pickerController = ABPeoplePickerNavigationController()
        pickerController.peoplePickerDelegate = self
        self.presentViewController(pickerController, animated: true, completion: nil)
    }
    
    func peoplePickerNavigationController(peoplePicker: ABPeoplePickerNavigationController, didSelectPerson person: ABRecord) {
        
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
        for (var i = 0;i < phoneCount ; i++) {
            let phone = (ABMultiValueCopyValueAtIndex(phoneNumbers, i).takeRetainedValue() as! String).trim()
            jPhones.addWithId(phone)
        }
        
        
        // Email
        let jEmails = JavaUtilArrayList()
        let emails: ABMultiValueRef = ABRecordCopyValue(person, kABPersonEmailProperty).takeRetainedValue()
        let emailsCount = ABMultiValueGetCount(emails)
        for (var i = 0; i < emailsCount; i++) {
            let email = (ABMultiValueCopyValueAtIndex(emails, i).takeRetainedValue() as! String).trim()
            if (email.length > 0) {
                jEmails.addWithId(email)
            }
        }

        // Sending
        
        Actor.sendContactWithPeer(self.peer, withName: name, withPhones: jPhones, withEmails: jEmails, withPhoto: jAvatarImage)
    }
    
    
    // send audio document
    
    // MARK: -
    // MARK: Audio recording callbacks
    
    func onAudioRecordingStarted() {
        print("onAudioRecordingStarted\n")
        stopAudioRecording()
        audioRecorder.delegate = self
        audioRecorder.start()
        
    }
    
    func onAudioRecordingFinished() {
        print("onAudioRecordingFinished\n")
        audioRecorder.finish({ (path: String!, duration: NSTimeInterval) -> Void in
            
            if (nil == path) {
                print("onAudioRecordingFinished: empty path")
                return
            }
            
            NSLog("onAudioRecordingFinished: %@ [%lfs]", path, duration)
            let range = path.rangeOfString("/tmp", options: NSStringCompareOptions(), range: nil, locale: nil)
            let descriptor = path.substringFromIndex(range!.startIndex)
            NSLog("Audio Recording file: \(descriptor)")
            

            Actor.sendAudioWithPeer(self.peer, withName: NSString.localizedStringWithFormat("%.0fs.ogg", duration + 0.5) as String,
                withDuration: jint(duration), withDescriptor: descriptor)
            
        })
    }
    
    func audioRecorderDidStartRecording() {
        self.voiceRecorderView.recordingStarted()
        
    }
    
    func onAudioRecordingCancelled() {
        print("onAudioRecordingCancelled\n")
        stopAudioRecording()
    }
    
    func stopAudioRecording()
    {
        if (audioRecorder != nil)
        {
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
        
        if ((self.voiceRecorderView.firstTouchPoint.x - self.voiceRecorderView.trackTouchPoint.x) > 130) {
            //cancel
            
            
            let leftButtonFrame = self.leftButton.frame
            leftButton.frame.origin.x = -100
            
            let textViewFrame = self.textView.frame
            textView.frame.origin.x = textView.frame.origin.x + 500
            
            let stickerViewFrame = self.stickersButton.frame
            stickersButton.frame.origin.x = self.stickersButton.frame.origin.x + 500
            
            self.voiceRecorderView.hidden = true
            self.stickersButton.hidden = false
            self.stopAudioRecording()
            self.voiceRecorderView.recordingStoped()
            button.cancelTrackingWithEvent(event)
            
            
            UIView.animateWithDuration(0.3, delay: 0.0, options: UIViewAnimationOptions.CurveLinear, animations: { () -> Void in
                
                self.leftButton.frame = leftButtonFrame
                self.textView.frame = textViewFrame
                self.stickersButton.frame = stickerViewFrame

                }) { (complite) -> Void in

            }
            
        }
        
    }
    
    func finishRecord(button:UIButton,event:UIEvent) {
        self.voiceRecorderView.hidden = true
        self.stickersButton.hidden = false
        self.onAudioRecordingFinished()
        self.voiceRecorderView.recordingStoped()
        
    }
    
    // MARK: - Stickers actions
    
    func updateStickersStateOnCloseKeyboard() {
        
        self.stickersOpen = false
        self.stickersButton.setImage(UIImage.bundled("sticker_button"), forState: UIControlState.Normal)
        self.textInputbar.textView.inputView = nil
        
    }
    
    func changeKeyboard() {
        
        
        if self.stickersOpen == false {
            self.stickersView.loadStickers()
            
            self.textInputbar.textView.inputView = self.stickersView
            self.textInputbar.textView.inputView?.opaque = false
            self.textInputbar.textView.inputView?.backgroundColor = UIColor.clearColor()
            self.textInputbar.textView.refreshFirstResponder()
            self.textInputbar.textView.refreshInputViews()
            self.textInputbar.textView.becomeFirstResponder()
            
            
            self.stickersButton.setImage(UIImage.bundled("keyboard_button"), forState: UIControlState.Normal)
            
            self.stickersOpen = true
            
            self.textInputbar.layoutIfNeeded()
            self.view.layoutIfNeeded()
            
            
        } else {
            
            self.textInputbar.textView.inputView = nil
            
            self.textInputbar.textView.refreshFirstResponder()
            self.textInputbar.textView.refreshInputViews()
            self.textInputbar.textView.becomeFirstResponder()
            
            self.stickersButton.setImage(UIImage.bundled("sticker_button"), forState: UIControlState.Normal)
            
            self.stickersOpen = false
            
            self.textInputbar.layoutIfNeeded()
            self.view.layoutIfNeeded()
            
        }
        
    }
    
    func sendSticker(sticker:ACSticker) {

        Actor.sendStickerWithPeer(self.peer, withSticker: sticker)
        
    }
    
    deinit {
        print("conversation controller deinit'ed")
    }

    
}
