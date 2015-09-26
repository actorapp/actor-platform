//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class GroupViewController: AATableViewController {
    
    private let GroupInfoCellIdentifier = "GroupInfoCellIdentifier"
    private let UserCellIdentifier = "UserCellIdentifier"
    private let CellIdentifier = "CellIdentifier"
    
    let gid: Int
    var group: ACGroupVM?
    var binder = Binder()
    
    private var tableData: UATableData!
    private var groupMembers = [ACGroupMember]()
    private var groupNameTextField: UITextField?
    
    init (gid: Int) {
        self.gid = gid
        super.init(style: UITableViewStyle.Plain)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = MainAppTheme.list.bgColor
        
        title = localized("ProfileTitle")
        
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        tableView.backgroundColor = MainAppTheme.list.backyardColor

        group = Actor.getGroupWithGid(jint(gid))
        
        tableData = UATableData(tableView: tableView)
        tableData.registerClass(AvatarCell.self, forCellReuseIdentifier: GroupInfoCellIdentifier)
        tableData.registerClass(GroupMemberCell.self, forCellReuseIdentifier: UserCellIdentifier)
        
        // Banner
        var section = tableData.addSection(true)
            .setFooterHeight(15)
        
        section.addCustomCell { (tableView, indexPath) -> UITableViewCell in
            var cell = tableView.dequeueReusableCellWithIdentifier(self.GroupInfoCellIdentifier, forIndexPath: indexPath) as! AvatarCell
            cell.selectionStyle = UITableViewCellSelectionStyle.None
            cell.subtitleLabel.hidden = true
            
            cell.didTap = { () -> () in
                let avatar = self.group!.getAvatarModel().get()
                if avatar != nil && avatar.getFullImage() != nil {
                    
                    let full = avatar.getFullImage().getFileReference()
                    let small = avatar.getSmallImage().getFileReference()
                    let size = CGSize(width: Int(avatar.getFullImage().getWidth()), height: Int(avatar.getFullImage().getHeight()))
                    
                    self.presentViewController(PhotoPreviewController(file: full, previewFile: small, size: size, fromView: cell.avatarView), animated: true, completion: nil)
                }
            }
            
            return cell
        }
        .setHeight(92)
        
        // Change Photo
        section.addActionCell("GroupSetPhoto", actionClosure: { () -> Bool in
            let hasCamera = UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera)
            self.showActionSheet( hasCamera ? ["PhotoCamera", "PhotoLibrary"] : ["PhotoLibrary"],
                cancelButton: "AlertCancel",
                destructButton: self.group?.getAvatarModel().get() != nil ? "PhotoRemove" : nil,
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
        })
        
        section.addActionCell("GroupSetTitle", actionClosure: { () -> Bool in
                self.editName()
                return true
        })
        
//        adminSection
//            .addNavigationCell("GroupIntegrations", actionClosure: { () -> () in
//                self.navigateNext(IntegrationViewController(gid: jint(self.gid)), removeCurrent: false)
//            })
        
        // Notifications
        tableData.addSection(true)
            .setHeaderHeight(15)
            .addCommonCell()
                .setStyle(.Switch)
                .setContent("GroupNotifications")
                .setModificator { (cell) -> () in
                    let groupPeer: ACPeer! = ACPeer.groupWithInt(jint(self.gid))
                    cell.setSwitcherOn(Actor.isNotificationsEnabledWithPeer(groupPeer))
                
                    cell.switchBlock = { (on: Bool) -> () in
                        Actor.changeNotificationsEnabledWithPeer(groupPeer, withValue: on)
                    }
                }
        
        // Members
        
        let membersSection = tableData.addSection(true)
        
        membersSection.addHeaderCell()
            .setTitle(localized("GroupMembers"))
        
        membersSection
            .addCustomCells(48, countClosure: { () -> Int in
            return self.groupMembers.count
        }) { (tableView, index, indexPath) -> UITableViewCell in
            let cell: GroupMemberCell = tableView.dequeueReusableCellWithIdentifier(self.UserCellIdentifier, forIndexPath: indexPath) as! GroupMemberCell
            let groupMember = self.groupMembers[index]
            if let user = Actor.getUserWithUid(groupMember.getUid()) {
                    cell.bind(user)
                    
                    // Notify to request onlines
                    Actor.onUserVisibleWithUid(groupMember.getUid())
            }
            return cell
        }.setAction { (index) -> Bool in
            let groupMember = self.groupMembers[index]
            if let user = Actor.getUserWithUid(groupMember.getUid()) {
                if (user.getId() == Actor.myUid()) {
                    return true
                }
                
                let name = user.getNameModel().get()
                self.showActionSheet(name,
                    buttons: isIPhone ? ["GroupMemberInfo", "GroupMemberWrite", "GroupMemberCall"] : ["GroupMemberInfo", "GroupMemberWrite"],
                    cancelButton: "Cancel",
                    destructButton: groupMember.getUid() != Actor.myUid() && (groupMember.getInviterUid() == Actor.myUid() || self.group!.getCreatorId() == Actor.myUid())  ? "GroupMemberKick" : nil,
                    sourceView: self.view,
                    sourceRect: self.view.bounds,
                    tapClosure: { (index) -> () in
                        if (index == -2) {
                            self.confirmUser(NSLocalizedString("GroupMemberKickMessage", comment: "Button Title").stringByReplacingOccurrencesOfString("{name}", withString: name, options: NSStringCompareOptions(), range: nil),
                                action: "GroupMemberKickAction",
                                cancel: "AlertCancel",
                                sourceView: self.view,
                                sourceRect: self.view.bounds,
                                tapYes: { () -> () in
                                    self.execute(Actor.kickMemberCommandWithGid(jint(self.gid), withUid: user.getId()))
                                })
                        } else if (index >= 0) {
                            if (index == 0) {
                                self.navigateNext(UserViewController(uid: Int(user.getId())), removeCurrent: false)
                            } else if (index == 1) {
                                self.navigateDetail(ConversationViewController(peer: ACPeer.userWithInt(user.getId())))
                                self.popover?.dismissPopoverAnimated(true)
                            } else if (index == 2) {
                                let phones = user.getPhonesModel().get()
                                if phones.size() == 0 {
                                    self.alertUser("GroupMemberCallNoPhones")
                                } else if phones.size() == 1 {
                                    let number = phones.getWithInt(0)
                                    UIApplication.sharedApplication().openURL(NSURL(string: "telprompt://+\(number.getPhone())")!)
                                } else {
                                    var numbers = [String]()
                                    for i in 0..<phones.size() {
                                        let p = phones.getWithInt(i)
                                        numbers.append("\(p.getTitle()): +\(p.getPhone())")
                                    }
                                    self.showActionSheet(numbers,
                                        cancelButton: "AlertCancel",
                                        destructButton: nil,
                                        sourceView: self.view,
                                        sourceRect: self.view.bounds,
                                        tapClosure: { (index) -> () in
                                        if (index >= 0) {
                                            let number = phones.getWithInt(jint(index))
                                            UIApplication.sharedApplication().openURL(NSURL(string: "telprompt://+\(number.getPhone())")!)
                                        }
                                    })
                                }
                            }
                        }
                    })    
            }
            
            return true
        }
        
        // Add member
        membersSection
            .setFooterHeight(15)
            .addActionCell("GroupAddParticipant", actionClosure: { () -> Bool in
                let addParticipantController = AddParticipantViewController(gid: self.gid)
                let navigationController = AANavigationController(rootViewController: addParticipantController)
                if (isIPad) {
                    navigationController.modalInPopover = true
                    navigationController.modalPresentationStyle = UIModalPresentationStyle.CurrentContext
                }
                self.presentViewController(navigationController, animated: true, completion: nil)
                
                return false
            })
            .setLeftInset(65.0)
        
        // Leave group
        tableData.addSection(true)
            .setFooterHeight(15)
            .setHeaderHeight(15)
            .addActionCell("GroupLeave", actionClosure: { () -> Bool in
                self.confirmUser("GroupLeaveConfirm", action: "GroupLeaveConfirmAction", cancel: "AlertCancel",
                    sourceView: self.view,
                    sourceRect: self.view.bounds,
                    tapYes: { () -> () in
                    self.execute(Actor.leaveGroupCommandWithGid(jint(self.gid)))
                })
                return true
            })
            .setStyle(.DestructiveCentered)
        
        // Init table
        tableView.reloadData()
        
        // Bind group info
        binder.bind(group!.getNameModel()!, closure: { (value: String?) -> () in
            let cell: AvatarCell? = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AvatarCell
            if cell != nil {
                cell!.titleLabel.text = value!
            }
        })
        
        binder.bind(group!.getAvatarModel(), closure: { (value: ACAvatar?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AvatarCell {
                if (self.group!.isMemberModel().get().booleanValue()) {
                    cell.avatarView.bind(self.group!.getNameModel().get(), id: jint(self.gid), avatar: value)
                } else {
                    cell.avatarView.bind(self.group!.getNameModel().get(), id: jint(self.gid), avatar: nil)
                }
            }
        })

        // Bind members
        binder.bind(group!.getMembersModel(), closure: { (value: JavaUtilHashSet?) -> () in
            if value != nil {
                self.groupMembers = value!.toArray().toSwiftArray()
                self.groupMembers.sortInPlace({ (left: ACGroupMember, right: ACGroupMember) -> Bool in
                    let lname = Actor.getUserWithUid(left.getUid()).getNameModel().get()
                    let rname = Actor.getUserWithUid(right.getUid()).getNameModel().get()
                    return lname < rname
                })
                
                self.tableView.reloadData()
            }
        })

        // Bind membership status
        binder.bind(group!.isMemberModel(), closure: { (member: JavaLangBoolean?) -> () in
            if member != nil {
                if Bool(member!.booleanValue()) == true {
                    self.hidePlaceholder()
                } else {
                    self.showPlaceholderWithImage(
                        UIImage(named: "contacts_list_placeholder"),
                        title: NSLocalizedString("Placeholder_Group_Title", comment: "Not a member Title"),
                        subtitle: NSLocalizedString("Placeholder_Group_Message", comment: "Message Title"))
                    
                    if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? GroupPhotoCell {
                        cell.groupAvatarView.bind(self.group!.getNameModel().get(), id: jint(self.gid), avatar: nil)
                    }
                }
            }
        })
    }
    
    func editName() {
        textInputAlert("GroupEditHeader", content: group!.getNameModel().get(), action: "AlertSave") { (nval) -> () in
            if nval.length > 0 {
                self.confirmUser("GroupEditConfirm",
                    action: "GroupEditConfirmAction",
                    cancel: "AlertCancel",
                    sourceView: self.view,
                    sourceRect: self.view.bounds,
                    tapYes: { () -> () in
                        self.execute(Actor.editGroupTitleCommandWithGid(jint(self.gid), withTitle: nval));
                })
            }

        }
    }
}
