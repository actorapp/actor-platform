//
//  MessagesViewController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 11.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
import UIKit
import MobileCoreServices

class MessagesViewController: EngineSlackListController, UIDocumentPickerDelegate, ABActionShitDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate {

    var peer: AMPeer!;
    let binder: Binder = Binder();
    
    let titleView: UILabel = UILabel();
    let subtitleView: UILabel = UILabel();
    let navigationView: UIView = UIView();
    
    let avatarView = BarAvatarView(frameSize: 36)
    
    init(peer: AMPeer) {
        super.init(isInverted: true);
        
        // Hack for fixing top offsets
        self.edgesForExtendedLayout = UIRectEdge.All ^ UIRectEdge.Top;
        
        self.peer = peer;
        self.tableView.separatorStyle = UITableViewCellSeparatorStyle.None;
        self.tableView.backgroundColor = UIColor.clearColor();
        self.tableView.allowsSelection = false;
        self.tableView.tableHeaderView = UIView(frame:CGRectMake(0, 0, 100, 6));
        
        self.textInputbar.backgroundColor = UIColor.whiteColor();
        self.textInputbar.autoHideRightButton = false;
        self.textView.placeholder = "Message";
        self.rightButton.titleLabel?.text = "Send"
        
        self.keyboardPanningEnabled = true;
        
        self.leftButton.setImage(UIImage(named: "conv_attach"), forState: UIControlState.Normal)
        
        // Title
        
        navigationView.frame = CGRectMake(0, 0, 190, 44);
        navigationView.autoresizingMask = UIViewAutoresizing.FlexibleWidth;
        
        titleView.frame = CGRectMake(0, 4, 190, 20)
        titleView.font = UIFont(name: "HelveticaNeue-Medium", size: 17)!
        titleView.adjustsFontSizeToFitWidth = false;
        titleView.textColor = UIColor.whiteColor();
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
        
        // Avatar
        
        avatarView.frame = CGRectMake(0, 0, 36, 36)
        var tapGesture = UITapGestureRecognizer(target: self, action: "onAvatarTap");
        tapGesture.numberOfTapsRequired = 1
        tapGesture.numberOfTouchesRequired = 1
        avatarView.addGestureRecognizer(tapGesture)
        
        var barItem = UIBarButtonItem(customView: avatarView)
        self.navigationItem.rightBarButtonItem = barItem
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated);
        textView.text = MSG.loadDraft(peer);
        var image = UIImage(named: "ChatBackground");
        var bg = UIImageView(image: UIImage(named: "ChatBackground"));
        view.insertSubview(bg, atIndex: 0);
        
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
            
            binder.bind(MSG.getTyping(peer.getPeerId())!.getTyping(), valueModel2: user.getPresence()!, closure:{ (typing:JavaLangBoolean?, presence:AMUserPresence?) -> () in
                
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
            binder.bind(MSG.getGroupTyping(group.getId()).getActive()!, valueModel2: group.getMembers(), valueModel3: group.getPresence(), closure: { (value1:IOSIntArray?, value2:JavaUtilHashSet?, value3:JavaLangInteger?) -> () in
                if (value1!.length() > 0) {
                    self.subtitleView.textColor = Resources.PrimaryLightText
                    if (value1!.length() == 1) {
                        var uid = value1!.intAtIndex(0);
                        var user = MSG.getUsers().getWithLong(jlong(uid)) as! AMUserVM;
                        self.subtitleView.text = MSG.getFormatter().formatTypingWithNSString(user.getName().get() as!String)
                    } else {
                        self.subtitleView.text = MSG.getFormatter().formatTypingWithInt(value1!.length());
                    }
                } else {
                    var membersString = MSG.getFormatter().formatGroupMembersWithInt(value2!.size())
                    if (value3 == nil || value3!.integerValue == 0) {
                        self.subtitleView.textColor = Resources.SecondaryLightText
                        self.subtitleView.text = membersString;
                    } else {
                        membersString = membersString + ", ";
                        var onlineString = MSG.getFormatter().formatGroupOnlineWithInt(value3!.intValue());
                        var attributedString = NSMutableAttributedString(string: (membersString + onlineString))
                        attributedString.addAttribute(NSForegroundColorAttributeName, value: Resources.PrimaryLightText, range: NSMakeRange(membersString.size(), onlineString.size()))
                        self.subtitleView.attributedText = attributedString
                    }
                }
            })
        }
        
        MSG.onConversationOpen(peer)
    }
    
    func onAvatarTap() {
        if (UInt(peer.getPeerType().ordinal()) == AMPeerType.PRIVATE.rawValue) {
            self.navigationController?.pushViewController(ProfileController(uid: Int(peer.getPeerId())), animated: true)
        } else if (UInt(peer.getPeerType().ordinal()) == AMPeerType.GROUP.rawValue) {
            self.navigationController?.pushViewController(GroupController(gid: Int(peer.getPeerId())), animated: true)
        }
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        MSG.onConversationClosed(peer)
    }
    
    override func textWillUpdate() {
        super.textWillUpdate();
        MSG.onTyping(peer);
    }
    
    override func didPressRightButton(sender: AnyObject!) {
        
        // Perform auto correct
        textView.refreshFirstResponder();
        
        MSG.sendMessage(peer, withText: textView.text);
        
        super.didPressRightButton(sender);
    }
    
    override func didPressLeftButton(sender: AnyObject!) {
        super.didPressLeftButton(sender)
        
        var actionShit = ABActionShit()
        actionShit.buttonTitles = ["Take Photo","Record Video", "Media Library", "Document"]
        actionShit.cancelButtonTitle = "Cancel"
        actionShit.delegate = self
        actionShit.showWithCompletion(nil)
    }
    
