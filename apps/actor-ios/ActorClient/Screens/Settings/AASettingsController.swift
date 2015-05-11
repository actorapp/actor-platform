//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit
import MobileCoreServices 

class AASettingsController: AATableViewController, UIScrollViewDelegate {
    
    // MARK: -
    // MARK: Private vars
    
    private let UserInfoCellIdentifier = "UserInfoCellIdentifier"
    private let TitledCellIdentifier = "TitledCellIdentifier"
    private let CellIdentifier = "CellIdentifier"
    
    private let uid: Int
    private var user: AMUserVM?
    private var binder = Binder()
    
    private var phones: JavaUtilArrayList?
    
    // MARK: -
    // MARK: Constructors
    
    init() {
        uid = Int(MSG.myUid())
        
        super.init(style: UITableViewStyle.Plain)
        initCommon()
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -

    override func viewDidLoad() {
        super.viewDidLoad()
        
        user = MSG.getUsers().getWithLong(jlong(uid)) as? AMUserVM
        
        navigationItem.title = NSLocalizedString("TabSettings", comment: "Settings Title")
        navigationItem.rightBarButtonItem = UIBarButtonItem(
            title: NSLocalizedString("SettingsEdit", comment: "Edtit Action"),
            style: UIBarButtonItemStyle.Plain,
            target: self,
            action: "editProfile")
        
        view.backgroundColor = MainAppTheme.list.bgColor
        
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        tableView.registerClass(AAUserInfoCell.self, forCellReuseIdentifier: UserInfoCellIdentifier)
        tableView.registerClass(AATitledCell.self, forCellReuseIdentifier: TitledCellIdentifier)
        tableView.registerClass(AATableViewCell.self, forCellReuseIdentifier: CellIdentifier)
        tableView.reloadData()
        tableView.clipsToBounds = false
        tableView.tableFooterView = UIView()
        
        binder.bind(user!.getName()!, closure: { (value: String?) -> () in
            if value == nil {
                return
            }
            
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AAUserInfoCell {
                cell.setUsername(value!)
            }
        })
        
        binder.bind(user!.getAvatar(), closure: { (value: AMAvatar?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AAUserInfoCell {
                cell.userAvatarView.bind(self.user!.getName().get() as! String, id: jint(self.uid), avatar: value)
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
        
        binder.bind(user!.getPhones(), closure: { (phones: JavaUtilArrayList?) -> () in
            if phones != nil {
                self.phones = phones
                self.tableView.reloadData()
            }
        })
    }
    
    func initCommon(){
        
        var title = "";
        if (MainAppTheme.tab.showText) {
            title = NSLocalizedString("TabSettings", comment: "Settings Title")
        }
        
        tabBarItem = UITabBarItem(title: title,
            image: MainAppTheme.tab.createUnselectedIcon("ic_settings_outline"),
            selectedImage: MainAppTheme.tab.createSelectedIcon("ic_settings_filled"))
        
        if (!MainAppTheme.tab.showText) {
            tabBarItem.imageInsets = UIEdgeInsetsMake(6, 0, -6, 0);
        }
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        MSG.onProfileOpen(jint(uid))
        
        MainAppTheme.navigation.applyStatusBar()
    }
    
    override func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated)
        MSG.onProfileClosed(jint(uid))
    }
    
    func scrollViewDidScroll(scrollView: UIScrollView) {
        if (scrollView == self.tableView) {
            var userCell = tableView.cellForRowAtIndexPath(NSIndexPath(forRow: 0, inSection: 0)) as? AAUserInfoCell
            if (scrollView.contentOffset.y < 0) {
                if (scrollView.contentOffset.y < -(scrollView.frame.width - 200)) {
                    scrollView.contentOffset = CGPointMake(0, -scrollView.frame.width + 200)
                }
                var offset = scrollView.contentOffset.y
                userCell?.userAvatarView.frame = CGRectMake(0, offset, scrollView.frame.width, 200 - offset)
            } else {
                userCell?.userAvatarView.frame = CGRectMake(0, 0, scrollView.frame.width, 200)
            }
        }
    }
    
    // MARK: -
    // MARK: Methods
    
    private func askSetPhoto(pressedView: UIView) {
        var actionSheet = UIActionSheet(title: nil, delegate: self,
            cancelButtonTitle: NSLocalizedString("AlertCancel", comment: "Cancel"),
            destructiveButtonTitle: nil,
            otherButtonTitles: NSLocalizedString("PhotoCamera", comment: "Camera"), NSLocalizedString("PhotoLibrary", comment: "Library"))
        if (user!.getAvatar().get() != nil) {
            actionSheet.addButtonWithTitle(NSLocalizedString("PhotoRemove", comment: "Remove"))
            actionSheet.destructiveButtonIndex = 3
        }
        actionSheet.showInView(view)
    }
    
    // MARK: -
    // MARK: Setters
    
    func editProfile() {
        var alertView = UIAlertView(title: nil,
            message: NSLocalizedString("SettingsEditHeader", comment: "Title"),
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
        cell.setLeftInset(15.0)
        
        return cell
    }
    
    private func setProfilePhotoCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Blue
        cell.setContent(NSLocalizedString("SettingsSetPhoto", comment: "Edit Photo"))
        cell.setLeftInset(15.0)
        
        cell.setBottomSeparatorLeftInset(15.0)
        cell.showBottomSeparator()
        
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
        
        cell.style = AATableViewCellStyle.Navigation
        cell.setContent("Notifications and Sounds") // TODO: Localize
        cell.setLeftInset(15.0)
        
        cell.showTopSeparator()
        
        return cell
    }
    
    private func privacyCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Navigation
        cell.setContent(NSLocalizedString("SettingsSecurity", comment: "Security Title"))
        cell.setLeftInset(15.0)
        
        cell.showTopSeparator()
        cell.showBottomSeparator()
        
        return cell
    }
    
    private func askQuestionCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Navigation
        cell.setContent("Ask Question")
        cell.setLeftInset(15.0)
        
        cell.showTopSeparator()
        cell.showBottomSeparator()
        
        return cell
    }

    private func aboutCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Navigation
        cell.setContent("About")
        cell.setLeftInset(15.0)
        
        cell.showBottomSeparator()
        
        return cell
    }

