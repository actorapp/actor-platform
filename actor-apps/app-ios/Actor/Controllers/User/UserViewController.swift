//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class UserViewController: AATableViewController {
    
    private let UserInfoCellIdentifier = "UserInfoCellIdentifier"
    private let TitledCellIdentifier = "TitledCellIdentifier"
    
    let uid: Int
    var user: AMUserVM?
    var phones: JavaUtilArrayList?
    var binder = Binder()
    
    var tableData: UATableData!
    
    init(uid: Int) {
        self.uid = uid
        super.init(style: UITableViewStyle.Plain)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        user = MSG.getUserWithUid(jint(uid))
        
        self.edgesForExtendedLayout = UIRectEdge.Top
        self.automaticallyAdjustsScrollViewInsets = false
        
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        tableView.clipsToBounds = false
        tableView.tableFooterView = UIView()
        
        tableData = UATableData(tableView: tableView)
        tableData.registerClass(UserPhotoCell.self, forCellReuseIdentifier: UserInfoCellIdentifier)
        tableData.registerClass(TitledCell.self, forCellReuseIdentifier: TitledCellIdentifier)
        tableData.tableScrollClosure = { (tableView: UITableView) -> () in
            self.applyScrollUi(tableView)
        }
        
        // Avatar
        tableData.addSection().addCustomCell { (tableView, indexPath) -> UITableViewCell in
            var cell: UserPhotoCell = tableView.dequeueReusableCellWithIdentifier(self.UserInfoCellIdentifier, forIndexPath: indexPath) as! UserPhotoCell
            cell.contentView.superview?.clipsToBounds = false
            if self.user != nil {
                cell.setUsername(self.user!.getNameModel().get())
            }
            cell.setLeftInset(15.0)
            
            self.applyScrollUi(tableView, cell: cell)
            
            return cell
            }.setHeight(Double(avatarHeight))
        
        // Send Message
        if (!user!.isBot().boolValue) {
            tableData.addSection()
                .addActionCell("ProfileSendMessage", actionClosure: { () -> () in
                    self.navigateDetail(ConversationViewController(peer: AMPeer.userWithInt(jint(self.uid))))
                    self.popover?.dismissPopoverAnimated(true)
                })
                .showTopSeparator(0)
                .showBottomSeparator(15)
        }
        
        // Phones
        tableData.addSection()
            .setFooterHeight(15)
            .addCustomCells(55, countClosure: { () -> Int in
                if (self.phones != nil) {
                    return Int(self.phones!.size())
                }
                return 0
                }) { (tableView, index, indexPath) -> UITableViewCell in
                    var cell: TitledCell = tableView.dequeueReusableCellWithIdentifier(self.TitledCellIdentifier, forIndexPath: indexPath) as! TitledCell
                    
                    cell.setLeftInset(15.0)
                    
                    if let phone = self.phones!.getWithInt(jint(index)) as? AMUserPhone {
                        cell.setTitle(phone.getTitle(), content: "+\(phone.getPhone())")
                    }
                    
                    cell.hideTopSeparator()
                    cell.showBottomSeparator()
                    
                    var phonesCount = Int(self.phones!.size());
                    if index == phonesCount - 1 {
                        cell.setBottomSeparatorLeftInset(0.0)
                    } else {
                        cell.setBottomSeparatorLeftInset(15.0)
                    }
                    
                    return cell
            }.setAction { (index) -> () in
                var phoneNumber = (self.phones?.getWithInt(jint(index)).getPhone())!
                var hasPhone = UIApplication.sharedApplication().canOpenURL(NSURL(string: "tel://")!)
                if (!hasPhone) {
                    UIPasteboard.generalPasteboard().string = "+\(phoneNumber)"
                    self.alertUser("NumberCopied")
                } else {
                    self.showActionSheet(["CallNumber", "CopyNumber"],
                        cancelButton: "AlertCancel",
                        destructButton: nil,
                        sourceView: self.view,
                        sourceRect: self.view.bounds,
                        tapClosure: { (index) -> () in
                            if (index == 0) {
                                UIApplication.sharedApplication().openURL(NSURL(string: "tel://+\(phoneNumber)")!)
                            } else if index == 1 {
                                UIPasteboard.generalPasteboard().string = "+\(phoneNumber)"
                                self.alertUser("NumberCopied")
                            }
                    })
                }
        }
        
        tableData.addSection()
            .setHeaderHeight(15)
            .setFooterHeight(15)
            .addCommonCell { (cell) -> () in
                let peer = AMPeer.userWithInt(jint(self.uid))
                cell.setSwitcherOn(MSG.isNotificationsEnabledWithPeer(peer))
                cell.switchBlock = { (on: Bool) -> () in
                    if !on && !self.user!.isBot().boolValue {
                        self.confirmAlertUser("ProfileNotificationsWarring",
                            action: "ProfileNotificationsWarringAction",
                            tapYes: { () -> () in
                                MSG.changeNotificationsEnabledWithPeer(peer, withValue: false)
                            }, tapNo: { () -> () in
                                cell.setSwitcherOn(true, animated: true)
                            })
                        return
                    }
                    MSG.changeNotificationsEnabledWithPeer(peer, withValue: on)
                }
            }
            .setContent("ProfileNotifications")
            .setStyle(.Switch)
        
        var contactSection = tableData.addSection()
            .setHeaderHeight(15)
            .setFooterHeight(15)

       contactSection
            .addCommonCell { (cell) -> () in
                if (self.user!.isContactModel().get().booleanValue()) {
                    cell.setContent(NSLocalizedString("ProfileRemoveFromContacts", comment: "Remove From Contacts"))
                    cell.style = .Destructive
                } else {
                    cell.setContent(NSLocalizedString("ProfileAddToContacts", comment: "Add To Contacts"))
                    cell.style = .Blue
                }
            }
            .setAction { () -> () in
                if (self.user!.isContactModel().get().booleanValue()) {
                    self.execute(MSG.removeContactCommandWithUid(jint(self.uid)))
                } else {
                    self.execute(MSG.addContactCommandWithUid(jint(self.uid)))
                }
            }
            .hideBottomSeparator()
        
        // Rename
        contactSection
            .addActionCell("ProfileRename", actionClosure: { () -> () in
                if (!MSG.isRenameHintShown()) {
                    self.confirmAlertUser("ProfileRenameMessage",
                        action: "ProfileRenameAction",
                        tapYes: { () -> () in
                            self.renameUser()
                        })
                } else {
                    self.renameUser()
                }
            })
            .showTopSeparator(15)
        
        // Binding data
        tableView.reloadData()
        
        binder.bind(user!.getAvatarModel(), closure: { (value: AMAvatar?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? UserPhotoCell {
                cell.userAvatarView.bind(self.user!.getNameModel().get(), id: jint(self.uid), avatar: value)
            }
        })

        binder.bind(user!.getNameModel(), closure: { ( name: String?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? UserPhotoCell {
                cell.setUsername(name!)
            }
            self.title = name!
        })

        binder.bind(user!.getPresenceModel(), closure: { (presence: AMUserPresence?) -> () in
            var presenceText = MSG.getFormatter().formatPresence(presence, withSex: self.user!.getSex())
            if presenceText != nil {
                if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? UserPhotoCell {
                    cell.setPresence(presenceText)
                }
            }
        })
        
        binder.bind(user!.getPhonesModel(), closure: { (phones: JavaUtilArrayList?) -> () in
            if phones != nil {
                self.phones = phones
                self.tableView.reloadData()
            }
        })
        
        binder.bind(user!.isContactModel(), closure: { (contect: AMValueModel?) -> () in
            self.tableView.reloadSections(NSIndexSet(index: contactSection.index), withRowAnimation: UITableViewRowAnimation.None)
        })
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        applyScrollUi(tableView)
        navigationController?.navigationBar.shadowImage = UIImage()
        MSG.onProfileOpenWithUid(jint(uid))
    }
    
    override func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated)
        MSG.onProfileClosedWithUid(jint(uid))
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        navigationController?.navigationBar.lt_reset()
    }
    
    func renameUser() {
        textInputAlert("ProfileEditHeader",
            content: self.user!.getNameModel().get(),
            action: "AlertSave",
            tapYes: { (nval) -> () in
                if count(nval) > 0 {
                    self.execute(MSG.editNameCommandWithUid(jint(self.uid), withName: nval))
                }
        })
    }
}
