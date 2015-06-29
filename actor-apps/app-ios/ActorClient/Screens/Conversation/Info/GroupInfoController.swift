//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class GroupInfoController: AATableViewController {
    
    private let GroupInfoCellIdentifier = "GroupInfoCellIdentifier"
    private let UserCellIdentifier = "UserCellIdentifier"
    private let CellIdentifier = "CellIdentifier"
    
    let gid: Int
    var group: AMGroupVM?
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

        group = MSG.getGroupWithGid(jint(gid))
        
        tableData = UATableData(tableView: tableView)
        tableData.registerClass(AAConversationGroupInfoCell.self, forCellReuseIdentifier: GroupInfoCellIdentifier)
        tableData.registerClass(AAConversationGroupInfoUserCell.self, forCellReuseIdentifier: UserCellIdentifier)
        tableData.tableScrollClosure = { (tableView: UITableView) -> () in
            self.applyScrollUi(tableView)
        }
        
        // Banner
        tableData.addSection()
            .addCustomCell { (tableView, indexPath) -> UITableViewCell in
                var cell = tableView.dequeueReusableCellWithIdentifier(
                    self.GroupInfoCellIdentifier,
                    forIndexPath: indexPath)
                    as! AAConversationGroupInfoCell
            
                cell.contentView.superview?.clipsToBounds = false
            
                cell.selectionStyle = UITableViewCellSelectionStyle.None
            
                self.applyScrollUi(tableView, cell: cell)
            
                return cell
            }
            .setHeight(Double(avatarHeight))
        
        // Change Photo
        var adminSection = tableData.addSection().setFooterHeight(15)
        
        adminSection
            .addActionCell("GroupSetPhoto", actionClosure: { () -> () in
                var hasCamera = UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera)
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
                                    MSG.removeGroupAvatarWithGid(jint(self.gid))
                                })
                        } else if (index >= 0) {
                            let takePhoto: Bool = (index == 0) && hasCamera
                            self.pickAvatar(takePhoto, closure: { (image) -> () in
                                MSG.changeGroupAvatar(jint(self.gid), image: image)
                            })
                        }
                    })
            })
            .setLeftInset(15.0)
            .showBottomSeparator(15.0)
            .hideTopSeparator()
        
        adminSection
            .addActionCell("GroupSetTitle", actionClosure: { () -> () in
                self.editName()
            })
            .setLeftInset(15.0)
            .showBottomSeparator(15.0)
            .hideTopSeparator()
        
        adminSection
            .addNavigationCell("GroupIntegrations", actionClosure: { () -> () in
                self.navigateNext(IntegrationController(gid: jint(self.gid)), removeCurrent: false)
            })
            .setLeftInset(15.0)
            .showBottomSeparator(0.0)
            .hideTopSeparator()
        
        // Notifications
        tableData.addSection()
            .setHeaderHeight(15)
            .setFooterHeight(15)
            .addCommonCell()
            .setStyle(AATableViewCellStyle.Switch)
            .setContent("GroupNotifications")
            .setLeftInset(15.0)
            .showTopSeparator(0.0)
            .showBottomSeparator(0.0)
            .setModificator { (cell) -> () in
                let groupPeer: AMPeer! = AMPeer.groupWithInt(jint(self.gid))
                cell.setSwitcherOn(MSG.isNotificationsEnabledWithPeer(groupPeer))
                
                cell.switchBlock = { (on: Bool) -> () in
                    MSG.changeNotificationsEnabledWithPeer(groupPeer, withValue: on)
                }
            }
        
        // Members
        tableData.addSection()
            .setHeaderHeight(15)
            .addCustomCells(48, countClosure: { () -> Int in
            if self.groupMembers != nil {
                return Int(self.groupMembers!.length())
            }
            return 0
        }) { (tableView, index, indexPath) -> UITableViewCell in
            var cell: AAConversationGroupInfoUserCell = tableView.dequeueReusableCellWithIdentifier(self.UserCellIdentifier, forIndexPath: indexPath) as! AAConversationGroupInfoUserCell
            
            if let groupMember = self.groupMembers!.objectAtIndex(UInt(index)) as? AMGroupMember,
                let user = MSG.getUserWithUid(groupMember.getUid()) {
                    var username = user.getNameModel().get()
                    let avatar: AMAvatar? = user.getAvatarModel().get()
                    
                    cell.userAvatarView.bind(username, id: user.getId(), avatar: avatar)
                    
                    cell.setUsername(username)
            }
            
            cell.setLeftInset(65.0)
            
            cell.showBottomSeparator()
            cell.setBottomSeparatorLeftInset(15.0)
            
            if index == 0 {
                cell.showTopSeparator()
                cell.setTopSeparatorLeftInset(0.0)
            }
            
            return cell
        }.setAction { (index) -> () in
            if let groupMember = self.groupMembers!.objectAtIndex(UInt(index)) as? AMGroupMember, let user = MSG.getUserWithUid(groupMember.getUid()) {
                if (user.getId() == MSG.myUid()) {
                    return
                }
                
                var name = user.getNameModel().get()
                self.showActionSheet(name,
                    buttons: isIPhone ? ["GroupMemberInfo", "GroupMemberWrite", "GroupMemberCall"] : ["GroupMemberInfo", "GroupMemberWrite"],
                    cancelButton: "Cancel",
                    destructButton: groupMember.getUid() != MSG.myUid() && (groupMember.getInviterUid() == MSG.myUid() || self.group!.getCreatorId() == MSG.myUid())  ? "GroupMemberKick" : nil,
                    sourceView: self.view,
                    sourceRect: self.view.bounds,
                    tapClosure: { (index) -> () in
                        if (index == -2) {
                            self.confirmUser(NSLocalizedString("GroupMemberKickMessage", comment: "Button Title").stringByReplacingOccurrencesOfString("{name}", withString: name, options: NSStringCompareOptions.allZeros, range: nil),
                                action: "GroupMemberKickAction",
                                cancel: "AlertCancel",
                                sourceView: self.view,
                                sourceRect: self.view.bounds,
                                tapYes: { () -> () in
                                    self.execute(MSG.kickMemberCommandWithGid(jint(self.gid), withUid: user.getId()))
                                })
                        } else if (index >= 0) {
                            if (index == 0) {
                                self.navigateNext(UserInfoController(uid: Int(user.getId())), removeCurrent: false)
                            } else if (index == 1) {
                                self.navigateDetail(ConversationController(peer: AMPeer.userWithInt(user.getId())))
                                self.popover?.dismissPopoverAnimated(true)
                            } else if (index == 2) {
                                var phones = user.getPhonesModel().get()
                                if phones.size() == 0 {
                                    self.alertUser("GroupMemberCallNoPhones")
                                } else if phones.size() == 1 {
                                    var number = phones.getWithInt(0)
                                    UIApplication.sharedApplication().openURL(NSURL(string: "telprompt://+\(number.getPhone())")!)
                                } else {
                                    var numbers = [String]()
                                    for i in 0..<phones.size() {
                                        var p = phones.getWithInt(i)
                                        numbers.append("\(p.getTitle()): +\(p.getPhone())")
                                    }
                                    self.showActionSheet(numbers,
                                        cancelButton: "AlertCancel",
                                        destructButton: nil,
                                        sourceView: self.view,
                                        sourceRect: self.view.bounds,
                                        tapClosure: { (index) -> () in
                                        if (index >= 0) {
                                            var number = phones.getWithInt(jint(index))
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
        tableData.addSection()
            .setFooterHeight(15)
            .addActionCell("GroupAddParticipant", actionClosure: { () -> () in
                let addParticipantController = AddParticipantController(gid: self.gid)
                let navigationController = AANavigationController(rootViewController: addParticipantController)
                if (isIPad) {
                    navigationController.modalInPopover = true
                    navigationController.modalPresentationStyle = UIModalPresentationStyle.CurrentContext
                }
                self.presentViewController(navigationController, animated: true, completion: nil)
            })
            .setLeftInset(65.0)
            .showBottomSeparator(0.0)
            .hideTopSeparator()
        
        // Leave group
        tableData.addSection()
            .setFooterHeight(15)
            .setHeaderHeight(15)
            .addActionCell("GroupLeave", actionClosure: { () -> () in
                self.confirmUser("GroupLeaveConfirm", action: "GroupLeaveConfirmAction", cancel: "AlertCancel",
                    sourceView: self.view,
                    sourceRect: self.view.bounds,
                    tapYes: { () -> () in
                    self.execute(MSG.leaveGroupCommandWithGid(jint(self.gid)))
                })
            }).setLeftInset(15)
            .showBottomSeparator(0.0)
            .showTopSeparator(0.0)
            .setStyle(AATableViewCellStyle.DestructiveCentered)
        
        // Init table
        tableView.reloadData()
        
        // Bind group info
        binder.bind(group!.getNameModel()!, closure: { (value: String?) -> () in
            var cell: AAConversationGroupInfoCell? = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AAConversationGroupInfoCell
            if cell != nil {
                cell!.setGroupName(value!)
            }
            self.title = value!
        })
        
        binder.bind(group!.getAvatarModel(), closure: { (value: AMAvatar?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AAConversationGroupInfoCell {
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
                    
                    if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AAConversationGroupInfoCell {
                        cell.groupAvatarView.bind(self.group!.getNameModel().get(), id: jint(self.gid), avatar: nil)
                    }
                }
            }
        })
    }
    
    func editName() {
        textInputAlert("GroupEditHeader", content: group!.getNameModel().get(), action: "AlertSave") { (nval) -> () in
            if count(nval) > 0 {
                self.confirmUser("GroupEditConfirm",
                    action: "GroupEditConfirmAction",
                    cancel: "AlertCancel",
                    sourceView: self.view,
                    sourceRect: self.view.bounds,
                    tapYes: { () -> () in
                        self.execute(MSG.editGroupTitleCommandWithGid(jint(self.gid), withTitle: nval));
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
