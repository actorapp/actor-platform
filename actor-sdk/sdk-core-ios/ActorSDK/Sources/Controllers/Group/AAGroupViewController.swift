//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

open class AAGroupViewController: AAContentTableController {
    
    fileprivate let membersSort = { (left: ACGroupMember, right: ACGroupMember) -> Bool in
        let lname = Actor.getUserWithUid(left.uid).getNameModel().get()
        let rname = Actor.getUserWithUid(right.uid).getNameModel().get()
        return lname! < rname!
    }
    
    open var headerRow: AAAvatarRow!
    open var memberRows: AAManagedArrayRows<ACGroupMember, AAGroupMemberCell>!
    
    public init (gid: Int) {
        super.init(style: AAContentTableStyle.settingsPlain)
        
        self.gid = gid
        self.autoTrack = true
        self.unbindOnDissapear = true
        
        self.title = AALocalized("ProfileTitle")
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    
    open override func tableDidLoad() {
        
        // NavigationBar
        if self.group.isCanEditInfo.get().booleanValue() {
            self.navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationEdit"), style: .plain, target: self, action: #selector(editDidPressed))
        } else {
            self.navigationItem.rightBarButtonItem = nil
        }
        
        // Header
        section { (s) -> () in
            self.headerRow = s.avatar { (r) -> () in
                
                r.id = self.gid
                
                r.bindAction = { (r) -> () in
                    r.avatar = self.group.getAvatarModel().get()
                    r.title = self.group.getNameModel().get()
                    r.subtitle = Actor.getFormatter().formatGroupMembers(self.group.getMembersCountModel().get().intValue())
                }
                
                r.avatarDidTap = { (view) -> () in
                    let avatar = self.group.getAvatarModel().get()
                    if avatar != nil && avatar?.fullImage != nil {
                        
                        let full = avatar?.fullImage.fileReference
                        let small = avatar?.smallImage.fileReference
                        let size = CGSize(width: Int((avatar?.fullImage.width)!), height: Int((avatar?.fullImage.height)!))
                        
                        self.present(AAPhotoPreviewController(file: full!, previewFile: small, size: size, fromView: view), animated: true, completion: nil)
                    }
                }
            }
        }
        
        // About
        if let about = self.group.about.get() {
            section { (s) in
                
                s.autoSeparatorTopOffset = 1
                
                s.header(AALocalized("GroupDescription").uppercased()).height = 22
                
                s.text(nil, content: about)
            }
        }
        
        // Link
        if let shortName = self.group.shortName.get(), let prefix = ActorSDK.sharedActor().invitePrefix {
            section { (s) in
                s.text(nil, content: prefix + shortName)
            }
        }
        
        // Calls
        if (ActorSDK.sharedActor().enableCalls) {
            let members = (group.members.get() as! JavaUtilHashSet).size()
            if (members <= 20) { // Temporary limitation
                if group.groupType == ACGroupType.group() {
                    section { (s) -> () in
                        s.action("CallsStartGroupAudio") { (r) -> () in
                            r.selectAction = { () -> Bool in
                                self.execute(Actor.doCall(withGid: jint(self.gid)))
                                return true
                            }
                        }
                    }
                }
            }
        }
        
        // Actions and Settings
        section { (s) -> () in
            
            // Notifications
            s.common { (r) -> () in
                
                let groupPeer: ACPeer! = ACPeer.group(with: jint(self.gid))
                
                r.style = .switch
                r.content = AALocalized("GroupNotifications")
                
                r.bindAction = { (r) -> () in
                    r.switchOn = Actor.isNotificationsEnabled(with: groupPeer)
                }
                
                r.switchAction = { (on: Bool) -> () in
                    Actor.changeNotificationsEnabled(with: groupPeer, withValue: on)
                }
                
                if(ActorSDK.sharedActor().enableChatGroupSound) {
                    if(Actor.isNotificationsEnabled(with: groupPeer)){
                        r.selectAction = {() -> Bool in
                            // Sound: Choose sound
                            let setRingtoneController = AARingtonesViewController()
                            let soundForGroupe = Actor.getNotificationsSound(with: groupPeer)
                            setRingtoneController.selectedRingtone = (soundForGroupe != nil) ? soundForGroupe! : ""
                            setRingtoneController.completion = {(selectedSound:String) in
                                Actor.changeNotificationsSound(groupPeer, withValue: selectedSound)
                            }
                            let navigationController = AANavigationController(rootViewController: setRingtoneController)
                            if (AADevice.isiPad) {
                                navigationController.isModalInPopover = true
                                navigationController.modalPresentationStyle = UIModalPresentationStyle.currentContext
                            }
                            self.present(navigationController, animated: true, completion: nil)
                            
                            return false
                        }
                    }
                }
            }
            
            // Admininstration
            if group.isCanEditAdministration.get().booleanValue() || group.isCanDelete.get().booleanValue() {
                s.common({ (r) in
                    r.content = AALocalized("GroupAdministration")
                    r.selectAction = { () -> Bool in
                        self.navigateNext(AAGroupAdministrationViewController(gid: self.gid))
                        return false
                    }
                })
            }
            
            // View Members
            if self.group.isCanViewMembers.get().booleanValue() && self.group.isAsyncMembers.get().booleanValue() {
                s.common({ (r) -> () in
                    r.content = AALocalized("GroupViewMembers")
                    r.style = .normal
                    r.selectAction = { () -> Bool in
                        self.navigateNext(AAGroupViewMembersController(gid: self.gid))
                        return false
                    }
                })
                
                s.action("GroupAddParticipant") { (r) -> () in
                    
                    r.selectAction = { () -> Bool in
                        let addParticipantController = AAAddParticipantViewController(gid: self.gid)
                        let navigationController = AANavigationController(rootViewController: addParticipantController)
                        if (AADevice.isiPad) {
                            navigationController.isModalInPopover = true
                            navigationController.modalPresentationStyle = UIModalPresentationStyle.currentContext
                        }
                        self.present(navigationController, animated: true, completion: nil)
                        return false
                        
                    }
                }
            }

        }
        
        // Members
        
        if group.isCanViewMembers.get().booleanValue() && !group.isAsyncMembers.get().booleanValue() {
            
            section { (s) -> () in
                
                s.autoSeparatorsInset = 65
                s.autoSeparatorTopOffset = 1
                s.headerHeight = 0
                
                // Members: Header
                s.header(AALocalized("GroupViewMembers").uppercased())
                
                // Members: Add
                s.action("GroupAddParticipant") { (r) -> () in
                    
                    r.contentInset = 65
                    
                    r.selectAction = { () -> Bool in
                        let addParticipantController = AAAddParticipantViewController(gid: self.gid)
                        let navigationController = AANavigationController(rootViewController: addParticipantController)
                        if (AADevice.isiPad) {
                            navigationController.isModalInPopover = true
                            navigationController.modalPresentationStyle = UIModalPresentationStyle.currentContext
                        }
                        self.present(navigationController, animated: true, completion: nil)
                        return false
                    }
                }
                
                // Members: List
                self.memberRows = s.arrays { (r: AAManagedArrayRows<ACGroupMember, AAGroupMemberCell>) -> () in
                    r.height = 48
                    r.data = (self.group.members.get() as AnyObject).toArray().toSwiftArray()
                    r.data.sort(by: self.membersSort)
                    
                    r.bindData = { (c, d) -> () in
                        let user = Actor.getUserWithUid(d.uid)
                        c.bind(user, isAdmin: d.isAdministrator)
                        
                        // Notify to request onlines
                        Actor.onUserVisible(withUid: d.uid)
                    }
                    
                    r.selectAction = { (d) -> Bool in
                        let user = Actor.getUserWithUid(d.uid)
                        if (user.getId() == Actor.myUid()) {
                            return true
                        }
                        
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
                                if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer.user(with: user.getId())) {
                                    self.navigateDetail(customController)
                                } else {
                                    self.navigateDetail(ConversationViewController(peer: ACPeer.user(with: user.getId())))
                                }
                                self.popover?.dismiss(animated: true)
                            }
                            
                            a.action("GroupMemberCall", closure: { () -> () in
                                let phones = user.getPhonesModel().get()
                                if phones?.size() == 0 {
                                    self.alertUser("GroupMemberCallNoPhones")
                                } else if phones?.size() == 1 {
                                    let number = phones!.getWith(0)
                                    ActorSDK.sharedActor().openUrl("telprompt://+\(number!.phone)")
                                } else {
                                    
                                    var numbers = [String]()
                                    for i in 0..<phones!.size() {
                                        let p = phones!.getWith(i)
                                        numbers.append("\(p!.title): +\(p!.phone)")
                                    }
                                    self.showActionSheet(numbers,
                                        cancelButton: "AlertCancel",
                                        destructButton: nil,
                                        sourceView: self.view,
                                        sourceRect: self.view.bounds,
                                        tapClosure: { (index) -> () in
                                            if (index >= 0) {
                                                let number = phones!.getWith(jint(index))
                                                ActorSDK.sharedActor().openUrl("telprompt://+\(number!.phone)")
                                            }
                                    })
                                }
                            })
                            
                            // Can kick user
                            let canKick: Bool =
                                (self.group.isCanKickAnyone.get().booleanValue() ||
                                (self.group.isCanKickInvited.get().booleanValue() && d.inviterUid == Actor.myUid()))
                            
                            if canKick {
                                let name = Actor.getUserWithUid(d.uid).getNameModel().get()
                                a.destructive("GroupMemberKick") { () -> () in
                                    self.confirmDestructive(AALocalized("GroupMemberKickMessage")
                                        .replace("{name}", dest: name!), action: AALocalized("GroupMemberKickAction")) {
                                        self.executeSafe(Actor.kickMemberCommand(withGid: jint(self.gid), withUid: user.getId()))
                                    }
                                }
                            }
                        }
                        
                        return true
                    }
                }
            }
        }
        
        // Leave group
        if group.isCanLeave.get().booleanValue() {
            section { (s) -> () in
                s.common({ (r) -> () in
                    
                    if self.group.groupType == ACGroupType.channel() {
                        r.content = AALocalized("ActionLeaveChannel")
                    } else {
                        r.content = AALocalized("ActionDeleteAndExit")
                    }
                    
                    r.style = .destructive
                    r.selectAction = { () -> Bool in
                        
                        let title: String
                        let action: String
                        if self.group.groupType == ACGroupType.channel() {
                            title = AALocalized("ActionLeaveChannelMessage")
                            action = AALocalized("ActionLeaveChannelAction")
                        } else {
                            title = AALocalized("ActionDeleteAndExitMessage")
                            action = AALocalized("ActionDeleteAndExitMessageAction")
                        }
                        self.confirmDestructive(title, action: action, yes: { () -> () in
                            self.executePromise(Actor.leaveAndDeleteGroup(withGid: jint(self.gid)))
                        })
                        
                        return true
                    }
                })
            }
        }
    }
    
    open override func tableWillBind(_ binder: AABinder) {
        
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
                    self.memberRows.data.sort(by: self.membersSort)
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
    
    open func editDidPressed() {
        self.presentInNavigation(AAGroupEditInfoController(gid: gid))
    }
}
