//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit
import MobileCoreServices 

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
        
        super.init(style: UITableViewStyle.Plain)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = MainAppTheme.list.bgColor
        
        group = MSG.getGroupWithGid(jint(gid))
        
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        tableView.registerClass(AAConversationGroupInfoCell.self, forCellReuseIdentifier: GroupInfoCellIdentifier)
        tableView.registerClass(AAConversationGroupInfoUserCell.self, forCellReuseIdentifier: UserCellIdentifier)
        tableView.registerClass(AATableViewCell.self, forCellReuseIdentifier: CellIdentifier)
        tableView.clipsToBounds = false
        tableView.reloadData()
        
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
        
        binder.bind(group!.getMembersModel(), closure: { (value: JavaUtilHashSet?) -> () in
            if value != nil {
                self.groupMembers = value!.toArray()
                
                self.tableView.reloadData()
//                self.tableView.reloadSections(NSIndexSet(index: 2), withRowAnimation: UITableViewRowAnimation.None)
            }
        })
        
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
    
    // MARK: -
    // MARK: Setters
    
    func editName() {
        var alertView = UIAlertView(title: nil,
            message: NSLocalizedString("GroupEditHeader", comment: "Change name"),
            delegate: self,
            cancelButtonTitle: NSLocalizedString("AlertCancel", comment: "Cancel titlte"))
        alertView.addButtonWithTitle(NSLocalizedString("AlertSave", comment: "Save titlte"))
        alertView.alertViewStyle = UIAlertViewStyle.PlainTextInput
        alertView.textFieldAtIndex(0)!.autocapitalizationType = UITextAutocapitalizationType.Words
        alertView.textFieldAtIndex(0)!.text = group!.getNameModel().get()
        alertView.textFieldAtIndex(0)!.keyboardAppearance = MainAppTheme.common.isDarkKeyboard ? UIKeyboardAppearance.Dark : UIKeyboardAppearance.Light
        alertView.show()
    }
    
    // MARK: -
    // MARK: Getters
    
    func scrollViewDidScroll(scrollView: UIScrollView) {
        if (scrollView == self.tableView) {
            var groupCell = tableView.cellForRowAtIndexPath(NSIndexPath(forRow: 0, inSection: 0)) as? AAConversationGroupInfoCell
            var topOffset = scrollView.contentInset.top
            var maxOffset = scrollView.frame.width - 200 + topOffset
            var offset = min((isiOS8 ? 0 : -topOffset) + scrollView.contentOffset.y + topOffset, 200)
            groupCell?.groupAvatarView.frame = CGRectMake(0, offset, scrollView.frame.width, 200 - offset)
        }
    }
    
    private func groupInfoCell(indexPath: NSIndexPath) -> AAConversationGroupInfoCell {
        var cell: AAConversationGroupInfoCell = tableView.dequeueReusableCellWithIdentifier(GroupInfoCellIdentifier, forIndexPath: indexPath) as! AAConversationGroupInfoCell
        
        cell.contentView.superview?.clipsToBounds = false
        
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        
        var topOffset = tableView.contentInset.top
        var maxOffset = tableView.frame.width - 200 + topOffset
        var offset = min(tableView.contentOffset.y + topOffset, 200)
        cell.groupAvatarView.frame = CGRectMake(0, offset, tableView.frame.width, 200 - offset)
        
        return cell
    }
    
    private func setGroupPhotoCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Blue
        cell.setContent(NSLocalizedString("GroupSetPhoto", comment: "Set Group Photo"))
        cell.setLeftInset(15.0)
        
        cell.showBottomSeparator()
        cell.setBottomSeparatorLeftInset(0.0)
        
        return cell
    }
    
    private func notificationsCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Switch
        cell.setContent(NSLocalizedString("GroupNotifications", comment: "Notifications"))
        cell.setLeftInset(15.0)
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        
        let groupPeer: AMPeer! = AMPeer.groupWithInt(jint(gid))
        cell.setSwitcherOn(MSG.isNotificationsEnabledWithPeer(groupPeer))

        
        cell.switchBlock = { (on: Bool) -> () in
            MSG.changeNotificationsEnabledWithPeer(groupPeer, withValue: on)
        }
        
        cell.showTopSeparator()
        cell.setTopSeparatorLeftInset(0.0)
        
        cell.showBottomSeparator()
        cell.setBottomSeparatorLeftInset(0.0)
        
        return cell
    }
    
    private func setupUserCellForIndexPath(indexPath: NSIndexPath) -> AAConversationGroupInfoUserCell {
        var cell: AAConversationGroupInfoUserCell = tableView.dequeueReusableCellWithIdentifier(UserCellIdentifier, forIndexPath: indexPath) as! AAConversationGroupInfoUserCell
        
        if let groupMember = groupMembers!.objectAtIndex(UInt(indexPath.row)) as? AMGroupMember,
            let user = MSG.getUserWithUid(groupMember.getUid()) {
            var username = user.getNameModel().get()
            let avatar: AMAvatar? = user.getAvatarModel().get()
            
            cell.userAvatarView.bind(username, id: user.getId(), avatar: avatar)
            
            cell.setUsername(username)
        }
        
        cell.setLeftInset(65.0)
        
        cell.showBottomSeparator()
        cell.setBottomSeparatorLeftInset(15.0)
        
        if indexPath.row == 0 {
            cell.showTopSeparator()
            cell.setTopSeparatorLeftInset(0.0)
        }
        
        return cell
    }
    
    private func addParticipantCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Blue
        cell.setContent(NSLocalizedString("GroupAddParticipant", comment: "Add Prticipant"))
        cell.setLeftInset(65.0)
        cell.selectionStyle = UITableViewCellSelectionStyle.Default
        
        cell.showBottomSeparator()
        cell.setBottomSeparatorLeftInset(0.0)
        
        return cell
    }
    
    private func leaveConversationCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.DestructiveCentered
        cell.setContent(NSLocalizedString("GroupLeave", comment: "Leave group"))
        cell.setLeftInset(15.0)
        cell.selectionStyle = UITableViewCellSelectionStyle.Default
        
        cell.setTopSeparatorLeftInset(0.0)
        cell.showTopSeparator()
        
        cell.setBottomSeparatorLeftInset(0.0)
        cell.showBottomSeparator()
        
        return cell
    }
    
    // MARK: -
    // MARK: Methods
    
    private func askSetPhoto() {
        var actionSheet = UIActionSheet(title: nil,
            delegate: self,
            cancelButtonTitle: NSLocalizedString("AlertCancel", comment: "Cancel"),
            destructiveButtonTitle: nil,
            otherButtonTitles: NSLocalizedString("PhotoCamera", comment: "Camera"), NSLocalizedString("PhotoLibrary", comment: "Camera"))
        
        if (group?.getAvatarModel().get() != nil) {
            actionSheet.addButtonWithTitle(NSLocalizedString("PhotoRemove", comment: "Remove"))
            actionSheet.destructiveButtonIndex = 3
        }
        actionSheet.showInView(view)
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
                if let groupMember = groupMembers!.objectAtIndex(UInt(indexPath.row)) as? AMGroupMember, let user = MSG.getUserWithUid(groupMember.getUid()) {
                    navigateToUserInfoWithUid(Int(user.getId()))
                }
            } else {
                showAddParticipants()
            }
        } else if indexPath.section == 4 {
            execute(MSG.leaveGroupCommandWithGid(jint(gid)))
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
        if section < 2 {
            return 0.0
        }
        return 15.0
    }
    
    func tableView(tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        if section == 0 {
            return 0.0
        }
        return 15.0
    }
    
    func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView {
        return UIView()
    }
    
    func tableView(tableView: UITableView, viewForFooterInSection section: Int) -> UIView {
        return UIView()
    }
    
