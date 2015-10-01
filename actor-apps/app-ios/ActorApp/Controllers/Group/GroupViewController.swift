//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class GroupViewController: ACContentTableController {
    
    private let membersSort = { (left: ACGroupMember, right: ACGroupMember) -> Bool in
        let lname = Actor.getUserWithUid(left.uid).getNameModel().get()
        let rname = Actor.getUserWithUid(right.uid).getNameModel().get()
        return lname < rname
    }
    
    var headerRow: ACAvatarRow!
    var memberRows: ACManagedArrayRows<ACGroupMember, GroupMemberCell>!
    
    init (gid: Int) {
        super.init(style: ACContentTableStyle.SettingsPlain)
        
        self.gid = gid
        self.autoTrack = true
        
        self.title = localized("ProfileTitle")
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    
    override func tableDidLoad() {

        // Header
        section { (s) -> () in
        
            // Header: Avatar
            self.headerRow = s.avatar { (r) -> () in
                
                r.id = self.gid
                
                r.bindAction = { (r) -> () in
                    r.avatar = self.group.getAvatarModel().get()
                    r.title = self.group.getNameModel().get()
                }
                
                r.avatarDidTap = { (view) -> () in
                    let avatar = self.group.getAvatarModel().get()
                    if avatar != nil && avatar.fullImage != nil {
                        
                        let full = avatar.fullImage.fileReference
                        let small = avatar.smallImage.fileReference
                        let size = CGSize(width: Int(avatar.fullImage.width), height: Int(avatar.fullImage.height))
                        
                        self.presentViewController(PhotoPreviewController(file: full, previewFile: small, size: size, fromView: view), animated: true, completion: nil)
                    }
                }
            }
            
            // Header: Change photo
            s.action("GroupSetPhoto") { (r) -> () in
                r.selectAction = { () -> Bool in
                    let hasCamera = UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera)
                    self.showActionSheet( hasCamera ? ["PhotoCamera", "PhotoLibrary"] : ["PhotoLibrary"],
                        cancelButton: "AlertCancel",
                        destructButton: self.group.getAvatarModel().get() != nil ? "PhotoRemove" : nil,
                        sourceView: self.view,
                        sourceRect: self.view.bounds,
                        tapClosure: { (index) -> () in
                            if (index == -2) {
                                self.confirmUser("PhotoRemoveGroupMessage",
                                    action: "PhotoRemove",
                                    cancel: "AlertCancel",
                                    sourceView: self.view,
                                    sourceRect: self.view.bounds,
                                    tapYes: { () -> () in
                                        Actor.removeGroupAvatarWithGid(jint(self.gid))
                                })
                            } else if (index >= 0) {
                                let takePhoto: Bool = (index == 0) && hasCamera
                                self.pickAvatar(takePhoto, closure: { (image) -> () in
                                    Actor.changeGroupAvatar(jint(self.gid), image: image)
                                })
                            }
                    })
                    
                    return true
                }
            }
            
            // Header: Change title
            s.action("GroupSetTitle") { (r) -> () in
                r.selectAction = { () -> Bool in
                    self.startEditField { (c) -> () in
                        
                        c.title = "GroupEditHeader"
                        
                        c.fieldHint = "GroupEditHint"
                        
                        c.actionTitle = "NavigationSave"
                        
                        c.initialText = self.group.getNameModel().get()
                        
                        c.didDoneTap = { (t, c) -> () in
                            
                            if t.length == 0 {
                                return
                            }

                            c.executeSafeOnlySuccess(Actor.editGroupTitleCommandWithGid(jint(self.gid), withTitle: t), successBlock: { (val) -> Void in
                                c.dismiss()
                            })
                        }
                    }
                    
                    return true
                }
            }
        }
        
        // Notifications
        section { (s) -> () in
            s.common { (r) -> () in
                let groupPeer: ACPeer! = ACPeer.groupWithInt(jint(self.gid))
                
                r.style = .Switch
                r.content = localized("GroupNotifications")
                
                r.bindAction = { (r) -> () in
                    r.switchOn = Actor.isNotificationsEnabledWithPeer(groupPeer)
                }
                
                r.switchAction = { (on: Bool) -> () in
                    Actor.changeNotificationsEnabledWithPeer(groupPeer, withValue: on)
                }
            }
        }
        
        // Members
        section { (s) -> () in
            
            s.autoSeparatorsInset = 65
            s.autoSeparatorTopOffset = 1
            s.headerHeight = 0
            
            // Members: Header
            s.header(localized("GroupMembers").uppercaseString)
            
            // Members: Add
            s.action("GroupAddParticipant") { (r) -> () in
                
                r.contentInset = 65
                
                r.selectAction = { () -> Bool in
                    let addParticipantController = AddParticipantViewController(gid: self.gid)
                    let navigationController = AANavigationController(rootViewController: addParticipantController)
                    if (isIPad) {
                        navigationController.modalInPopover = true
                        navigationController.modalPresentationStyle = UIModalPresentationStyle.CurrentContext
                    }
                    self.presentViewController(navigationController, animated: true, completion: nil)
                    return false
                }
            }
            
            // Members: List
            self.memberRows = s.arrays { (r: ACManagedArrayRows<ACGroupMember, GroupMemberCell>) -> () in
                r.height = 48
                r.data = self.group.members.get().toArray().toSwiftArray()
                r.data.sortInPlace(self.membersSort)
                
                r.bindData = { (c, d) -> () in
                    if let user = Actor.getUserWithUid(d.uid) {
                        c.bind(user, isAdmin: d.isAdministrator)
                        
                        // Notify to request onlines
                        Actor.onUserVisibleWithUid(d.uid)
                    }
                }
                
                r.selectAction = { (d) -> Bool in
                    if let user = Actor.getUserWithUid(d.uid) {
                        if (user.getId() == Actor.myUid()) {
                            return true
                        }
                        
                        let name = user.getNameModel().get()
                        
                        self.alertSheet { (a: AlertSetting) -> () in
                            
                            a.cancel = "AlertCancel"
                            
                            a.action("GroupMemberInfo") { () -> () in
                                self.navigateNext(UserViewController(uid: Int(user.getId())), removeCurrent: false)
                            }
                         
                            a.action("GroupMemberWrite") { () -> () in
                                self.navigateDetail(ConversationViewController(peer: ACPeer.userWithInt(user.getId())))
                                self.popover?.dismissPopoverAnimated(true)
                            }
                            
                            a.action("GroupMemberCall", closure: { () -> () in
                                let phones = user.getPhonesModel().get()
                                if phones.size() == 0 {
                                    self.alertUser("GroupMemberCallNoPhones")
                                } else if phones.size() == 1 {
                                    let number = phones.getWithInt(0)
                                    UIApplication.sharedApplication().openURL(NSURL(string: "telprompt://+\(number.phone)")!)
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
                                                UIApplication.sharedApplication().openURL(NSURL(string: "telprompt://+\(number.phone)")!)
                                            }
                                    })
                                }
                            })

                            // Detect if we are admin
                            let members: [ACGroupMember] = self.group.members.get().toArray().toSwiftArray()
                            var isAdmin = self.group.creatorId == Actor.myUid()
                            if !isAdmin {
                                for m in members {
                                    if m.uid == Actor.myUid() {
                                        isAdmin = m.isAdministrator
                                    }
                                }
                            }
                            
                            // Can mark as admin
                            let canMarkAdmin = isAdmin && !d.isAdministrator
                            
                            if canMarkAdmin {
                                a.action("GroupMemberMakeAdmin") { () -> () in
                                    
                                    self.confirmDestructive(localized("GroupMemberMakeMessage").replace("{name}", dest: name), action: localized("GroupMemberMakeAction")) {

                                        self.executeSafe(Actor.makeAdminCommandWithGid(jint(self.gid), withUid: jint(user.getId())))
                                    }
                                }
                            }
                            
                            // Can kick user
                            let canKick = isAdmin || d.inviterUid == Actor.myUid()
                            
                            if canKick {
                                a.destructive("GroupMemberKick") { () -> () in       
                                    self.confirmDestructive(localized("GroupMemberKickMessage")
                                        .replace("{name}", dest: name), action: localized("GroupMemberKickAction")) {

                                        self.executeSafe(Actor.kickMemberCommandWithGid(jint(self.gid), withUid: user.getId()))
                                    }
                                }
                            }
                        }
                    }
                    
                    return true
                }
            }
        }
        
        // Leave group
        section { (s) -> () in
            s.common({ (r) -> () in
                r.content = localized("GroupLeave")
                r.style = .DestructiveCentered
                r.selectAction = { () -> Bool in
                    
                    self.confirmDestructive(localized("GroupLeaveConfirm"), action: localized("GroupLeaveConfirmAction"), yes: { () -> () in
                        self.executeSafe(Actor.leaveGroupCommandWithGid(jint(self.gid)))
                    })
                    
                    return true
                }
            })
        }
    }
    
    override func tableWillBind(binder: Binder) {
        
        // Bind group info
        
        binder.bind(group.getNameModel()!) { (value: String?) -> () in
            self.headerRow.reload()
        }
        
        binder.bind(group.getAvatarModel()) { (value: ACAvatar?) -> () in
            self.headerRow.reload()
        }
        
        // Bind members
        
        binder.bind(group.getMembersModel()) { (value: JavaUtilHashSet?) -> () in
            if let v = value {
                self.memberRows.data = v.toArray().toSwiftArray()
                self.memberRows.data.sortInPlace(self.membersSort)
                self.memberRows.reload()
            }
        }
        
        // Bind membership status
        
        binder.bind(group.isMemberModel()) { (member: JavaLangBoolean?) -> () in
            if let m = member {
                if Bool(m.booleanValue()) == true {
                    self.hidePlaceholder()
                } else {
                    self.showPlaceholderWithImage(
                        UIImage(named: "contacts_list_placeholder"),
                        title: NSLocalizedString("Placeholder_Group_Title", comment: "Not a member Title"),
                        subtitle: NSLocalizedString("Placeholder_Group_Message", comment: "Message Title"))
                }
            } else {
                self.hidePlaceholder()
            }
        }
    }
}