//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class AAConversationGroupInfoController: AATableViewController {
    
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
            .setHeight(264)
        
        // Change Photo
        tableData.addSection()
            .setFooterHeight(15)
            .addActionCell("GroupSetPhoto", actionClosure: { () -> () in
                var hasCamera = UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera)
                self.showActionSheet( hasCamera ? ["PhotoCamera", "PhotoLibrary"] : ["PhotoLibrary"],
                    cancelButton: "AlertCancel",
                    destructButton: self.group?.getAvatarModel().get() != nil ? "PhotoRemove" : nil,
                    tapClosure: { (index) -> () in
                        if (index == -2) {
                            self.confirmUser("PhotoRemoveGroupMessage",
                                action: "PhotoRemove",
                                cancel: "AlertCancel",
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
                self.showActionSheet(["Open profile", "Write message", "Call"],
                    cancelButton: "Cancel",
                    destructButton: groupMember.getUid() != MSG.myUid() && (groupMember.getInviterUid() == MSG.myUid() || self.group!.getCreatorId() == MSG.myUid())  ? "Kick from group" : nil,
                    tapClosure: { (index) -> () in
                        if (index == -2) {
                            self.confirmUser("Are you sure want to kick <name> from group?", action: "Kick", cancel: "AlertCancel", tapYes: { () -> () in
                                self.execute(MSG.kickMemberCommandWithGid(jint(self.gid), withUid: user.getId()))
                            })
                        } else if (index >= 0) {
                            if (index == 0) {
                                self.navigateNext(AAUserInfoController(uid: Int(user.getId())), removeCurrent: false)
                            } else if (index == 1) {
                                self.navigateNext(AAConversationController(peer: AMPeer.userWithInt(user.getId())), removeCurrent: false)
                            }
                        }
                    })
            }
        }
        
        // Add member
        tableData.addSection()
            .setFooterHeight(15)
            .addActionCell("GroupAddParticipant", actionClosure: { () -> () in
                let addParticipantController = AAAddParticipantController(gid: self.gid)
                let navigationController = AANavigationController(rootViewController: addParticipantController)
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
                // TODO: Ask user
                self.execute(MSG.leaveGroupCommandWithGid(jint(self.gid)))
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
                cell.groupAvatarView.bind(self.group!.getNameModel().get(), id: jint(self.gid), avatar: value)
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
                    self.navigationItem.rightBarButtonItem = UIBarButtonItem(
                        title: NSLocalizedString("NavigationEdit", comment: "Edit Title"),
                        style: UIBarButtonItemStyle.Plain,
                        target: self, action: "editName")
                    
                    self.hidePlaceholder()
                } else {
                    self.navigationItem.rightBarButtonItem = nil
                    
                    self.showPlaceholderWithImage(
                        UIImage(named: "contacts_list_placeholder"),
                        title: NSLocalizedString("Placeholder_Group_Title", comment: "Not a member Title"),
                        subtitle: NSLocalizedString("Placeholder_Group_Message", comment: "Message Title"))
                }
            }
        })
    }
    
    func editName() {
        var alertView = UIAlertView(title: nil,
            message: NSLocalizedString("GroupEditHeader", comment: "Change name"),
            delegate: SwiftAlertDelegate(closure: { (alertView, index) -> () in
                if (index == 1) {
                    let textField = alertView.textFieldAtIndex(0)!
                    if count(textField.text) > 0 {
                        self.confirmUser("Are you sure want to change group title?",
                            action: "AlertSave",
                            cancel: "AlertCancel",
                            tapYes: { () -> () in
                                self.execute(MSG.editGroupTitleCommandWithGid(jint(self.gid), withTitle: textField.text));
                            })
                    }
                }
            }),
        cancelButtonTitle: NSLocalizedString("AlertCancel", comment: "Cancel titlte"))
        alertView.addButtonWithTitle(NSLocalizedString("AlertSave", comment: "Save titlte"))
        alertView.alertViewStyle = UIAlertViewStyle.PlainTextInput
        alertView.textFieldAtIndex(0)!.autocapitalizationType = UITextAutocapitalizationType.Words
        alertView.textFieldAtIndex(0)!.text = group!.getNameModel().get()
        alertView.textFieldAtIndex(0)!.keyboardAppearance = MainAppTheme.common.isDarkKeyboard ? UIKeyboardAppearance.Dark : UIKeyboardAppearance.Light
        alertView.show()
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
