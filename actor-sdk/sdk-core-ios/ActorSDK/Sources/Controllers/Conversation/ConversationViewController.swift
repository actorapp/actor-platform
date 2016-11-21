//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit
import MobileCoreServices
import AddressBook
import AddressBookUI

open class ConversationViewController:
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
    fileprivate let binder = AABinder()
    
    // Internal state
    // Members for autocomplete
    var filteredMembers = [ACMentionFilterResult]()
    let content: ACPage!
    var appStyle: ActorStyle { get { return ActorSDK.sharedActor().style } }
    
    
    //
    // Views
    //
    
    fileprivate let titleView: UILabel = UILabel()
    fileprivate let subtitleView: UILabel = UILabel()
    fileprivate let navigationView: UIView = UIView()
    fileprivate let avatarView = AABarAvatarView()
    fileprivate let backgroundView = UIImageView()
    fileprivate var audioButton: UIButton = UIButton()
    fileprivate var voiceRecorderView : AAVoiceRecorderView!
    fileprivate let inputOverlay = UIView()
    fileprivate let inputOverlayLabel = UILabel()
    
    //
    // Stickers
    //
    
    fileprivate var stickersView: AAStickersKeyboard!
    fileprivate var stickersButton : UIButton!
    fileprivate var stickersOpen = false
    
    
    //
    // Audio Recorder
    //
    
    open var audioRecorder: AAAudioRecorder!
    
    
    //
    // Mode
    //
    
    fileprivate var textMode:Bool!
    fileprivate var micOn: Bool! = true
    

    
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
        backgroundView.contentMode = .scaleAspectFill
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
        view.insertSubview(backgroundView, at: 0)
        
        
        //
        // slk settings
        //
        self.bounces = false
        self.isKeyboardPanningEnabled = true
        self.registerPrefixes(forAutoCompletion: ["@"])
        
        
        //
        // Text Input
        //
        self.textInputbar.backgroundColor = appStyle.chatInputFieldBgColor
        self.textInputbar.autoHideRightButton = false;
        self.textInputbar.isTranslucent = false
        
        
        //
        // Text view
        //
        self.textView.placeholder = AALocalized("ChatPlaceholder")
        self.textView.keyboardAppearance = ActorSDK.sharedActor().style.isDarkApp ? .dark : .light
        
        
        //
        // Overlay
        //
        self.inputOverlay.addSubview(inputOverlayLabel)
        self.inputOverlayLabel.textAlignment = .center
        self.inputOverlayLabel.font = UIFont.systemFont(ofSize: 18)
        self.inputOverlayLabel.textColor = ActorSDK.sharedActor().style.vcTintColor
        self.inputOverlay.viewDidTap = {
            self.onOverlayTap()
        }
        
        //
        // Add stickers button
        //
        self.stickersButton = UIButton(type: UIButtonType.system)
        self.stickersButton.tintColor = UIColor.lightGray.withAlphaComponent(0.5)
        self.stickersButton.setImage(UIImage.bundled("sticker_button"), for: UIControlState())
        self.stickersButton.addTarget(self, action: #selector(ConversationViewController.changeKeyboard), for: UIControlEvents.touchUpInside)
        self.textInputbar.addSubview(stickersButton)
        
        
        //
        // Check text for set right button
        //
        let checkText = Actor.loadDraft(with: peer)!.trimmingCharacters(in: CharacterSet.whitespacesAndNewlines)
        
        if (checkText.isEmpty) {
            
            self.textMode = false
            
            self.rightButton.tintColor = appStyle.chatSendColor
            self.rightButton.setImage(UIImage.tinted("aa_micbutton", color: appStyle.chatAttachColor), for: UIControlState())
            self.rightButton.setTitle("", for: UIControlState())
            self.rightButton.isEnabled = true
            
            self.rightButton.layoutIfNeeded()
            
            self.rightButton.addTarget(self, action: #selector(ConversationViewController.beginRecord(_:event:)), for: UIControlEvents.touchDown)
            self.rightButton.addTarget(self, action: #selector(ConversationViewController.mayCancelRecord(_:event:)), for: UIControlEvents.touchDragInside.union(UIControlEvents.touchDragOutside))
            self.rightButton.addTarget(self, action: #selector(ConversationViewController.finishRecord(_:event:)), for: UIControlEvents.touchUpInside.union(UIControlEvents.touchCancel).union(UIControlEvents.touchUpOutside))
            
        } else {
            
            self.textMode = true
            
            self.stickersButton.isHidden = true
            
            self.rightButton.setTitle(AALocalized("ChatSend"), for: UIControlState())
            self.rightButton.setTitleColor(appStyle.chatSendColor, for: UIControlState())
            self.rightButton.setTitleColor(appStyle.chatSendDisabledColor, for: UIControlState.disabled)
            self.rightButton.setImage(nil, for: UIControlState())
            self.rightButton.isEnabled = true
            
            self.rightButton.layoutIfNeeded()
        }
        
        
        //
        // Voice Recorder
        //
        self.audioRecorder = AAAudioRecorder()
        self.audioRecorder.delegate = self
        
        self.leftButton.setImage(UIImage.tinted("conv_attach", color: appStyle.chatAttachColor), for: UIControlState())
        
        
        //
        // Navigation Title
        //
        
        navigationView.frame = CGRect(x: 0, y: 0, width: 200, height: 44)
        navigationView.autoresizingMask = UIViewAutoresizing.flexibleWidth
        
        titleView.font = UIFont.mediumSystemFontOfSize(17)
        titleView.adjustsFontSizeToFitWidth = false
        titleView.textAlignment = NSTextAlignment.center
        titleView.lineBreakMode = NSLineBreakMode.byTruncatingTail
        titleView.autoresizingMask = UIViewAutoresizing.flexibleWidth
        titleView.textColor = appStyle.navigationTitleColor
        
        subtitleView.font = UIFont.systemFont(ofSize: 13)
        subtitleView.adjustsFontSizeToFitWidth = true
        subtitleView.textAlignment = NSTextAlignment.center
        subtitleView.lineBreakMode = NSLineBreakMode.byTruncatingTail
        subtitleView.autoresizingMask = UIViewAutoresizing.flexibleWidth
        
        navigationView.addSubview(titleView)
        navigationView.addSubview(subtitleView)
        
        self.navigationItem.titleView = navigationView
        
        
        //
        // Navigation Avatar
        //
        avatarView.frame = CGRect(x: 0, y: 0, width: 40, height: 40)
        avatarView.viewDidTap = onAvatarTap
        
        let barItem = UIBarButtonItem(customView: avatarView)
        let isBot: Bool
        if (peer.isPrivate) {
            isBot = Bool(Actor.getUserWithUid(peer.peerId).isBot())
        } else {
            isBot = false
        }
        if (ActorSDK.sharedActor().enableCalls && !isBot && peer.isPrivate) {
            if ActorSDK.sharedActor().enableVideoCalls {
                let callButtonView = AACallButton(image: UIImage.bundled("ic_call_outline_22")?.tintImage(ActorSDK.sharedActor().style.navigationTintColor))
                callButtonView.viewDidTap = onCallTap
                let callButtonItem = UIBarButtonItem(customView: callButtonView)
                
                let videoCallButtonView = AACallButton(image: UIImage.bundled("ic_video_outline_22")?.tintImage(ActorSDK.sharedActor().style.navigationTintColor))
                videoCallButtonView.viewDidTap = onVideoCallTap
                let callVideoButtonItem = UIBarButtonItem(customView: videoCallButtonView)
                
                self.navigationItem.rightBarButtonItems = [barItem, callVideoButtonItem, callButtonItem]
            } else {
                let callButtonView = AACallButton(image: UIImage.bundled("ic_call_outline_22")?.tintImage(ActorSDK.sharedActor().style.navigationTintColor))
                callButtonView.viewDidTap = onCallTap
                let callButtonItem = UIBarButtonItem(customView: callButtonView)
                self.navigationItem.rightBarButtonItems = [barItem, callButtonItem]
            }
        } else {
            self.navigationItem.rightBarButtonItems = [barItem]
        }
    }
    
    required public init(coder aDecoder: NSCoder!) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override open func viewDidLoad() {
        super.viewDidLoad()
        
        self.voiceRecorderView = AAVoiceRecorderView(frame: CGRect(x: 0, y: 0, width: view.width - 30, height: 44))
        self.voiceRecorderView.isHidden = true
        self.voiceRecorderView.binedController = self
        self.textInputbar.addSubview(self.voiceRecorderView)
        
        self.inputOverlay.backgroundColor = UIColor.white
        self.inputOverlay.isHidden = false
        self.textInputbar.addSubview(self.inputOverlay)
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: UIBarButtonItemStyle.plain, target: nil, action: nil)
        
        let frame = CGRect(x: 0, y: 0, width: self.view.frame.size.width, height: 216)
        self.stickersView = AAStickersKeyboard(frame: frame)
        self.stickersView.delegate = self
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(ConversationViewController.updateStickersStateOnCloseKeyboard),
            name: NSNotification.Name.SLKKeyboardWillHide,
            object: nil)
    }
    
    open override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        self.stickersButton.frame = CGRect(x: self.view.frame.size.width-67, y: 12, width: 20, height: 20)
        self.voiceRecorderView.frame = CGRect(x: 0, y: 0, width: view.width - 30, height: 44)
        self.inputOverlay.frame = CGRect(x: 0, y: 0, width: view.width, height: 44)
        self.inputOverlayLabel.frame = CGRect(x: 0, y: 0, width: view.width, height: 44)
    }
    
    ////////////////////////////////////////////////////////////
    // MARK: - Lifecycle
    ////////////////////////////////////////////////////////////
    
    override open func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        // Installing bindings
        if (peer.peerType.ordinal() == ACPeerType.private().ordinal()) {

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
                    if (user.isBot()) {
                        self.subtitleView.text = "bot"
                        self.subtitleView.textColor = self.appStyle.userOnlineNavigationColor
                    } else {
                        let stateText = Actor.getFormatter().formatPresence(presence, with: user.getSex())
                        self.subtitleView.text = stateText;
                        let state = presence!.state.ordinal()
                        if (state == ACUserPresence_State.online().ordinal()) {
                            self.subtitleView.textColor = self.appStyle.userOnlineNavigationColor
                        } else {
                            self.subtitleView.textColor = self.appStyle.userOfflineNavigationColor
                        }
                    }
                }
            })
            
            self.inputOverlay.isHidden = true
        } else if (peer.peerType.ordinal() == ACPeerType.group().ordinal()) {
            let group = Actor.getGroupWithGid(peer.peerId)
            let nameModel = group.getNameModel()
            
            binder.bind(nameModel, closure: { (value: NSString?) -> () in
                self.titleView.text = String(value!);
                self.navigationView.sizeToFit();
            })
            binder.bind(group.getAvatarModel(), closure: { (value: ACAvatar?) -> () in
                self.avatarView.bind(group.getNameModel().get(), id: Int(group.getId()), avatar: value)
            })
            binder.bind(Actor.getGroupTyping(withGid: group.getId()), valueModel2: group.membersCount, valueModel3: group.getPresenceModel(), closure: { (typingValue:IOSIntArray?, membersCount: JavaLangInteger?, onlineCount:JavaLangInteger?) -> () in
                if (!group.isMemberModel().get().booleanValue()) {
                    self.subtitleView.text = AALocalized("ChatNoGroupAccess")
                    self.subtitleView.textColor = self.appStyle.navigationSubtitleColor
                    return
                }
                
                if (typingValue != nil && typingValue!.length() > 0) {
                    self.subtitleView.textColor = self.appStyle.navigationSubtitleActiveColor
                    if (typingValue!.length() == 1) {
                        let uid = typingValue!.int(at: 0);
                        let user = Actor.getUserWithUid(uid)
                        self.subtitleView.text = Actor.getFormatter().formatTyping(withName: user.getNameModel().get())
                    } else {
                        self.subtitleView.text = Actor.getFormatter().formatTyping(withCount: typingValue!.length());
                    }
                } else {
                    var membersString = Actor.getFormatter().formatGroupMembers(membersCount!.intValue())
                    self.subtitleView.textColor = self.appStyle.navigationSubtitleColor
                    if (onlineCount == nil || onlineCount!.intValue == 0) {
                        self.subtitleView.text = membersString;
                    } else {
                        membersString = membersString! + ", ";
                        let onlineString = Actor.getFormatter().formatGroupOnline(onlineCount!.intValue());
                        let attributedString = NSMutableAttributedString(string: (membersString! + onlineString!))
                        attributedString.addAttribute(NSForegroundColorAttributeName, value: self.appStyle.userOnlineNavigationColor, range: NSMakeRange(membersString!.length, onlineString!.length))
                        self.subtitleView.attributedText = attributedString
                    }
                }
            })
            
            binder.bind(group.isMember, valueModel2: group.isCanWriteMessage, valueModel3: group.isCanJoin, closure: { (isMember: JavaLangBoolean?, canWriteMessage: JavaLangBoolean?, canJoin: JavaLangBoolean?) in
                
                if canWriteMessage!.booleanValue() {
                    self.stickersButton.isHidden = false
                    self.inputOverlay.isHidden = true
                } else {
                    if !isMember!.booleanValue() {
                        if canJoin!.booleanValue() {
                            self.inputOverlayLabel.text = AALocalized("ChatJoin")
                        } else {
                            self.inputOverlayLabel.text = AALocalized("ChatNoGroupAccess")
                        }
                    } else {
                        if Actor.isNotificationsEnabled(with: self.peer) {
                            self.inputOverlayLabel.text = AALocalized("ActionMute")
                        } else {
                            self.inputOverlayLabel.text = AALocalized("ActionUnmute")
                        }
                    }
                    self.stickersButton.isHidden = true
                    self.stopAudioRecording()
                    self.textInputbar.textView.text = ""
                    self.inputOverlay.isHidden = false
                }
            })
            
            
            binder.bind(group.isDeleted) { (isDeleted: JavaLangBoolean?) in
                if isDeleted!.booleanValue() {
                    self.alertUser(AALocalized("ChatDeleted")) {
                        self.execute(Actor.deleteChatCommand(with: self.peer), successBlock: { (r) in
                            self.navigateBack()
                        })
                    }
                }
            }
        }
        
        Actor.onConversationOpen(with: peer)
        ActorSDK.sharedActor().trackPageVisible(content)
        
        
        if textView.isFirstResponder == false {
            textView.resignFirstResponder()
        }
        
        textView.text = Actor.loadDraft(with: peer)
        
    }
    
    open func onOverlayTap() {
        if peer.isGroup {
            let group = Actor.getGroupWithGid(peer.peerId)
            if !group.isMember.get().booleanValue() {
                if group.isCanJoin.get().booleanValue() {
                    executePromise(Actor.joinGroup(withGid: peer.peerId))
                } else {
                    // DO NOTHING
                }
            } else if !group.isCanWriteMessage.get().booleanValue() {
                if Actor.isNotificationsEnabled(with: peer) {
                    Actor.changeNotificationsEnabled(with: peer, withValue: false)
                    inputOverlayLabel.text = AALocalized("ActionUnmute")
                } else {
                    Actor.changeNotificationsEnabled(with: peer, withValue: true)
                    inputOverlayLabel.text = AALocalized("ActionMute")
                }
            }
        } else if peer.isPrivate {
            
        }
    }
    
    override open func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        backgroundView.frame = view.bounds
        
        titleView.frame = CGRect(x: 0, y: 4, width: (navigationView.frame.width - 0), height: 20)
        subtitleView.frame = CGRect(x: 0, y: 22, width: (navigationView.frame.width - 0), height: 20)
        
        stickersView.frame = CGRect(x: 0, y: 0, width: self.view.frame.size.width, height: 216)
    }
    
    override open func viewDidAppear(_ animated: Bool) {
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
    
    override open func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        Actor.onConversationClosed(with: peer)
        ActorSDK.sharedActor().trackPageHidden(content)

        if !AADevice.isiPad {
            AANavigationBadge.hideBadge()
        }
        
        // Closing keyboard
        self.textView.resignFirstResponder()
    }

    override open func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        
        Actor.saveDraft(with: peer, withDraft: textView.text)
        
        // Releasing bindings
        binder.unbindAll()
    }

    ////////////////////////////////////////////////////////////
    // MARK: - Chat avatar tap
    ////////////////////////////////////////////////////////////
    
    func onAvatarTap() {
        let id = Int(peer.peerId)
        var controller: AAViewController!
        if (peer.peerType.ordinal() == ACPeerType.private().ordinal()) {
            controller = ActorSDK.sharedActor().delegate.actorControllerForUser(id)
            if controller == nil {
                controller = AAUserViewController(uid: id)
            }
        } else if (peer.peerType.ordinal() == ACPeerType.group().ordinal()) {
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
            popover.present(from: navigationItem.rightBarButtonItem!,
                permittedArrowDirections: UIPopoverArrowDirection.up,
                animated: true)
        } else {
            navigateNext(controller, removeCurrent: false)
        }
    }
    
    func onCallTap() {
        if (self.peer.isGroup) {
            execute(ActorSDK.sharedActor().messenger.doCall(withGid: self.peer.peerId))
        } else if (self.peer.isPrivate) {
            execute(ActorSDK.sharedActor().messenger.doCall(withUid: self.peer.peerId))
        }
    }
    
    func onVideoCallTap() {
        if (self.peer.isPrivate) {
            execute(ActorSDK.sharedActor().messenger.doVideoCall(withUid: self.peer.peerId))
        }
    }
    
    ////////////////////////////////////////////////////////////
    // MARK: - Text bar actions
    ////////////////////////////////////////////////////////////
    
    override open func textDidUpdate(_ animated: Bool) {
        super.textDidUpdate(animated)
        Actor.onTyping(with: peer)
        checkTextInTextView()
    }
    
    func checkTextInTextView() {
        
        let text = self.textView.text.trimmingCharacters(in: CharacterSet.whitespacesAndNewlines)
        self.rightButton.isEnabled = true
        
        //change button's
        
        if !text.isEmpty && textMode == false {
            
            self.rightButton.removeTarget(self, action: #selector(ConversationViewController.beginRecord(_:event:)), for: UIControlEvents.touchDown)
            self.rightButton.removeTarget(self, action: #selector(ConversationViewController.mayCancelRecord(_:event:)), for: UIControlEvents.touchDragInside.union(UIControlEvents.touchDragOutside))
            self.rightButton.removeTarget(self, action: #selector(ConversationViewController.finishRecord(_:event:)), for: UIControlEvents.touchUpInside.union(UIControlEvents.touchCancel).union(UIControlEvents.touchUpOutside))
            
            self.rebindRightButton()
            
            self.stickersButton.isHidden = true
            
            self.rightButton.setTitle(AALocalized("ChatSend"), for: UIControlState())
            self.rightButton.setTitleColor(appStyle.chatSendColor, for: UIControlState())
            self.rightButton.setTitleColor(appStyle.chatSendDisabledColor, for: UIControlState.disabled)
            self.rightButton.setImage(nil, for: UIControlState())
            
            self.rightButton.layoutIfNeeded()
            self.textInputbar.layoutIfNeeded()
            
            self.textMode = true
            
        } else if (text.isEmpty && textMode == true) {
            
            self.rightButton.addTarget(self, action: #selector(ConversationViewController.beginRecord(_:event:)), for: UIControlEvents.touchDown)
            self.rightButton.addTarget(self, action: #selector(ConversationViewController.mayCancelRecord(_:event:)), for: UIControlEvents.touchDragInside.union(UIControlEvents.touchDragOutside))
            self.rightButton.addTarget(self, action: #selector(ConversationViewController.finishRecord(_:event:)), for: UIControlEvents.touchUpInside.union(UIControlEvents.touchCancel).union(UIControlEvents.touchUpOutside))
            
            self.stickersButton.isHidden = false
            
                
            self.rightButton.tintColor = appStyle.chatAttachColor
            self.rightButton.setImage(UIImage.bundled("aa_micbutton"), for: UIControlState())
            self.rightButton.setTitle("", for: UIControlState())
            self.rightButton.isEnabled = true
            
            
            self.rightButton.layoutIfNeeded()
            self.textInputbar.layoutIfNeeded()
            
            self.textMode = false
            
        }
        
    }
    
    ////////////////////////////////////////////////////////////
    // MARK: - Right/Left button pressed
    ////////////////////////////////////////////////////////////
    
    override open func didPressRightButton(_ sender: Any!) {
        if !self.textView.text.isEmpty {
            Actor.sendMessage(withMentionsDetect: peer, withText: textView.text)
            super.didPressRightButton(sender)
        }
    }
    
    override open func didPressLeftButton(_ sender: Any!) {
        super.didPressLeftButton(sender)
        
        self.textInputbar.textView.resignFirstResponder()
        
        self.rightButton.layoutIfNeeded()
        
        if !ActorSDK.sharedActor().delegate.actorConversationCustomAttachMenu(self) {
            let actionSheet = AAConvActionSheet()
            actionSheet.addCustomButton("SendDocument")
            actionSheet.addCustomButton("ShareLocation")
            actionSheet.addCustomButton("ShareContact")
            actionSheet.delegate = self
            actionSheet.presentInController(self)
        }
    }
 
    ////////////////////////////////////////////////////////////
    // MARK: - Completition
    ////////////////////////////////////////////////////////////
    
    override open func didChangeAutoCompletionPrefix(_ prefix: String!, andWord word: String!) {
        if self.peer.peerType.ordinal() == ACPeerType.group().ordinal() {
            if prefix == "@" {
                
                let oldCount = filteredMembers.count
                filteredMembers.removeAll(keepingCapacity: true)
                
                let res = Actor.findMentions(withGid: self.peer.peerId, withQuery: word)!
                for index in 0..<res.size() {
                    filteredMembers.append(res.getWith(index) as! ACMentionFilterResult)
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
    
    override open func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return filteredMembers.count
    }
    
    override open func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let res = AAAutoCompleteCell(style: UITableViewCellStyle.default, reuseIdentifier: "user_name")
        res.bindData(filteredMembers[(indexPath as NSIndexPath).row], highlightWord: foundWord)
        return res
    }
    
    override open func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let user = filteredMembers[(indexPath as NSIndexPath).row]

        var postfix = " "
        if foundPrefixRange.location == 0 {
            postfix = ": "
        }
        
        acceptAutoCompletion(with: user.mentionString + postfix, keepPrefix: !user.isNickname)
    }
    
    override open func heightForAutoCompletionView() -> CGFloat {
        let cellHeight: CGFloat = 44.0;
        return cellHeight * CGFloat(filteredMembers.count)
    }
    
    override open func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.separatorInset = UIEdgeInsets.zero
        cell.preservesSuperviewLayoutMargins = false
        cell.layoutMargins = UIEdgeInsets.zero
    }

    ////////////////////////////////////////////////////////////
    // MARK: - Picker
    ////////////////////////////////////////////////////////////
    
    open func actionSheetPickedImages(_ images:[(Data,Bool)]) {
        for (i,j) in images {
            Actor.sendUIImage(i, peer: peer, animated:j)
        }
    }
    
    open func actionSheetPickCamera() {
        pickImage(.camera)
    }
    
    open func actionSheetPickGallery() {
        pickImage(.photoLibrary)
    }
    
    open func actionSheetCustomButton(_ index: Int) {
        if index == 0 {
            pickDocument()
        } else if index == 1 {
            pickLocation()
        } else if index == 2 {
            pickContact()
        }
    }
    
    open func pickContact() {
        let pickerController = ABPeoplePickerNavigationController()
        pickerController.peoplePickerDelegate = self
        self.present(pickerController, animated: true, completion: nil)
    }

    open func pickLocation() {
        let pickerController = AALocationPickerController()
        pickerController.delegate = self
        self.present(AANavigationController(rootViewController:pickerController), animated: true, completion: nil)
    }
    
    open func pickDocument() {
        let documentPicker = UIDocumentMenuViewController(documentTypes: UTTAll as [String], in: UIDocumentPickerMode.import)
        documentPicker.view.backgroundColor = UIColor.clear
        documentPicker.delegate = self
        self.present(documentPicker, animated: true, completion: nil)
    }
    
    ////////////////////////////////////////////////////////////
    // MARK: - Document picking
    ////////////////////////////////////////////////////////////
    
    open func documentMenu(_ documentMenu: UIDocumentMenuViewController, didPickDocumentPicker documentPicker: UIDocumentPickerViewController) {
        documentPicker.delegate = self
        self.present(documentPicker, animated: true, completion: nil)
    }
    
    open func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentAt url: URL) {
        
        // Loading path and file name
        let path = url.path
        let fileName = url.lastPathComponent
        
        // Check if file valid or directory
        var isDir : ObjCBool = false
        if !FileManager.default.fileExists(atPath: path, isDirectory: &isDir) {
            // Not exists
            return
        }
        
        // Destination file
        let descriptor = "/tmp/\(UUID().uuidString)"
        let destPath = CocoaFiles.pathFromDescriptor(descriptor)
        
        if isDir.boolValue {
            
            // Zipping contents and sending
            execute(AATools.zipDirectoryCommand(path, to: destPath)) { (val) -> Void in
                Actor.sendDocument(with: self.peer, withName: fileName, withMime: "application/zip", withDescriptor: descriptor)
            }
        } else {
            
            // Sending file itself
            execute(AATools.copyFileCommand(path, to: destPath)) { (val) -> Void in
                Actor.sendDocument(with: self.peer, withName: fileName, withMime: "application/octet-stream", withDescriptor: descriptor)
            }
        }
    }
    
    
    ////////////////////////////////////////////////////////////
    // MARK: - Image picking
    ////////////////////////////////////////////////////////////
    
    func pickImage(_ source: UIImagePickerControllerSourceType) {
        let pickerController = AAImagePickerController()
        pickerController.sourceType = source
        pickerController.mediaTypes = [kUTTypeImage as String,kUTTypeMovie as String]

        pickerController.delegate = self

        self.present(pickerController, animated: true, completion: nil)
    }
    
    open func imagePickerController(_ picker: UIImagePickerController, didFinishPickingImage image: UIImage, editingInfo: [String : AnyObject]?) {
        picker.dismiss(animated: true, completion: nil)
         let imageData = UIImageJPEGRepresentation(image, 0.8)
         Actor.sendUIImage(imageData!, peer: peer, animated:false)
    }
    
    open func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : Any]) {
        picker.dismiss(animated: true, completion: nil)
        if let image = info[UIImagePickerControllerOriginalImage] as? UIImage {
            let imageData = UIImageJPEGRepresentation(image, 0.8)
            
            //TODO: Need implement assert fetching here to get images
            Actor.sendUIImage(imageData!, peer: peer, animated:false)
            
        } else {
            Actor.sendVideo(info[UIImagePickerControllerMediaURL] as! URL, peer: peer)
        }
        
    }
    
    open func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true, completion: nil)
    }
    
    ////////////////////////////////////////////////////////////
    // MARK: - Location picking
    ////////////////////////////////////////////////////////////

    open func locationPickerDidCancelled(_ controller: AALocationPickerController) {
        controller.dismiss(animated: true, completion: nil)
    }
    
    open func locationPickerDidPicked(_ controller: AALocationPickerController, latitude: Double, longitude: Double) {
        Actor.sendLocation(with: self.peer, withLongitude: JavaLangDouble(value: longitude), withLatitude: JavaLangDouble(value: latitude), withStreet: nil, withPlace: nil)
        controller.dismiss(animated: true, completion: nil)
    }
    
    ////////////////////////////////////////////////////////////
    // MARK: - Contact picking
    ////////////////////////////////////////////////////////////
    
    open func peoplePickerNavigationController(_ peoplePicker: ABPeoplePickerNavigationController, didSelectPerson person: ABRecord) {
        
        // Dismissing picker
        
        peoplePicker.dismiss(animated: true, completion: nil)
        
        // Names
        
        let name = ABRecordCopyCompositeName(person)?.takeRetainedValue() as String?
        
        // Avatar
        
        var jAvatarImage: String? = nil
        let hasAvatarImage = ABPersonHasImageData(person)
        if (hasAvatarImage) {
            let imgData = ABPersonCopyImageDataWithFormat(person, kABPersonImageFormatOriginalSize).takeRetainedValue()
            let image = UIImage(data: imgData as Data)?.resizeSquare(90, maxH: 90)
            if (image != nil) {
                let thumbData = UIImageJPEGRepresentation(image!, 0.55)
                jAvatarImage = thumbData?.base64EncodedString(options: NSData.Base64EncodingOptions())
            }
        }
        
        // Phones
        
        let jPhones = JavaUtilArrayList()
        let phoneNumbers: ABMultiValue = ABRecordCopyValue(person, kABPersonPhoneProperty).takeRetainedValue()
        let phoneCount = ABMultiValueGetCount(phoneNumbers)
        for i in 0 ..< phoneCount {
            let phone = (ABMultiValueCopyValueAtIndex(phoneNumbers, i).takeRetainedValue() as! String).trim()
            jPhones?.add(withId: phone)
        }
        
        
        // Email
        let jEmails = JavaUtilArrayList()
        let emails: ABMultiValue = ABRecordCopyValue(person, kABPersonEmailProperty).takeRetainedValue()
        let emailsCount = ABMultiValueGetCount(emails)
        for i in 0 ..< emailsCount {
            let email = (ABMultiValueCopyValueAtIndex(emails, i).takeRetainedValue() as! String).trim()
            if (email.length > 0) {
                jEmails?.add(withId: email)
            }
        }

        // Sending
        
        Actor.sendContact(with: self.peer, withName: name!, withPhones: jPhones!, withEmails: jEmails!, withPhoto: jAvatarImage)
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
        
        audioRecorder.finish { (path: String?, duration: TimeInterval) -> Void in
            
            if (nil == path) {
                print("onAudioRecordingFinished: empty path")
                return
            }
            
            NSLog("onAudioRecordingFinished: %@ [%lfs]", path!, duration)
            let range = path!.range(of: "/tmp", options: NSString.CompareOptions(), range: nil, locale: nil)
            let descriptor = path!.substring(from: range!.lowerBound)
            NSLog("Audio Recording file: \(descriptor)")

            Actor.sendAudio(with: self.peer, withName: NSString.localizedStringWithFormat("%@.ogg", UUID().uuidString) as String,
                withDuration: jint(duration*1000), withDescriptor: descriptor)
        }
        audioRecorder.cancel()
    }
    
    open func audioRecorderDidStartRecording() {
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
    
    func beginRecord(_ button:UIButton,event:UIEvent) {
        
        self.voiceRecorderView.startAnimation()
        
        self.voiceRecorderView.isHidden = false
        self.stickersButton.isHidden = true
        
        let touches : Set<UITouch> = event.touches(for: button)!
        let touch = touches.first!
        let location = touch.location(in: button)
        
        self.voiceRecorderView.trackTouchPoint = location
        self.voiceRecorderView.firstTouchPoint = location
        
        
        self.onAudioRecordingStarted()
    }
    
    func mayCancelRecord(_ button:UIButton,event:UIEvent) {
        
        let touches : Set<UITouch> = event.touches(for: button)!
        let touch = touches.first!
        let currentLocation = touch.location(in: button)
        
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
            
            self.voiceRecorderView.isHidden = true
            self.stickersButton.isHidden = false
            self.stopAudioRecording()
            self.voiceRecorderView.recordingStoped()
            button.cancelTracking(with: event)
            
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
        
        UIView.animate(withDuration: 1.5, delay: 0.0, usingSpringWithDamping: 0.5, initialSpringVelocity: 1.0, options: UIViewAnimationOptions.curveLinear, animations: { () -> Void in
            
            self.leftButton.frame = leftButtonFrame
            self.textView.frame = textViewFrame
            self.stickersButton.frame = stickerViewFrame
            
            }, completion: { (complite) -> Void in
                
                // animation complite
                
        })
        
    }
    
    func finishRecord(_ button:UIButton,event:UIEvent) {
        closeRecorderAnimation()
        self.voiceRecorderView.isHidden = true
        self.stickersButton.isHidden = false
        self.onAudioRecordingFinished()
        self.voiceRecorderView.recordingStoped()
    }
    
    ////////////////////////////////////////////////////////////
    // MARK: - Stickers actions
    ////////////////////////////////////////////////////////////
    
    func updateStickersStateOnCloseKeyboard() {
        self.stickersOpen = false
        self.stickersButton.setImage(UIImage.bundled("sticker_button"), for: UIControlState())
        self.textInputbar.textView.inputView = nil
    }
    
    func changeKeyboard() {
        if self.stickersOpen == false {
            // self.stickersView.loadStickers()
            
            self.textInputbar.textView.inputView = self.stickersView
            self.textInputbar.textView.inputView?.isOpaque = false
            self.textInputbar.textView.inputView?.backgroundColor = UIColor.clear
            self.textInputbar.textView.refreshFirstResponder()
            self.textInputbar.textView.refreshInputViews()
            self.textInputbar.textView.becomeFirstResponder()
            
            self.stickersButton.setImage(UIImage.bundled("keyboard_button"), for: UIControlState())
            
            self.stickersOpen = true
        } else {
            self.textInputbar.textView.inputView = nil
            
            self.textInputbar.textView.refreshFirstResponder()
            self.textInputbar.textView.refreshInputViews()
            self.textInputbar.textView.becomeFirstResponder()
            
            self.stickersButton.setImage(UIImage.bundled("sticker_button"), for: UIControlState())
            
            self.stickersOpen = false
        }
        self.textInputbar.layoutIfNeeded()
        self.view.layoutIfNeeded()
    }
    
    open func stickerDidSelected(_ keyboard: AAStickersKeyboard, sticker: ACSticker) {
        Actor.sendSticker(with: self.peer, with: sticker)
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
    
    override var alignmentRectInsets : UIEdgeInsets {
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
    
    override var alignmentRectInsets : UIEdgeInsets {
        return UIEdgeInsets(top: 0, left: -2, bottom: 0, right: 0)
    }
}