    func actionShit(actionShit: ABActionShit!, clickedButtonAtIndex buttonIndex: Int) {
        if (buttonIndex == 0) {
            var pickerController = UIImagePickerController()
            pickerController.sourceType = UIImagePickerControllerSourceType.Camera
            pickerController.mediaTypes = [kUTTypeImage]
            pickerController.view.backgroundColor = UIColor.blackColor()
            pickerController.navigationBar.tintColor = Resources.TintColor
            pickerController.delegate = self
            pickerController.navigationBar.tintColor = UIColor.whiteColor()
            pickerController.navigationBar.titleTextAttributes = [NSForegroundColorAttributeName: UIColor.whiteColor()];
            self.presentViewController(pickerController, animated: true, completion: nil)
        } else if (buttonIndex == 1) {
            var pickerController = UIImagePickerController()
            pickerController.sourceType = UIImagePickerControllerSourceType.Camera
            pickerController.mediaTypes = [kUTTypeVideo, kUTTypeMovie]
            pickerController.view.backgroundColor = UIColor.blackColor()
            pickerController.navigationBar.tintColor = Resources.TintColor
            pickerController.delegate = self
            pickerController.navigationBar.tintColor = UIColor.whiteColor()
            pickerController.navigationBar.titleTextAttributes = [NSForegroundColorAttributeName: UIColor.whiteColor()];
            self.presentViewController(pickerController, animated: true, completion: nil)
        } else if (buttonIndex == 2) {
            var pickerController = UIImagePickerController()
            pickerController.sourceType = UIImagePickerControllerSourceType.PhotoLibrary
            pickerController.mediaTypes = [kUTTypeImage, kUTTypeVideo, kUTTypeMovie]
            pickerController.view.backgroundColor = UIColor.blackColor()
            pickerController.navigationBar.tintColor = Resources.TintColor
            pickerController.delegate = self
            pickerController.navigationBar.tintColor = UIColor.whiteColor()
            pickerController.navigationBar.titleTextAttributes = [NSForegroundColorAttributeName: UIColor.whiteColor()];
            self.presentViewController(pickerController, animated: true, completion: nil)
        } else if (buttonIndex == 3) {
            var documentView = UIDocumentPickerViewController(documentTypes: [kUTTypeText as NSString, "com.apple.iwork.pages.pages", "com.apple.iwork.numbers.numbers", "com.apple.iwork.keynote.key"], inMode: UIDocumentPickerMode.Import)
            documentView.delegate = self
            documentView.view.backgroundColor = UIColor.whiteColor()
            self.presentViewController(documentView, animated: true, completion: nil)
        }
    }
    
    // Image picker
    
    func navigationController(navigationController: UINavigationController, willShowViewController viewController: UIViewController, animated: Bool) {
        UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.LightContent, animated: false)
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage!, editingInfo: [NSObject : AnyObject]!) {
        picker.dismissViewControllerAnimated(true, completion: nil)
        
        MSG.sendUIImage(image, peer: peer!)
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [NSObject : AnyObject]) {
        picker.dismissViewControllerAnimated(true, completion: nil)
        
        MSG.sendUIImage(info[UIImagePickerControllerOriginalImage] as! UIImage, peer: peer!)
    }
    
    func imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion: nil)
    }
    
    // Document picker
    
    func documentPicker(controller: UIDocumentPickerViewController, didPickDocumentAtURL url: NSURL) {
        var path = url.path;
        
        // TODO: Implement
    }
    
    override func buildCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?) -> UITableViewCell {
        
        var message = (item as! AMMessage);
        
        if (message.getSenderId() != MSG.myUid()){
            MSG.onInMessageShown(peer, withRid: message.getRid(), withDate: message.getDate(), withEncrypted: false);
        }
        
        if (message.getContent() is AMTextContent){
            var cell = tableView.dequeueReusableCellWithIdentifier("bubble_text") as! BubbleTextCell?
            if (cell == nil) {
                cell = BubbleTextCell(reuseId: "bubble_text")
            }
            return cell!
        } else if (message.getContent() is AMPhotoContent || message.getContent() is AMVideoContent) {
            var cell = tableView.dequeueReusableCellWithIdentifier("bubble_media") as! BubbleMediaCell?
            if (cell == nil) {
                cell = BubbleMediaCell(reuseId: "bubble_media")
            }
            return cell!
            
        } else if (message.getContent() is AMServiceContent){
            var cell = tableView.dequeueReusableCellWithIdentifier("bubble_service") as! BubbleServiceCell?
            if (cell == nil) {
                cell = BubbleServiceCell(reuseId: "bubble_service")
            }
            return cell!
        } else {
            var cell = tableView.dequeueReusableCellWithIdentifier("bubble_unsupported") as! BubbleUnsupportedCell?
            if (cell == nil) {
                cell = BubbleUnsupportedCell(reuseId: "bubble_unsupported")
            }
            return cell!
        }
    }
    
    override func bindCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?, cell: UITableViewCell) {
        (cell as! BubbleCell).performBind(item as! AMMessage);
    }
    
    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        var item = objectAtIndexPath(indexPath) as! AMMessage;
        return BubbleCell.measureHeight(item);
    }
    
    override func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated);
        MSG.saveDraft(peer, withText: textView.text);
    }
    
    override func getDisplayList() -> AMBindedDisplayList {
        return MSG.getMessagesGlobalListWithAMPeer(peer)
    }
}

class BarAvatarView : AvatarView {
    override init(frameSize: Int) {
        super.init(frameSize: frameSize)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func alignmentRectInsets() -> UIEdgeInsets {
        return UIEdgeInsets(top: 0, left: 36, bottom: 0, right: 8)
    }
}