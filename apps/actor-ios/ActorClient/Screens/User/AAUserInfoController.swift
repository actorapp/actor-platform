//
//  AAUserInfoController.swift
//  ActorClient
//
//  Created by Danil Gontovnik on 4/4/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AAUserInfoController: AATableViewController {
    
    // MARK: -
    // MARK: Private vars
    
    private let WhiteHeaderIdentifier = "WhiteHeaderIdentifier"
    private let UserInfoCellIdentifier = "UserInfoCellIdentifier"
    private let CellIdentifier = "CellIdentifier"
    private let TitledCellIdentifier = "TitledCellIdentifier"
    
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
        
        title = "User Info" // TODO: Localize
        
        user = MSG.getUsers().getWithLong(jlong(uid)) as? AMUserVM
        
        tableView.registerClass(AAUserInfoCell.self, forCellReuseIdentifier: UserInfoCellIdentifier)
        tableView.registerClass(AATableViewCell.self, forCellReuseIdentifier: CellIdentifier)
        tableView.registerClass(AAUserInfoTitledCell.self, forCellReuseIdentifier: TitledCellIdentifier) // TODO: Do we display phone numbers or no?
        
        tableView.reloadData()
        
        tableView.tableFooterView = UIView()
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        
        binder.bind(user!.getAvatar(), closure: { (value: AMAvatar?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AAUserInfoCell {
                cell.userAvatarView.bind(self.user!.getName().get() as! String, id: jint(self.uid), avatar: value)
            }
        })
        
        binder.bind(user!.getName(), closure: { ( name: String?) -> () in
            if name != nil {
                if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AAUserInfoCell {
                    cell.setUsername(name!)
                }
            }
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
            self.tableView.reloadSections(NSIndexSet(index: 3), withRowAnimation: UITableViewRowAnimation.None)
        })
        
        navigationItem.rightBarButtonItem = editButtonItem()
        
        // TODO: Allow cancellation
    }
    
    // MARK: -
    // MARK: Getters
    
    private func userInfoCell(indexPath: NSIndexPath) -> AAUserInfoCell {
        var cell: AAUserInfoCell = tableView.dequeueReusableCellWithIdentifier(UserInfoCellIdentifier, forIndexPath: indexPath) as! AAUserInfoCell
        
        if user != nil {
            
            if let username = user!.getName().get() as? String {
                cell.setUsername(username)
                cell.usernameChangedBlock = { (newUsername: String) -> () in
                    self.execute(MSG.editNameWithInt(jint(self.uid), withNSString: newUsername))
                }
            }
            
        }
        
        return cell
    }
    
    private func sendMessageCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
            
        cell.style = AATableViewCellStyle.Blue
        cell.setLeftInset(65.0)
        cell.setTitle("Send Message") // TODO: Localize
        cell.showBottomSeparator()
        
        return cell
    }
    
    private func notificationsCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Switch
        cell.setLeftInset(65.0)
        cell.setTitle("Notifications") // TODO: Localize
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        
        let userPeer: AMPeer! = AMPeer.userWithInt(jint(uid))
        cell.setSwitcherOn(MSG.isNotificationsEnabledWithAMPeer(userPeer))
        
        
        cell.switchBlock = { (on: Bool) -> () in
            MSG.changeNotificationsEnabledWithAMPeer(userPeer, withBoolean: on)
        }
        cell.showBottomSeparator()
        
        return cell
    }
    
    private func deleteUserCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Destructive
        cell.setLeftInset(65.0)
        cell.setTitle("Delete User") // TODO: Localize
        cell.showBottomSeparator()
        
        return cell
    }
    
    private func addUserCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Green
        cell.setLeftInset(65.0)
        cell.setTitle("Add User") // TODO: Localize
        cell.showBottomSeparator()
        
        return cell
    }
    
    // MARK: -
    // MARK: UITableView Data Source
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 4
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch (section) {
        case 0:
            return 1
        case 1:
            return 1
        case 2:
            return 1
        case 3:
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
            return notificationsCell(indexPath)
        } else if indexPath.section == 3 {
            if ((user!.isContact().get() as! JavaLangBoolean).booleanValue()) {
                return deleteUserCell(indexPath) // TODO: Make it work
            } else {
                return addUserCell(indexPath)
            }
        }
        return UITableViewCell()
    }
    
    func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        if indexPath.section == 0 && indexPath.row == 0 {
            return true
        }
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
        } else if indexPath.section == 3 {
            if ((user!.isContact().get() as! JavaLangBoolean).booleanValue()) {
                execute(MSG.removeContactWithInt(jint(uid)))
            } else {
                execute(MSG.addContactWithInt(jint(uid)))
            }
        }
    }
    
    func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if section == 0 {
            return 0
        }
        return 22
    }
    
    func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        var view = tableView.dequeueReusableHeaderFooterViewWithIdentifier(WhiteHeaderIdentifier) as? UITableViewHeaderFooterView
        if view == nil {
            view = UITableViewHeaderFooterView(reuseIdentifier: WhiteHeaderIdentifier)
            
            view!.backgroundView = UIView()
            view!.backgroundView!.backgroundColor = UIColor.whiteColor()
        }
        return view
    }
    
    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        if indexPath.section == 0 {
            if indexPath.row == 0 {
                return 89
            }
            return 60
        }
        return 44
    }
    
    // MARK: -
    // MARK: Navigation
    
    private func navigateToMessages() {
        let messagesController = MessagesViewController(peer: AMPeer.userWithInt(jint(uid)))
        navigationController?.pushViewController(messagesController, animated: true);
    }

}
