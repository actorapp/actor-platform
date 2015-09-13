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
    private var groupMembers: IOSObjectArray?
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
        edgesForExtendedLayout = UIRectEdge.Top
        automaticallyAdjustsScrollViewInsets = false
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        tableView.clipsToBounds = false

        group = Actor.getGroupWithGid(jint(gid))
        
        tableData = UATableData(tableView: tableView)
        tableData.registerClass(GroupPhotoCell.self, forCellReuseIdentifier: GroupInfoCellIdentifier)
        tableData.registerClass(GroupMemberCell.self, forCellReuseIdentifier: UserCellIdentifier)
        tableData.tableScrollClosure = { (tableView: UITableView) -> () in
            self.applyScrollUi(tableView)
        }
        
        // Banner
        tableData.addSection()
            .addCustomCell { (tableView, indexPath) -> UITableViewCell in
                var cell = tableView.dequeueReusableCellWithIdentifier(
                    self.GroupInfoCellIdentifier,
                    forIndexPath: indexPath)
                    as! GroupPhotoCell
            
                cell.contentView.superview?.clipsToBounds = false
            
                cell.selectionStyle = UITableViewCellSelectionStyle.None
            
                self.applyScrollUi(tableView, cell: cell)
            
                return cell
            }
            .setHeight(avatarHeight)
        
        // Change Photo
        let adminSection = tableData.addSection(true)
            .setFooterHeight(15)
        
        adminSection.addActionCell("GroupSetPhoto", actionClosure: { () -> () in
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
        })
        
        adminSection
            .addActionCell("GroupSetTitle", actionClosure: { () -> () in
                self.editName()
            })
        
//        adminSection
//            .addNavigationCell("GroupIntegrations", actionClosure: { () -> () in
//                self.navigateNext(IntegrationViewController(gid: jint(self.gid)), removeCurrent: false)
//            })
        
        // Notifications
        tableData.addSection(true)
            .setHeaderHeight(15)
            .setFooterHeight(15)
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
            .setHeaderHeight(15)
        
        membersSection
            .addCustomCells(48, countClosure: { () -> Int in
            if self.groupMembers != nil {
                return Int(self.groupMembers!.length())
            }
            return 0
        }) { (tableView, index, indexPath) -> UITableViewCell in
            let cell: GroupMemberCell = tableView.dequeueReusableCellWithIdentifier(self.UserCellIdentifier, forIndexPath: indexPath) as! GroupMemberCell
            if let groupMember = self.groupMembers!.objectAtIndex(UInt(index)) as? ACGroupMember,
                let user = Actor.getUserWithUid(groupMember.getUid()) {
                    let username = user.getNameModel().get()
                    let avatar: ACAvatar? = user.getAvatarModel().get()
                    cell.userAvatarView.bind(username, id: user.getId(), avatar: avatar)
                    cell.setUsername(username)
            }
            return cell
        }.setAction { (index) -> () in
            if let groupMember = self.groupMembers!.objectAtIndex(UInt(index)) as? ACGroupMember, let user = Actor.getUserWithUid(groupMember.getUid()) {
                if (user.getId() == Actor.myUid()) {
                    return
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
        }
        
        // Add member
        membersSection
            .setFooterHeight(15)
            .addActionCell("GroupAddParticipant", actionClosure: { () -> () in
                let addParticipantController = AddParticipantViewController(gid: self.gid)
                let navigationController = AANavigationController(rootViewController: addParticipantController)
                if (isIPad) {
                    navigationController.modalInPopover = true
                    navigationController.modalPresentationStyle = UIModalPresentationStyle.CurrentContext
                }
                self.presentViewController(navigationController, animated: true, completion: nil)
            })
            .setLeftInset(65.0)
        
        // Leave group
        tableData.addSection(true)
            .setFooterHeight(15)
            .setHeaderHeight(15)
            .addActionCell("GroupLeave", actionClosure: { () -> () in
                self.confirmUser("GroupLeaveConfirm", action: "GroupLeaveConfirmAction", cancel: "AlertCancel",
                    sourceView: self.view,
                    sourceRect: self.view.bounds,
                    tapYes: { () -> () in
                    self.execute(Actor.leaveGroupCommandWithGid(jint(self.gid)))
                })
            })
            .setStyle(.DestructiveCentered)
        
        // Init table
        tableView.reloadData()
        
        // Bind group info
        binder.bind(group!.getNameModel()!, closure: { (value: String?) -> () in
            let cell: GroupPhotoCell? = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? GroupPhotoCell
            if cell != nil {
                cell!.setGroupName(value!)
            }
            self.title = value!
        })
        
        binder.bind(group!.getAvatarModel(), closure: { (value: ACAvatar?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? GroupPhotoCell {
                if (self.group!.isMemberModel().get().booleanValue()) {
                    cell.groupAvatarView.bind(self.group!.getNameModel().get(), id: jint(self.gid), avatar: value)
                } else {
                    cell.groupAvatarView.bind(self.group!.getNameModel().get(), id: jint(self.gid), avatar: nil)
                }
            }
        })

        // Bind members
        binder.bind(group!.getMembersModel(), closure: { (value: JavaUtilHashSet?) -> () in
            if value != nil {
                self.groupMembers = value!.toArray()
                
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
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        navigationController?.navigationBar.lt_reset()
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        applyScrollUi(tableView)
        navigationController?.navigationBar.shadowImage = UIImage()
    }
}
