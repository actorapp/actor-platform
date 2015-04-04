//
//  AAConversationGroupInfoController.swift
//  ActorClient
//
//  Created by Danil Gontovnik on 4/2/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AAConversationGroupInfoController: AATableViewController {
    
    // MARK: -
    // MARK: Private vars
    
    private let GroupInfoCellIdentifier = "GroupInfoCellIdentifier"
    private let UserCellIdentifier = "UserCellIdentifier"
    private let CellIdentifier = "CellIdentifier"
    
    private var groupMembers: IOSObjectArray?
    
    private var groupNameBeforeEditing: String = ""
    private var groupNameTextField: UITextField?
    
    // MARK: -
    // MARK: Public vars
    
    let gid: Int
    var group: AMGroupVM?
    var binder = Binder()
    
    // MARK: -
    // MARK: Constructors
    
    init (gid: Int) {
        self.gid = gid
        
        super.init(style: UITableViewStyle.Grouped)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "Group Info" // TODO: Localize
        
        group = MSG.getGroups().getWithLong(jlong(gid)) as? AMGroupVM;
        
        tableView.registerClass(AAConversationGroupInfoCell.self, forCellReuseIdentifier: GroupInfoCellIdentifier)
        tableView.registerClass(AAConversationGroupInfoUserCell.self, forCellReuseIdentifier: UserCellIdentifier)
        tableView.registerClass(AATableViewCell.self, forCellReuseIdentifier: CellIdentifier)
        
        tableView.reloadData()
        
        binder.bind(group!.getName()!, closure: { (value: String?) -> () in
            var cell: AAConversationGroupInfoCell? = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AAConversationGroupInfoCell
            if cell != nil {
                cell!.setGroupName(value!)
            }
        })
        
        binder.bind(group!.getAvatar(), closure: { (value: AMAvatar?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AAConversationGroupInfoCell {
                cell.groupAvatarView.bind(self.group!.getName().get() as! String, id: jint(self.gid), avatar: value)
            }
        })
        
        binder.bind(group!.getMembers(), closure: { (value: JavaUtilHashSet?) -> () in
            if value != nil {
                let groupMembetsPreviousCount = Int(self.groupMembers != nil ? self.groupMembers!.length() : 0)
                self.groupMembers = value!.toArray()
                
                self.tableView.reloadSections(NSIndexSet(index: 2), withRowAnimation: (groupMembetsPreviousCount == 0 ? UITableViewRowAnimation.None : UITableViewRowAnimation.Automatic))
            }
        })
        
        binder.bind(group!.isMember(), closure: { (member: JavaLangBoolean?) -> () in
            if member != nil {
                println("isMember: \(member!)")
                self.tableView.beginUpdates()
                self.tableView.endUpdates()
                
                if Bool(member!.booleanValue()) == true {
                    self.navigationItem.rightBarButtonItem = self.editButtonItem()
                    
                    self.hidePlaceholder()
                } else {
                    self.navigationItem.rightBarButtonItem = nil
                    self.showPlaceholderWithImage(nil, title: "Not a member", subtitle: "Unfortunately, you are not a member of this group.")
                }
            }
        })
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "Back", style: UIBarButtonItemStyle.Plain, target: nil, action: nil) // TODO: Localize
    }
    
    // MARK: -
    // MARK: Setters
    
    override func setEditing(editing: Bool, animated: Bool) {
        super.setEditing(editing, animated: animated)
        
        var cell = tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AAConversationGroupInfoCell
        
        if cell != nil {
            if editing == true {
                groupNameBeforeEditing = cell!.groupName()
                groupNameTextField = cell!.groupNameTextField
            } else {
                if groupNameBeforeEditing != cell!.groupName() {
                    execute(MSG.editGroupTitleWithInt(jint(gid), withNSString: cell!.groupName()))
                }
            }
        }
        
        if groupNameTextField != nil {
            if editing == true {
                groupNameTextField!.becomeFirstResponder()
            } else {
                groupNameTextField!.resignFirstResponder()
                groupNameTextField = nil
            }
        }
        
        tableView.beginUpdates()
        tableView.endUpdates()
    }
    
    // MARK: -
    // MARK: Getters
    
    private func groupInfoCell(indexPath: NSIndexPath) -> AAConversationGroupInfoCell {
        var cell: AAConversationGroupInfoCell = tableView.dequeueReusableCellWithIdentifier(GroupInfoCellIdentifier, forIndexPath: indexPath) as! AAConversationGroupInfoCell
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        return cell
    }
    
    private func setGroupPhotoCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Blue
        cell.setTitle("Set Group Photo")
        cell.setLeftInset(15.0)
        
        return cell
    }
    
    private func notificationsCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Switch
        cell.setTitle("Notifications")
        cell.setLeftInset(15.0)
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        
        let groupPeer: AMPeer! = AMPeer.groupWithInt(jint(gid))
        cell.setSwitcherOn(MSG.isNotificationsEnabledWithAMPeer(groupPeer))

        
        cell.switchBlock = { (on: Bool) -> () in
            MSG.changeNotificationsEnabledWithAMPeer(groupPeer, withBoolean: on)
        }
        
        return cell
    }
    
    private func setupUserCellForIndexPath(indexPath: NSIndexPath) -> AAConversationGroupInfoUserCell {
        var cell: AAConversationGroupInfoUserCell = tableView.dequeueReusableCellWithIdentifier(UserCellIdentifier, forIndexPath: indexPath) as! AAConversationGroupInfoUserCell
        
        if let groupMember = groupMembers!.objectAtIndex(UInt(indexPath.row)) as? AMGroupMember,
           let user = MSG.getUsers().getWithLong(jlong(groupMember.getUid())) as? AMUserVM {

            var username = ""
            if let uname = user.getName().get() as? String {
                username = uname
            }

            let avatar: AMAvatar? = user.getAvatar().get() as? AMAvatar
            cell.userAvatarView.bind(username, id: user.getId(), avatar: avatar)
            
            cell.setUsername(username)
        }
        
        cell.setLeftInset(65.0)
        
        return cell
    }
    
    private func addParticipantCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Blue
        cell.setTitle("Add Participant...") // TODO: Localize
        cell.setLeftInset(65.0)
        cell.selectionStyle = UITableViewCellSelectionStyle.Default
        
        return cell
    }
    
    private func leaveConversationCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Destructive
        cell.setTitle("Leave group") // TODO: Localize
        cell.setLeftInset(15.0)
        cell.selectionStyle = UITableViewCellSelectionStyle.Default
        
        return cell
    }
    
    // MARK: - 
    // MARK: UITableView Data Source
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 4
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch section {
        case 0:
            return 2
        case 1:
            return 1
        case 2:
            if groupMembers != nil {
                return Int(groupMembers!.length()) + 1
            }
            return 0
        case 3:
            return 1
        default:
            return 0
        }
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if indexPath.section == 0 && indexPath.row == 0 {
            return groupInfoCell(indexPath)
        } else if indexPath.section == 0 && indexPath.row == 1 {
            return setGroupPhotoCell(indexPath)
        } else if indexPath.section == 1 {
            return notificationsCell(indexPath)
        } else if indexPath.section == 2 {
            let groupMembersCount = Int(groupMembers!.length())
            if indexPath.row < groupMembersCount {
                return setupUserCellForIndexPath(indexPath)
            } else {
                return addParticipantCell(indexPath)
            }
        } else if indexPath.section == 3 {
            return leaveConversationCell(indexPath)
        }
        return UITableViewCell()
    }
    
    override func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        if indexPath.section == 0 && indexPath.row == 0 {
            return true
        }
        return false
    }
    
    override func tableView(tableView: UITableView, canMoveRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        return false
    }
    
    override func tableView(tableView: UITableView, editingStyleForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCellEditingStyle {
        return UITableViewCellEditingStyle.None
    }
    
    override func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        if section == 1 {
            return "SETTINGS" // TODO: Localize
        } else if section == 2 {
            let groupMembersCount = (groupMembers != nil) ? Int(groupMembers!.length()) : 0
            return "\(groupMembersCount) MEMBERS" // TODO: Localize
        }
        return ""
    }
    
    // MARK: - 
    // MARK: UITableView Delegate
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        
        if indexPath.section == 2 {
            let groupMembersCount = Int(groupMembers!.length())
            if indexPath.row < groupMembersCount - 1 {
                
                if let groupMember = groupMembers!.objectAtIndex(UInt(indexPath.row)) as? AMGroupMember,
                   let user = MSG.getUsers().getWithLong(jlong(groupMember.getUid())) as? AMUserVM {
                    let profileController = ProfileController(uid: Int(user.getId()))
                    navigationController!.pushViewController(profileController, animated: true)
                }
                
            } else {
                let addParticipantController = AAAddParticipantController(gid: gid)
                let navigationController = AANavigationController(rootViewController: addParticipantController)
                presentViewController(navigationController, animated: true, completion: nil)
                // TODO: Allow to add participants
                // TODO: Allow to delete participants if I am a creator?
            }
        } else if indexPath.section == 3 {
            execute(MSG.leaveGroupWithInt(jint(gid)))
        }
    }
    
    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        if indexPath.section == 0 && indexPath.row == 0 {
            return (editing ? 160 : 150)
        } else if indexPath.section == 2 {
            let groupMembersCount = Int(groupMembers!.length())
            if indexPath.row < groupMembersCount - 1 {
                return 48
            }
        }
        return 44
    }
    
    override func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if section == 0 {
            return CGFloat.min
        }
        return tableView.sectionHeaderHeight
    }

}
