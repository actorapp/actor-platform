//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class UserViewController: AATableViewController {
    
    private let UserInfoCellIdentifier = "UserInfoCellIdentifier"
    private let TitledCellIdentifier = "TitledCellIdentifier"
    
    let uid: Int
    var user: ACUserVM?
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
        
        user = Actor.getUserWithUid(jint(uid))
        
        title = localized("ProfileTitle")
        
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        
        tableData = UATableData(tableView: tableView)
        tableData.registerClass(AvatarCell.self, forCellReuseIdentifier: UserInfoCellIdentifier)
        tableData.registerClass(TitledCell.self, forCellReuseIdentifier: TitledCellIdentifier)
        
        // Top section
        let contactsSection = tableData
            .addSection(true)
            .setFooterHeight(15)
        
        contactsSection.addCustomCell { (tableView, indexPath) -> UITableViewCell in
            let cell = tableView.dequeueReusableCellWithIdentifier(self.UserInfoCellIdentifier, forIndexPath: indexPath) as! AvatarCell
            cell.selectionStyle = .None
            if self.user != nil {
                cell.titleLabel.text = self.user!.getNameModel().get()
                cell.avatarView.bind(self.user!.getNameModel().get(), id: self.user!.getId(), avatar: self.user!.getAvatarModel().get())
            }
            return cell
            }.setHeight(92)

        
        let nick = user!.getNickModel().get()
        if nick != nil {
            contactsSection
                .addTitledCell(localized("ProfileUsername"), text: "@\(nick)")
        }
        
        
        // Phones
        contactsSection
            .addCustomCells(55, countClosure: { () -> Int in
                if (self.phones != nil) {
                    return Int(self.phones!.size())
                }
                return 0
                }) { (tableView, index, indexPath) -> UITableViewCell in
                    let cell: TitledCell = tableView.dequeueReusableCellWithIdentifier(self.TitledCellIdentifier, forIndexPath: indexPath) as! TitledCell
                    if let phone = self.phones!.getWithInt(jint(index)) as? ACUserPhone {
                        cell.setTitle(phone.getTitle(), content: "+\(phone.getPhone())")
                    }
                    return cell
            }.setAction { (index) -> Bool in
                let phoneNumber = (self.phones?.getWithInt(jint(index)).getPhone())!
                let hasPhone = UIApplication.sharedApplication().canOpenURL(NSURL(string: "telprompt://")!)
                if (!hasPhone) {
                    UIPasteboard.generalPasteboard().string = "+\(phoneNumber)"
                    self.alertUser("NumberCopied")
                } else {
                    UIApplication.sharedApplication().openURL(NSURL(string: "telprompt://+\(phoneNumber)")!)
                }
                return true
            }.setCanCopy(true)
        
        let about = user!.getAboutModel().get()
        if about != nil {
            contactsSection
                .addTextCell(localized("ProfileAbout"), text: about)
                .setCopy(about)
        }
        
        
        // Send Message
        if (!user!.isBot().boolValue) {
            tableData.addSection(true)
                .setHeaderHeight(15)
                .setFooterHeight(15)
                .addActionCell("ProfileSendMessage", actionClosure: { () -> Bool in
                    self.navigateDetail(ConversationViewController(peer: ACPeer.userWithInt(jint(self.uid))))
                    self.popover?.dismissPopoverAnimated(true)
                    return false
                })
        }
        
        tableData.addSection()
            .setHeaderHeight(15)
            .setFooterHeight(15)
            .addCommonCell { (cell) -> () in
                let peer = ACPeer.userWithInt(jint(self.uid))
                cell.setSwitcherOn(Actor.isNotificationsEnabledWithPeer(peer))
                cell.switchBlock = { (on: Bool) -> () in
                    if !on && !self.user!.isBot().boolValue {
                        self.confirmAlertUser("ProfileNotificationsWarring",
                            action: "ProfileNotificationsWarringAction",
                            tapYes: { () -> () in
                                Actor.changeNotificationsEnabledWithPeer(peer, withValue: false)
                            }, tapNo: { () -> () in
                                cell.setSwitcherOn(true, animated: true)
                            })
                        return
                    }
                    Actor.changeNotificationsEnabledWithPeer(peer, withValue: on)
                }
            }
            .setContent("ProfileNotifications")
            .setStyle(.Switch)
        
        let contactSection = tableData.addSection(true)
            .setHeaderHeight(15)
            .setFooterHeight(15)

       contactSection
            .addCommonCell { (cell) -> () in
                if (self.user!.isContactModel().get().booleanValue()) {
                    cell.setContent(NSLocalizedString("ProfileRemoveFromContacts", comment: "Remove From Contacts"))
                    cell.style = .Destructive
                } else {
                    cell.setContent(NSLocalizedString("ProfileAddToContacts", comment: "Add To Contacts"))
                    cell.style = .Action
                }
            }
            .setAction { () -> Bool in
                if (self.user!.isContactModel().get().booleanValue()) {
                    self.execute(Actor.removeContactCommandWithUid(jint(self.uid)))
                } else {
                    self.execute(Actor.addContactCommandWithUid(jint(self.uid)))
                }
                return true
            }
        
        // Rename
        contactSection
            .addActionCell("ProfileRename", actionClosure: { () -> Bool in
                if (!Actor.isRenameHintShown()) {
                    self.confirmAlertUser("ProfileRenameMessage",
                        action: "ProfileRenameAction",
                        tapYes: { () -> () in
                            self.renameUser()
                        })
                } else {
                    self.renameUser()
                }
                return true
            })
        
        // Binding data
        tableView.reloadData()
        
        binder.bind(user!.getAvatarModel(), closure: { (value: ACAvatar?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AvatarCell {
                cell.avatarView.bind(self.user!.getNameModel().get(), id: jint(self.uid), avatar: value)
            }
        })

        binder.bind(user!.getNameModel(), closure: { ( name: String?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AvatarCell {
                cell.titleLabel.text = name!
            }
        })

        binder.bind(user!.getPresenceModel(), closure: { (presence: ACUserPresence?) -> () in
            let presenceText = Actor.getFormatter().formatPresence(presence, withSex: self.user!.getSex())
            if presenceText != nil {
                if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AvatarCell {
                    
                    if presence!.getState().ordinal() == jint(ACUserPresence_State.ONLINE.rawValue) {
                        cell.subtitleLabel.applyStyle("user.online")
                    } else {
                        cell.subtitleLabel.applyStyle("user.offline")
                    }
                    
                    cell.subtitleLabel.text = presenceText
                }
            }
        })
        
        binder.bind(user!.getPhonesModel(), closure: { (phones: JavaUtilArrayList?) -> () in
            if phones != nil {
                self.phones = phones
                self.tableView.reloadData()
            }
        })
        
        binder.bind(user!.isContactModel(), closure: { (contect: ARValueModel?) -> () in
            self.tableView.reloadSections(NSIndexSet(index: contactSection.index), withRowAnimation: UITableViewRowAnimation.None)
        })
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
//        applyScrollUi(tableView)
//        navigationController?.navigationBar.shadowImage = UIImage()
        Actor.onProfileOpenWithUid(jint(uid))
    }
    
    override func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated)
        Actor.onProfileClosedWithUid(jint(uid))
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
//        navigationController?.navigationBar.lt_reset()
    }
    
    func renameUser() {
        textInputAlert("ProfileEditHeader",
            content: self.user!.getNameModel().get(),
            action: "AlertSave",
            tapYes: { (nval) -> () in
                if nval.length > 0 {
                    self.execute(Actor.editNameCommandWithUid(jint(self.uid), withName: nval))
                }
        })
    }
}