    private func versionCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Hint
        cell.setLeftInset(15.0)

        var version = NSBundle.mainBundle().infoDictionary!["CFBundleShortVersionString"] as! String
        cell.setContent("App Version: \(version)")
        
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
            if phones == nil {
                return 0
            }
            return Int(phones!.size());
        case 3:
            return 2
        case 4:
            return 3
        default:
            return 0
        }
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        if indexPath.section == 0 && indexPath.row == 0 {
            return userInfoCell(indexPath)
        } else if indexPath.section == 1 && indexPath.row == 0 {
            return setProfilePhotoCell(indexPath)
        } else if indexPath.section == 2 {
            return phoneCell(indexPath)
        } else if (indexPath.section == 3 && indexPath.row == 0) {
            return notificationsCell(indexPath)
        } else if (indexPath.section == 3 && indexPath.row == 1) {
            return privacyCell(indexPath)
        } else if indexPath.section == 4 && indexPath.row == 0 {
            return askQuestionCell(indexPath)
        } else if indexPath.section == 4 && indexPath.row == 1 {
            return aboutCell(indexPath)
        } else if indexPath.section == 4 && indexPath.row == 2 {
            return versionCell(indexPath)
        }

        return UITableViewCell()
    }
    
    func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        return false
    }
    
    // MARK: -
    // MARK: UITableView Delegate
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        
        if indexPath.section == 1 && indexPath.row == 0 {
            askSetPhoto(tableView.cellForRowAtIndexPath(indexPath)!)
        } else if indexPath.section == 3 && indexPath.row == 0 {
            navigateToNotificationsSettings()
        } else if indexPath.section == 3 && indexPath.row == 1 {
            navigateToPrivacySettings()
        }
    }

    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        if indexPath.section == 0 && indexPath.row == 0 {
            return 200
        } else if phones != nil && indexPath.section == 2 {
            return 55
        }
        return 44
    }
    
    func tableView(tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        if section < 2 {
            return 0.0
        }
        return CGFloat(15.0)
    }
    
    func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if section < 3 {
            return 0.0
        }
        return CGFloat(15.0)
    }
    
    func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView {
        return UIView()
    }
    
    func tableView(tableView: UITableView, viewForFooterInSection section: Int) -> UIView {
        return UIView()
    }
    
    // MARK: -
    // MARK: Navigation
    
    private func navigateToNotificationsSettings() {
        let notificationsSettingsController = AASettingsNotificationsController()
        notificationsSettingsController.hidesBottomBarWhenPushed = true
        navigationController?.pushViewController(notificationsSettingsController, animated: true)
    }
    
    private func navigateToPrivacySettings() {
        let privacySettingsController = AASettingsPrivacyController(user: user)
        privacySettingsController.hidesBottomBarWhenPushed = true
        navigationController?.pushViewController(privacySettingsController, animated: true)
    }
    
}

