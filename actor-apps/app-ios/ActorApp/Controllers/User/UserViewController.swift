//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class UserViewController: AATableViewController {
    
    private let UserInfoCellIdentifier = "UserInfoCellIdentifier"
    private let TitledCellIdentifier = "TitledCellIdentifier"
    
    let uid: Int
    var user: ACUserVM?
    var isBot: Bool!
    var phones: ACArrayListUserPhone?
    var binder = Binder()
    
    var tableData: ACManagedTable!
    
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
        isBot = user!.isBot().boolValue
        
        title = localized("ProfileTitle")
        
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        
        tableData = ACManagedTable(tableView: tableView, controller: self)
        tableData.registerClass(AvatarCell.self, forCellReuseIdentifier: UserInfoCellIdentifier)
        tableData.registerClass(TitledCell.self, forCellReuseIdentifier: TitledCellIdentifier)
        
        // Top section
        var section = tableData
            .addSection(true)
            .setFooterHeight(15)
        
        section.addCustomCell { (tableView, indexPath) -> UITableViewCell in
            let cell = tableView.dequeueReusableCellWithIdentifier(self.UserInfoCellIdentifier, forIndexPath: indexPath) as! AvatarCell
            cell.selectionStyle = .None
            if self.user != nil {
                cell.titleLabel.text = self.user!.getNameModel().get()
                cell.avatarView.bind(self.user!.getNameModel().get(), id: self.user!.getId(), avatar: self.user!.getAvatarModel().get())
            }
            
            if self.isBot! {
                cell.subtitleLabel.hidden = true
            }
            
            cell.didTap = { () -> () in
                let avatar = self.user!.getAvatarModel().get()
                if avatar != nil && avatar.fullImage != nil {
                    
                    let full = avatar.fullImage.fileReference
                    let small = avatar.smallImage.fileReference
                    let size = CGSize(width: Int(avatar.fullImage.width), height: Int(avatar.fullImage.height))
                    
                    self.presentViewController(PhotoPreviewController(file: full, previewFile: small, size: size, fromView: cell.avatarView), animated: true, completion: nil)
                }
            }
            return cell
            }.setHeight(92)
        
        // Send Message
        if (!isBot) {
            section
                .addActionCell("ProfileSendMessage", actionClosure: { () -> Bool in
                    self.navigateDetail(ConversationViewController(peer: ACPeer.userWithInt(jint(self.uid))))
                    self.popover?.dismissPopoverAnimated(true)
                    return false
                })
        }
        
        let nick = user!.getNickModel().get()
        let about = user!.getAboutModel().get()
        
        if !isBot || nick != nil || about != nil {
            section = tableData
                .addSection(true)
                .setFooterHeight(15)
                .setHeaderHeight(15)
        }
    
        if nick != nil {
            section
                .addTitledCell(localized("ProfileUsername"), text: "@\(nick)")
        }
        
        // Phones
        section
            .addCustomCells(55, countClosure: { () -> Int in
                if (self.phones != nil) {
                    return Int(self.phones!.size())
                }
                return 0
                }) { (tableView, index, indexPath) -> UITableViewCell in
                    let cell: TitledCell = tableView.dequeueReusableCellWithIdentifier(self.TitledCellIdentifier, forIndexPath: indexPath) as! TitledCell
                    let phone = self.phones!.getWithInt(jint(index))
                    cell.setTitle(phone.title, content: "+\(phone.phone)")
                    return cell
            }.setAction { (index) -> Bool in
                let phoneNumber = self.phones!.getWithInt(jint(index)).phone
                let hasPhone = UIApplication.sharedApplication().canOpenURL(NSURL(string: "telprompt://")!)
                if (!hasPhone) {
                    UIPasteboard.generalPasteboard().string = "+\(phoneNumber)"
                    self.alertUser("NumberCopied")
                } else {
                    UIApplication.sharedApplication().openURL(NSURL(string: "telprompt://+\(phoneNumber)")!)
                }
                return true
            }.setCanCopy(true)
        
        if about != nil {
            section
                .addTextCell(localized("ProfileAbout"), text: about)
                .setCopy(about)
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
        
        var contactsIndex: Int!
        if !isBot {
        section = tableData.addSection(true)
            .setHeaderHeight(15)
            .setFooterHeight(15)
        contactsIndex = section.index

        section
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
        section
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
        }
        
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
        
        if !isBot {
            
            binder.bind(user!.getPresenceModel(), closure: { (presence: ACUserPresence?) -> () in
                let presenceText = Actor.getFormatter().formatPresence(presence, withSex: self.user!.getSex())
                if presenceText != nil {
                    if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AvatarCell {
                    
                        if presence!.state.ordinal() == jint(ACUserPresence_State.ONLINE.rawValue) {
                            cell.subtitleLabel.applyStyle("user.online")
                        } else {
                            cell.subtitleLabel.applyStyle("user.offline")
                        }
                    
                        cell.subtitleLabel.text = presenceText
                    }
                }
            })
        
            binder.bind(user!.getPhonesModel(), closure: { (phones: ACArrayListUserPhone?) -> () in
                if phones != nil {
                    self.phones = phones
                    self.tableView.reloadData()
                }
            })
        
            binder.bind(user!.isContactModel(), closure: { (contect: ARValueModel?) -> () in
                self.tableView.reloadSections(NSIndexSet(index: contactsIndex), withRowAnimation: UITableViewRowAnimation.None)
            })
        }
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
