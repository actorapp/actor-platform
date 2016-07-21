//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class AAGroupViewController: AAContentTableController {
    
    private let membersSort = { (left: ACGroupMember, right: ACGroupMember) -> Bool in
        let lname = Actor.getUserWithUid(left.uid).getNameModel().get()
        let rname = Actor.getUserWithUid(right.uid).getNameModel().get()
        return lname < rname
    }
    
    public var headerRow: AAAvatarRow!
    public var memberRows: AAManagedArrayRows<ACGroupMember, AAGroupMemberCell>!
    
    public init (gid: Int) {
        super.init(style: AAContentTableStyle.SettingsPlain)
        
        self.gid = gid
        self.autoTrack = true
        
        self.title = AALocalized("ProfileTitle")
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    
    public override func tableDidLoad() {
        
        // NavigationBar
        if self.group.isCanEditInfo.get().booleanValue() {
            self.navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationEdit"), style: .Plain, target: self, action: #selector(editDidPressed))
        } else {
            self.navigationItem.rightBarButtonItem = nil
        }
        
        // Header
        section { (s) -> () in
            
            // Header: Avatar
            self.headerRow = s.avatar { (r) -> () in
                
                r.id = self.gid
                
                r.bindAction = { (r) -> () in
                    r.avatar = self.group.getAvatarModel().get()
                    r.title = self.group.getNameModel().get()
                    r.subtitle = Actor.getFormatter().formatGroupMembers(self.group.getMembersCountModel().get().intValue())
                    self.group.getPresenceModel().get()
                }
                
                r.avatarDidTap = { (view) -> () in
                    let avatar = self.group.getAvatarModel().get()
                    if avatar != nil && avatar.fullImage != nil {
                        
                        let full = avatar.fullImage.fileReference
                        let small = avatar.smallImage.fileReference
                        let size = CGSize(width: Int(avatar.fullImage.width), height: Int(avatar.fullImage.height))
                        
                        self.presentViewController(AAPhotoPreviewController(file: full, previewFile: small, size: size, fromView: view), animated: true, completion: nil)
                    }
                }
            }
            
            // About
            if let about = self.group.about.get() {
                if self.group.groupType == ACGroupType.CHANNEL() {
                    s.text("GroupAboutChannel", content: about)
                } else {
                    s.text("GroupAbout", content: about)
                }
            }
            
//            if self.group.isCanEditInfo.get().booleanValue() {
//                
//                // Header: Change photo
//                s.action("GroupSetPhoto") { (r) -> () in
//                    r.selectAction = { () -> Bool in
//                        let hasCamera = UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera)
//                        self.showActionSheet( hasCamera ? ["PhotoCamera", "PhotoLibrary"] : ["PhotoLibrary"],
//                            cancelButton: "AlertCancel",
//                            destructButton: self.group.getAvatarModel().get() != nil ? "PhotoRemove" : nil,
//                            sourceView: self.view,
//                            sourceRect: self.view.bounds,
//                            tapClosure: { (index) -> () in
//                                if (index == -2) {
//                                    self.confirmAlertUser("PhotoRemoveGroupMessage",
//                                        action: "PhotoRemove",
//                                        tapYes: { () -> () in
//                                            Actor.removeGroupAvatarWithGid(jint(self.gid))
//                                        }, tapNo: nil)
//                                } else if (index >= 0) {
//                                    let takePhoto: Bool = (index == 0) && hasCamera
//                                    self.pickAvatar(takePhoto, closure: { (image) -> () in
//                                        Actor.changeGroupAvatar(jint(self.gid), image: image)
//                                    })
//                                }
//                        })
//                        
//                        return true
//                    }
//                }
//                
//                // Header: Change title
//                s.action("GroupSetTitle") { (r) -> () in
//                    r.selectAction = { () -> Bool in
//                        self.startEditField { (c) -> () in
//                            
//                            c.title = "GroupEditHeader"
//                            
//                            c.fieldHint = "GroupEditHint"
//                            
//                            c.actionTitle = "NavigationSave"
//                            
//                            c.initialText = self.group.getNameModel().get()
//                            
//                            c.didDoneTap = { (t, c) -> () in
//                                
//                                if t.length == 0 {
//                                    return
//                                }
//                                
//                                //                            c.executeSafeOnlySuccess(Actor.editGroupTitleCommandWithGid(jint(self.gid), withTitle: t)!, successBlock: { (val) -> Void in
//                                //                                c.dismiss()
//                                //                            })
//                            }
//                        }
//                        
//                        return true
//                    }
//                }
//            }
            
        }
        
        // Calls
        if (ActorSDK.sharedActor().enableCalls) {
            let members = (group.members.get() as! JavaUtilHashSet).size()
            if (members <= 20) { // Temporary limitation
                if group.groupType == ACGroupType.GROUP() {
                    section { (s) -> () in
                        s.action("CallsStartGroupAudio") { (r) -> () in
                            r.selectAction = { () -> Bool in
                                self.execute(Actor.doCallWithGid(jint(self.gid)))
                                return true
                            }
                        }
                    }
                }
            }
        }
        
        // Notifications
        section { (s) -> () in
            
            let groupPeer: ACPeer! = ACPeer.groupWithInt(jint(self.gid))
            
            s.common { (r) -> () in
                
                r.style = .Switch
                r.content = AALocalized("GroupNotifications")
                
                r.bindAction = { (r) -> () in
                    r.switchOn = Actor.isNotificationsEnabledWithPeer(groupPeer)
                }
                
                r.switchAction = { (on: Bool) -> () in
                    Actor.changeNotificationsEnabledWithPeer(groupPeer, withValue: on)
                }
                
                if(ActorSDK.sharedActor().enableChatGroupSound) {
                    if(Actor.isNotificationsEnabledWithPeer(groupPeer)){
                        r.selectAction = {() -> Bool in
                            // Sound: Choose sound
                            let setRingtoneController = AARingtonesViewController()
                            let soundForGroupe = Actor.getNotificationsSoundWithPeer(groupPeer)
                            setRingtoneController.selectedRingtone = (soundForGroupe != nil) ? soundForGroupe : ""
                            setRingtoneController.completion = {(selectedSound:String) in
                                Actor.changeNotificationsSoundPeer(groupPeer, withValue: selectedSound)
                            }
                            let navigationController = AANavigationController(rootViewController: setRingtoneController)
                            if (AADevice.isiPad) {
                                navigationController.modalInPopover = true
                                navigationController.modalPresentationStyle = UIModalPresentationStyle.CurrentContext
                            }
                            self.presentViewController(navigationController, animated: true, completion: nil)
                            
                            return false
                        }
                    }
                }
            }
            
            // View Members
            if self.group.isCanViewMembers.get().booleanValue() && self.group.isAsyncMembers.get().booleanValue() {
                //section { (s) -> () in
                    
                    s.common({ (r) -> () in
                        r.content = AALocalized("GroupViewMembers")
                        r.style = .Normal
                        r.selectAction = { () -> Bool in
                            // TODO: Implement
                            return false
                        }
                    })
                    
                    s.action("GroupAddParticipant") { (r) -> () in
                        
                        r.selectAction = { () -> Bool in
                            let addParticipantController = AAAddParticipantViewController(gid: self.gid)
                            let navigationController = AANavigationController(rootViewController: addParticipantController)
                            if (AADevice.isiPad) {
                                navigationController.modalInPopover = true
                                navigationController.modalPresentationStyle = UIModalPresentationStyle.CurrentContext
                            }
                            self.presentViewController(navigationController, animated: true, completion: nil)
                            return false
                        }
                    }
                //}
            }

        }
        
        
        // Leave group
        if group.isCanLeave.get().booleanValue() {
            section { (s) -> () in
                s.common({ (r) -> () in
                    
                    if self.group.groupType == ACGroupType.CHANNEL() {
                        r.content = AALocalized("GroupLeaveChannel")
                    } else {
                        r.content = AALocalized("GroupLeave")
                    }
                    
                    r.style = .Destructive
                    r.selectAction = { () -> Bool in
                        
                        let title: String
                        if self.group.groupType == ACGroupType.CHANNEL() {
                            title = AALocalized("GroupLeaveConfirmChannel")
                        } else {
                            title = AALocalized("GroupLeaveConfirm")
                        }
                        self.confirmDestructive(title, action: AALocalized("GroupLeaveConfirmAction"), yes: { () -> () in
                            // self.executeSafe(Actor.leaveGroupCommandWithGid(jint(self.gid))!)
                        })
                        
                        return true
                    }
                })
            }
        }
        
        // Members
        
        if group.isCanViewMembers.get().booleanValue() && !group.isAsyncMembers.get().booleanValue() {
            
            section { (s) -> () in
                
                s.autoSeparatorsInset = 65
                s.autoSeparatorTopOffset = 1
                s.headerHeight = 0
                
                // Members: Header
                s.header(AALocalized("GroupMembers").uppercaseString)
                
                // Members: Add
                s.action("GroupAddParticipant") { (r) -> () in
                    
                    r.contentInset = 65
                    
                    r.selectAction = { () -> Bool in
                        let addParticipantController = AAAddParticipantViewController(gid: self.gid)
                        let navigationController = AANavigationController(rootViewController: addParticipantController)
                        if (AADevice.isiPad) {
                            navigationController.modalInPopover = true
                            navigationController.modalPresentationStyle = UIModalPresentationStyle.CurrentContext
                        }
                        self.presentViewController(navigationController, animated: true, completion: nil)
                        return false
                    }
                }
                
                // Members: List
                self.memberRows = s.arrays { (r: AAManagedArrayRows<ACGroupMember, AAGroupMemberCell>) -> () in
                    r.height = 48
                    r.data = self.group.members.get().toArray().toSwiftArray()
                    r.data.sortInPlace(self.membersSort)
                    
                    r.bindData = { (c, d) -> () in
                        let user = Actor.getUserWithUid(d.uid)
                        c.bind(user, isAdmin: d.isAdministrator)
                        
                        // Notify to request onlines
                        Actor.onUserVisibleWithUid(d.uid)
                    }
                    
                    r.selectAction = { (d) -> Bool in
                        let user = Actor.getUserWithUid(d.uid)
                        if (user.getId() == Actor.myUid()) {
                            return true
                        }
                        
                        let name = user.getNameModel().get()
                        
                        self.alertSheet { (a: AAAlertSetting) -> () in
                            
                            a.cancel = "AlertCancel"
                            
                            a.action("GroupMemberInfo") { () -> () in
                                var controller: AAViewController! = ActorSDK.sharedActor().delegate.actorControllerForUser(Int(user.getId()))
                                if controller == nil {
                                    controller = AAUserViewController(uid: Int(user.getId()))
                                }
                                self.navigateNext(controller, removeCurrent: false)
                            }
                            
                            a.action("GroupMemberWrite") { () -> () in
                                if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer.userWithInt(user.getId())) {
                                    self.navigateDetail(customController)
                                } else {
                                    self.navigateDetail(ConversationViewController(peer: ACPeer.userWithInt(user.getId())))
                                }
                                self.popover?.dismissPopoverAnimated(true)
                            }
                            
                            a.action("GroupMemberCall", closure: { () -> () in
                                let phones = user.getPhonesModel().get()
                                if phones.size() == 0 {
                                    self.alertUser("GroupMemberCallNoPhones")
                                } else if phones.size() == 1 {
                                    let number = phones.getWithInt(0)
                                    ActorSDK.sharedActor().openUrl("telprompt://+\(number.phone)")
                                } else {
                                    
                                    var numbers = [String]()
                                    for i in 0..<phones.size() {
                                        let p = phones.getWithInt(i)
                                        numbers.append("\(p.title): +\(p.phone)")
                                    }
                                    self.showActionSheet(numbers,
                                        cancelButton: "AlertCancel",
                                        destructButton: nil,
                                        sourceView: self.view,
                                        sourceRect: self.view.bounds,
                                        tapClosure: { (index) -> () in
                                            if (index >= 0) {
                                                let number = phones.getWithInt(jint(index))
                                                ActorSDK.sharedActor().openUrl("telprompt://+\(number.phone)")
                                            }
                                    })
                                }
                            })
                            
                            //                        // Detect if we are admin
                            //                        let members: [ACGroupMember] = self.group.members.get().toArray().toSwiftArray()
                            //                        var isAdmin = self.group.creatorId == Actor.myUid()
                            //                        if !isAdmin {
                            //                            for m in members {
                            //                                if m.uid == Actor.myUid() {
                            //                                    isAdmin = m.isAdministrator
                            //                                }
                            //                            }
                            //                        }
                            //
                            //                        // Can mark as admin
                            //                        let canMarkAdmin = isAdmin && !d.isAdministrator
                            //
                            //                        if canMarkAdmin {
                            //                            a.action("GroupMemberMakeAdmin") { () -> () in
                            //
                            //                                self.confirmDestructive(AALocalized("GroupMemberMakeMessage").replace("{name}", dest: name), action: AALocalized("GroupMemberMakeAction")) {
                            //
                            //                                    self.executeSafe(Actor.makeAdminCommandWithGid(jint(self.gid), withUid: jint(user.getId()))!)
                            //                                }
                            //                            }
                            //                        }
                            
                            // Can kick user
                            //                        let canKick = isAdmin || d.inviterUid == Actor.myUid()
                            //                        
                            //                        if canKick {
                            //                            a.destructive("GroupMemberKick") { () -> () in
                            //                                self.confirmDestructive(AALocalized("GroupMemberKickMessage")
                            //                                    .replace("{name}", dest: name), action: AALocalized("GroupMemberKickAction")) {
                            //                                        
                            //                                        self.executeSafe(Actor.kickMemberCommandWithGid(jint(self.gid), withUid: user.getId())!)
                            //                                }
                            //                            }
                            //                        }
                        }
                        
                        return true
                    }
                }
            }
        }
    }
    
    public override func tableWillBind(binder: AABinder) {
        
        // Bind group info
        
        binder.bind(group.getNameModel()) { (value: String?) -> () in
            self.headerRow.reload()
        }
        
        binder.bind(group.getAvatarModel()) { (value: ACAvatar?) -> () in
            self.headerRow.reload()
        }
        
        // Bind members
        if memberRows != nil{
            binder.bind(group.getMembersModel()) { (value: JavaUtilHashSet?) -> () in
                if let v = value {
                    self.memberRows.data = v.toArray().toSwiftArray()
                    self.memberRows.data.sortInPlace(self.membersSort)
                    self.memberRows.reload()
                }
            }
        }
        
        // Bind membership status
        
        binder.bind(group.isMemberModel()) { (member: JavaLangBoolean?) -> () in
            if let m = member {
                if Bool(m.booleanValue()) == true {
                    self.hidePlaceholder()
                } else {
                    self.showPlaceholderWithImage(
                        UIImage.bundled("contacts_list_placeholder"),
                        title: AALocalized("Placeholder_Group_Title"),
                        subtitle: AALocalized("Placeholder_Group_Message"))
                }
            } else {
                self.hidePlaceholder()
            }
        }
    }
    
    public func editDidPressed() {
        self.navigateNext(AAGroupEditInfoController(gid: gid))
    }
}