// MARK: -
// MARK: UIActionSheet Delegate

extension AASettingsController: UIActionSheetDelegate {
    func actionSheet(actionSheet: UIActionSheet, didDismissWithButtonIndex buttonIndex: Int) {

        // Cancel button
        if (buttonIndex == 0) {
            return
        }
        
        if (buttonIndex == 1 || buttonIndex == 2) {
            let takePhoto = (buttonIndex == 1)
            var picker = AAImagePickerController()
            picker.sourceType = (takePhoto ? UIImagePickerControllerSourceType.Camera : UIImagePickerControllerSourceType.PhotoLibrary)
            picker.mediaTypes = [kUTTypeImage]
            picker.view.backgroundColor = MainAppTheme.list.bgColor
            picker.navigationBar.tintColor = MainAppTheme.navigation.barColor
            picker.delegate = self
            picker.navigationBar.tintColor = MainAppTheme.navigation.titleColor
            picker.navigationBar.titleTextAttributes = [NSForegroundColorAttributeName: MainAppTheme.navigation.titleColor]
            self.navigationController!.presentViewController(picker, animated: true, completion: nil)
        } else if (buttonIndex == 3) {
            MSG.removeAvatar()
        }
    }
}

// MARK: -
// MARK: UIImagePickerController Delegate

extension AASettingsController: UIImagePickerControllerDelegate, PECropViewControllerDelegate, UINavigationControllerDelegate {
    
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
        MSG.changeOwnAvatar(croppedImage)
        navigationController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    func cropViewControllerDidCancel(controller: PECropViewController!) {
        navigationController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    // TODO: Allow to crop rectangle
    func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage!, editingInfo: [NSObject : AnyObject]!) {
        MainAppTheme.navigation.applyStatusBar()
        navigationController!.dismissViewControllerAnimated(true, completion: nil)
        cropImage(image)
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [NSObject : AnyObject]) {
        MainAppTheme.navigation.applyStatusBar()
        
        let image = info[UIImagePickerControllerOriginalImage] as! UIImage
        navigationController!.dismissViewControllerAnimated(true, completion: nil)
        cropImage(image)
    }
    
    func imagePickerControllerDidCancel(picker: UIImagePickerController) {
        MainAppTheme.navigation.applyStatusBar()
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
}

// MARK: -
// MARK: UIAlertView Delegate

extension AASettingsController: UIAlertViewDelegate {
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        if (buttonIndex == 1) {
            let textField = alertView.textFieldAtIndex(0)!
            if count(textField.text) > 0 {
                execute(MSG.editMyNameWithNSString(textField.text))
            }
        }
    }
}

