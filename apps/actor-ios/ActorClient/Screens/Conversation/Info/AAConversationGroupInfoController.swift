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
            self.title = value!
        })
        
        binder.bind(group!.getAvatar(), closure: { (value: AMAvatar?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AAConversationGroupInfoCell {
                cell.groupAvatarView.bind(self.group!.getName().get() as! String, id: jint(self.gid), avatar: value)
            }
        })
        
        binder.bind(group!.getMembers(), closure: { (value: JavaUtilHashSet?) -> () in
            if value != nil {
                self.groupMembers = value!.toArray()
                
                self.tableView.reloadData()
//                self.tableView.reloadSections(NSIndexSet(index: 2), withRowAnimation: UITableViewRowAnimation.None)
            }
        })
        
        binder.bind(group!.isMember(), closure: { (member: JavaLangBoolean?) -> () in
            if member != nil {
                if Bool(member!.booleanValue()) == true {
                    self.navigationItem.rightBarButtonItem = self.editButtonItem()
                    
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
        
        // TODO: Allow cancellation
    }
    
    // MARK: -
    // MARK: Setters
    
    override func setEditing(editing: Bool, animated: Bool) {
        // TODO: Localize
        var alertView = UIAlertView(title: nil, message: "Change name", delegate: self, cancelButtonTitle: "Cancel")
        alertView.addButtonWithTitle("Change")
        alertView.alertViewStyle = UIAlertViewStyle.PlainTextInput
        alertView.textFieldAtIndex(0)!.text = group!.getName().get() as! String
        alertView.show()
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
        cell.setContent("Set Group Photo") // TODO: Localize
        cell.setLeftInset(15.0)
        
        return cell
    }
    
    private func notificationsCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Switch
        cell.setContent("Notifications") // TODO: Localize
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
        cell.setContent("Add Participant...") // TODO: Localize
        cell.setLeftInset(65.0)
        cell.selectionStyle = UITableViewCellSelectionStyle.Default
        
        return cell
    }
    
    private func leaveConversationCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.DestructiveCentered
        cell.setContent("Leave group") // TODO: Localize
        cell.setLeftInset(15.0)
        cell.selectionStyle = UITableViewCellSelectionStyle.Default
        
        return cell
    }
    
    // MARK: -
    // MARK: Methods
    
    private func askSetPhoto() {
        var actionSheet = UIActionSheet(title: nil, delegate: self, cancelButtonTitle: "Cancel", destructiveButtonTitle: nil, otherButtonTitles: "Take Photo", "Choose Photo") // TODO: Localize
        actionSheet.addButtonWithTitle("Delete Photo")
        actionSheet.destructiveButtonIndex = 3
        actionSheet.showInView(view)
    }
    
    private func changeAvatarToImage(image: UIImage) {
        let avatarPath = NSTemporaryDirectory().stringByAppendingPathComponent("avatar.jpg")
        var thumb = image.resizeSquare(200, maxH: 200);
        UIImageJPEGRepresentation(thumb, 0.8).writeToFile(avatarPath, atomically: true) // TODO: Check smallest 100x100, crop to 800x800
        MSG.changeGroupAvatarWithInt(jint(gid), withNSString: "/tmp/avatar.jpg")
    }
    
    // MARK: - 
    // MARK: UITableView Data Source
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 5
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch section {
        case 0:
            return 1
        case 1:
            return 1
        case 2:
            return 1
        case 3:
            if groupMembers != nil {
                return Int(groupMembers!.length()) + 1
            }
            return 0
        case 4:
            return 1
        default:
            return 0
        }
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if indexPath.section == 0 {
            return groupInfoCell(indexPath)
        } else if indexPath.section == 1 {
            return setGroupPhotoCell(indexPath)
        } else if indexPath.section == 2 {
            return notificationsCell(indexPath)
        } else if indexPath.section == 3 {
            let groupMembersCount = Int(groupMembers!.length())
            if indexPath.row < groupMembersCount {
                return setupUserCellForIndexPath(indexPath)
            } else {
                return addParticipantCell(indexPath)
            }
        } else if indexPath.section == 4 {
            return leaveConversationCell(indexPath)
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
    
    func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        if section == 2 {
            return "SETTINGS" // TODO: Localize
        } else if section == 3 {
            let groupMembersCount = (groupMembers != nil) ? Int(groupMembers!.length()) : 0
            return "\(groupMembersCount) MEMBERS" // TODO: Localize
        }
        return ""
    }
    
    // MARK: - 
    // MARK: UITableView Delegate
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        
        // TODO: Allow to change group photo? 
        // TODO: Allow to delete participants if I am a creator?
        
        if indexPath.section == 1 && indexPath.row == 0 {
            askSetPhoto()
        } else if indexPath.section == 3 {
            let groupMembersCount = Int(groupMembers!.length())
            if indexPath.row < groupMembersCount {
                
                if let groupMember = groupMembers!.objectAtIndex(UInt(indexPath.row)) as? AMGroupMember,
                   let user = MSG.getUsers().getWithLong(jlong(groupMember.getUid())) as? AMUserVM {
                    navigateToUserInfoWithUid(Int(user.getId()))
                }
                
            } else {
                showAddParticipants()
            }
        } else if indexPath.section == 4 {
            execute(MSG.leaveGroupWithInt(jint(gid)))
        }
    }
    
    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        if indexPath.section == 0 {
            return 200.0
        } else if indexPath.section == 3 {
            let groupMembersCount = Int(groupMembers!.length())
            if indexPath.row < groupMembersCount - 1 {
                return 48
            }
        }
        return 44
    }
    
    func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if section == 0 {
            return CGFloat.min
        }
        return tableView.sectionHeaderHeight
    }
    
    // MARK: -
    // MARK: Navigation
    
    private func navigateToUserInfoWithUid(uid: Int) {
        let userInfoController = AAUserInfoController(uid: uid)
        userInfoController.hidesBottomBarWhenPushed = true
        navigationController!.pushViewController(userInfoController, animated: true)
    }
    
    private func showAddParticipants() {
        let addParticipantController = AAAddParticipantController(gid: gid)
        let navigationController = AANavigationController(rootViewController: addParticipantController)
        presentViewController(navigationController, animated: true, completion: nil)
    }

}