//    func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
//        if section == 2 {
//            return "SETTINGS" // TODO: Localize
//        } else if section == 3 {
//            let groupMembersCount = (groupMembers != nil) ? Int(groupMembers!.length()) : 0
//            return "\(groupMembersCount) MEMBERS" // TODO: Localize
//        }
//        return ""
//    }
    
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
        if (buttonIndex == 0) {
            return
        }

        if (buttonIndex == 1 || buttonIndex == 2) {
            let takePhoto = (buttonIndex == 1)
            var pickerController = AAImagePickerController()
            pickerController.sourceType = (takePhoto ? UIImagePickerControllerSourceType.Camera : UIImagePickerControllerSourceType.PhotoLibrary)
            pickerController.mediaTypes = [kUTTypeImage]
            pickerController.view.backgroundColor = MainAppTheme.list.bgColor
            pickerController.navigationBar.tintColor = MainAppTheme.navigation.barColor
            pickerController.delegate = self
            pickerController.navigationBar.tintColor = MainAppTheme.navigation.titleColor
            pickerController.navigationBar.titleTextAttributes = [NSForegroundColorAttributeName: MainAppTheme.navigation.titleColor]
            self.navigationController!.presentViewController(pickerController, animated: true, completion: nil)
        } else if (buttonIndex == 3) {
            MSG.removeGroupAvatarWithGid(jint(gid))
        }
    }
}

// MARK: -
// MARK: UIImagePickerController Delegate

extension AAConversationGroupInfoController: UIImagePickerControllerDelegate, PECropViewControllerDelegate, UINavigationControllerDelegate {
    
    func cropImage(image: UIImage) {
        var cropController = PECropViewController()
        cropController.cropAspectRatio = 1.0
        cropController.keepingCropAspectRatio = true
        cropController.image = image
        cropController.delegate = self
        cropController.toolbarHidden = true
        navigationController!.presentViewController(UINavigationController(rootViewController: cropController), animated: true, completion: nil)
    }
    
    func cropViewController(controller: PECropViewController!, didFinishCroppingImage croppedImage: UIImage!) {
        MSG.changeGroupAvatar(jint(gid), image: croppedImage)
        navigationController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    func cropViewControllerDidCancel(controller: PECropViewController!) {
        navigationController!.dismissViewControllerAnimated(true, completion: nil)
    }

    
    // TODO: Allow to crop rectangle
    func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage!, editingInfo: [NSObject : AnyObject]!) {
        MainAppTheme.navigation.applyStatusBar()
        navigationController!.dismissViewControllerAnimated(true, completion: { () -> Void in
            self.cropImage(image)
        })
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [NSObject : AnyObject]) {
        MainAppTheme.navigation.applyStatusBar()
        let image = info[UIImagePickerControllerOriginalImage] as! UIImage
        navigationController!.dismissViewControllerAnimated(true, completion: { () -> Void in
            self.cropImage(image)
        })
    }
    
    func imagePickerControllerDidCancel(picker: UIImagePickerController) {
        MainAppTheme.navigation.applyStatusBar()
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
        if (buttonIndex == 1) {
            let textField = alertView.textFieldAtIndex(0)!
            if count(textField.text) > 0 {
                execute(MSG.editGroupTitleCommandWithGid(jint(gid), withTitle: textField.text))
            }
        }
    }
}
