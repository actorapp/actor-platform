//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class AAUserInfoController: AATableViewController {
    
    // MARK: -
    // MARK: Private vars
    
    private let UserInfoCellIdentifier = "UserInfoCellIdentifier"
    private let TitledCellIdentifier = "TitledCellIdentifier"
    private let CellIdentifier = "CellIdentifier"
    
    private var phones: JavaUtilArrayList?
    
    // MARK: -
    // MARK: Public vars
    
    let uid: Int
    var user: AMUserVM?
    var binder = Binder()
    
    // MARK: -
    // MARK: Constructors
    
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
        
        user = MSG.getUsers().getWithLong(jlong(uid)) as? AMUserVM
        
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        tableView.registerClass(AAUserInfoCell.self, forCellReuseIdentifier: UserInfoCellIdentifier)
        tableView.registerClass(AATableViewCell.self, forCellReuseIdentifier: CellIdentifier)
        tableView.registerClass(AATitledCell.self, forCellReuseIdentifier: TitledCellIdentifier)
        
        tableView.reloadData()
        
        tableView.tableFooterView = UIView()
        
        binder.bind(user!.getAvatar(), closure: { (value: AMAvatar?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AAUserInfoCell {
                cell.userAvatarView.bind(self.user!.getName().get() as! String, id: jint(self.uid), avatar: value)
            }
        })
        
        binder.bind(user!.getName(), closure: { ( name: String?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AAUserInfoCell {
                cell.setUsername(name!)
            }
            self.title = name!
        })
        
        binder.bind(user!.getPresence(), closure: { (presence: AMUserPresence?) -> () in
            var presenceText = MSG.getFormatter().formatPresenceWithAMUserPresence(presence, withAMSexEnum: self.user!.getSex())
            if presenceText != nil {
                if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AAUserInfoCell {
                    cell.setPresence(presenceText)
                }
            }
        })
        
        binder.bind(user!.isContact(), closure: { (contect: AMValueModel?) -> () in
            self.tableView.reloadSections(NSIndexSet(index: 4), withRowAnimation: UITableViewRowAnimation.None)
        })
        
        binder.bind(user!.getPhones(), closure: { (phones: JavaUtilArrayList?) -> () in
            if phones != nil {
                self.phones = phones
                self.tableView.reloadData()
            }
        })
        
        navigationItem.rightBarButtonItem = UIBarButtonItem(
            title: NSLocalizedString("NavigationEdit", comment: "Edit Title"),
            style: UIBarButtonItemStyle.Plain,
            target: self, action: "editName")
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        MSG.onProfileOpen(jint(uid))
    }
    
    override func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated)
        MSG.onProfileClosed(jint(uid))
    }
    
    func scrollViewDidScroll(scrollView: UIScrollView) {
        if (scrollView == self.tableView) {
            var userCell = tableView.cellForRowAtIndexPath(NSIndexPath(forRow: 0, inSection: 0)) as? AAUserInfoCell
            var topOffset = getNavigationBarHeight() + getStatusBarHeight()
            var maxOffset = scrollView.frame.width - 200 + topOffset
            var offset = min(scrollView.contentOffset.y + topOffset, 200)
            userCell?.userAvatarView.frame = CGRectMake(0, offset, scrollView.frame.width, 200 - offset)
        }
    }
    
    // MARK: -
    // MARK: Setters
    
    func editName() {
        // TODO: Localize
        var alertView = UIAlertView(
            title: nil,
            message: NSLocalizedString("ProfileEditHeader", comment: "Edit Title"),
            delegate: self,
            cancelButtonTitle: NSLocalizedString("AlertCancel", comment: "Cancel Title"))
        alertView.addButtonWithTitle(NSLocalizedString("AlertSave", comment: "Save Title"))
        alertView.alertViewStyle = UIAlertViewStyle.PlainTextInput
        alertView.textFieldAtIndex(0)!.autocapitalizationType = UITextAutocapitalizationType.Words
        alertView.textFieldAtIndex(0)!.text = user!.getName().get() as! String
        alertView.show()
    }
    
    // MARK: -
    // MARK: Getters
    
    private func userInfoCell(indexPath: NSIndexPath) -> AAUserInfoCell {
        var cell: AAUserInfoCell = tableView.dequeueReusableCellWithIdentifier(UserInfoCellIdentifier, forIndexPath: indexPath) as! AAUserInfoCell
        cell.contentView.superview?.clipsToBounds = false
        if user != nil {
            
            if let username = user!.getName().get() as? String {
                cell.setUsername(username)
            }
            
        }
        
        return cell
    }
    
    private func sendMessageCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
            
        cell.style = AATableViewCellStyle.Blue
        cell.setLeftInset(15.0)
        cell.setContent(NSLocalizedString("ProfileSendMessage", comment: "Send Message Title"))
        
        cell.showBottomSeparator()
        cell.setBottomSeparatorLeftInset(15.0)
        
        return cell
    }
    
    private func phoneCell(indexPath: NSIndexPath) -> AATitledCell {
        var cell: AATitledCell = tableView.dequeueReusableCellWithIdentifier(TitledCellIdentifier, forIndexPath: indexPath) as! AATitledCell
        
        cell.setLeftInset(15.0)
        
        if let phone = phones!.getWithInt(jint(indexPath.row)) as? AMUserPhone {
            cell.setTitle(phone.getTitle(), content: "+\(phone.getPhone())")
        }
        
        cell.showBottomSeparator()
        
        var phonesCount = Int(phones!.size());
        if indexPath.row == phonesCount - 1 {
            cell.setBottomSeparatorLeftInset(0.0)
        } else {
            cell.setBottomSeparatorLeftInset(15.0)
        }
        
        return cell
    }
    
    private func notificationsCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Switch
        cell.setLeftInset(15.0)
        cell.setContent(NSLocalizedString("ProfileNotifications", comment: "Notificaitons Title"))
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        
        let userPeer: AMPeer! = AMPeer.userWithInt(jint(uid))
        cell.setSwitcherOn(MSG.isNotificationsEnabledWithAMPeer(userPeer))
        
        cell.switchBlock = { (on: Bool) -> () in
            MSG.changeNotificationsEnabledWithAMPeer(userPeer, withBoolean: on)
        }
        
        cell.showTopSeparator()
        cell.showBottomSeparator()
        
        return cell
    }
    
    private func deleteUserCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Destructive
        cell.setLeftInset(15.0)
        cell.setContent(NSLocalizedString("ProfileRemoveFromContacts", comment: "Remove From Contacts"))
        
        cell.showTopSeparator()
        cell.showBottomSeparator()
        
        return cell
    }
    
    private func addUserCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Blue
        cell.setLeftInset(15.0)
        cell.setContent(NSLocalizedString("ProfileAddToContacts", comment: "Add To Contacts"))
        
        cell.showTopSeparator()
        cell.showBottomSeparator()
        
        return cell
    }
    
    // MARK: -
    // MARK: UITableView Data Source
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 5
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch (section) {
        case 0:
            return 1
        case 1:
            return 1
        case 2:
            if phones != nil {
                return Int(phones!.size())
            }
            return 0
        case 3:
            return 1
        case 4:
            return 1
        default:
            return 0
        }
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if indexPath.section == 0 && indexPath.row == 0 {
            return userInfoCell(indexPath)
        } else if indexPath.section == 1 {
            return sendMessageCell(indexPath)
        } else if indexPath.section == 2 {
            return phoneCell(indexPath)
        } else if indexPath.section == 3 {
            return notificationsCell(indexPath)
        } else if indexPath.section == 4 {
            if ((user!.isContact().get() as! JavaLangBoolean).booleanValue()) {
                return deleteUserCell(indexPath)
            } else {
                return addUserCell(indexPath)
            }
        }
        return UITableViewCell()
    }
    
    func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        return false
    }
    
    func tableView(tableView: UITableView, canMoveRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        return false
    }
    
    func tableView(tableView: UITableView, editingStyleForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCellEditingStyle {
        return UITableViewCellEditingStyle.None
    }
    
    // MARK: -
    // MARK: UITableView Delegate
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        
        if indexPath.section == 1 {
            navigateToMessages()
        } else if indexPath.section == 4 {
            if ((user!.isContact().get() as! JavaLangBoolean).booleanValue()) {
                execute(MSG.removeContactWithInt(jint(uid)))
            } else {
                execute(MSG.addContactWithInt(jint(uid)))
            }
        }
    }
    
    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        if indexPath.section == 0 && indexPath.row == 0 {
            return 200.0
        } else if phones != nil && indexPath.section == 2 {
            return 55
        }
        return 44
    }
    
    func tableView(tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        if section < 2 {
            return 0.0
        }
        return 15.0
    }
    
    func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if section < 3 {
            return 0
        }
        return 15.0
    }
    
    func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView {
        return UIView()
    }
    
    func tableView(tableView: UITableView, viewForFooterInSection section: Int) -> UIView {
        return UIView()
    }
    
    // MARK: -
    // MARK: Navigation
    
    private func navigateToMessages() {
        let conversationController = AAConversationController(peer: AMPeer.userWithInt(jint(uid)))
        navigationController?.pushViewController(conversationController, animated: true);
    }

}

// MARK: -
// MARK: UIAlertView Delegate

extension AAUserInfoController: UIAlertViewDelegate {
    
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        if (buttonIndex == 1) {
            let textField = alertView.textFieldAtIndex(0)!
            if count(textField.text) > 0 {
                execute(MSG.editNameWithInt(jint(uid), withNSString: textField.text))
            }
        }
    }
}