// MARK: -
// MARK: UIActionSheet Delegate

extension AAConversationGroupInfoController: UIActionSheetDelegate {
    
    func actionSheet(actionSheet: UIActionSheet, didDismissWithButtonIndex buttonIndex: Int) {
        let title = actionSheet.buttonTitleAtIndex(buttonIndex)
        
        // TODO: Localize
        if title == "Choose Photo" || title == "Take Photo" {
            let takePhoto = (title == "Take Photo")
            var picker = UIImagePickerController()
            picker.sourceType = (takePhoto ? UIImagePickerControllerSourceType.Camera : UIImagePickerControllerSourceType.PhotoLibrary)
            picker.delegate = self
            self.navigationController!.presentViewController(picker, animated: true, completion: nil)
            
        } else if title == "Delete Photo" {
            MSG.removeGroupAvatarWithInt(jint(gid)) // TODO: Why request is not working?
        }
    }
    
}

// MARK: -
// MARK: UIImagePickerController Delegate

extension AAConversationGroupInfoController: UIImagePickerControllerDelegate {
    
    // TODO: Allow to crop rectangle
    func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage!, editingInfo: [NSObject : AnyObject]!) {

        changeAvatarToImage(image)
        
        navigationController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [NSObject : AnyObject]) {
        let image = info[UIImagePickerControllerOriginalImage] as! UIImage

        changeAvatarToImage(image)
        
        navigationController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    func imagePickerControllerDidCancel(picker: UIImagePickerController) {
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
}

// MARK: -
// MARK: UINavigationController Delegate

extension AAConversationGroupInfoController: UINavigationControllerDelegate {
    
    
    
}

// MARK: -
// MARK: UIAlertView Delegate

extension AAConversationGroupInfoController: UIAlertViewDelegate {
    
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        if alertView.buttonTitleAtIndex(buttonIndex) == "Change" {
            let textField = alertView.textFieldAtIndex(0)!
            if count(textField.text) > 0 {
                execute(MSG.editGroupTitleWithInt(jint(gid), withNSString: textField.text))
            }
        }
    }
}
