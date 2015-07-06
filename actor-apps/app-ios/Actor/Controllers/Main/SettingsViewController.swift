//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit
import MobileCoreServices 

class SettingsViewController: AATableViewController {
    
    // MARK: -
    // MARK: Private vars
    
    private let UserInfoCellIdentifier = "UserInfoCellIdentifier"
    private let TitledCellIdentifier = "TitledCellIdentifier"
    
    private var tableData: UATableData!
    
    private let uid: Int
    private var user: AMUserVM?
    private var binder = Binder()
    
    private var phones: JavaUtilArrayList?
    
    // MARK: -
    // MARK: Constructors
    
    init() {
        uid = Int(MSG.myUid())
        super.init(style: UITableViewStyle.Plain)
        
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

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -

    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = MainAppTheme.list.bgColor
        edgesForExtendedLayout = UIRectEdge.Top
        automaticallyAdjustsScrollViewInsets = false

        user = MSG.getUserWithUid(jint(uid))
        
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
        
        // Profile
        var topSection = tableData.addSection()
        topSection.setHeaderHeight(15)
        topSection.setFooterHeight(15)
        
        // Profile: Set Photo
        topSection.addActionCell("SettingsSetPhoto", actionClosure: { () -> () in
            var hasCamera = UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera)
            self.showActionSheet(hasCamera ? ["PhotoCamera", "PhotoLibrary"] : ["PhotoLibrary"],
                cancelButton: "AlertCancel",
                destructButton: self.user!.getAvatarModel().get() != nil ? "PhotoRemove" : nil,
                sourceView: self.view,
                sourceRect: self.view.bounds,
                tapClosure: { (index) -> () in
                    if index == -2 {
                        self.confirmUser("PhotoRemoveGroupMessage",
                            action: "PhotoRemove",
                            cancel: "AlertCancel",
                            sourceView: self.view,
                            sourceRect: self.view.bounds,
                            tapYes: { () -> () in
                                MSG.removeMyAvatar()
                            })
                    } else if index >= 0 {
                        let takePhoto: Bool = (index == 0) && hasCamera
                        self.pickAvatar(takePhoto, closure: { (image) -> () in
                            MSG.changeOwnAvatar(image)
                        })
                    }
            })
        }).hideTopSeparator().hideBottomSeparator()
        
        // Profile: Set Name
        topSection.addActionCell("SettingsChangeName", actionClosure: { () -> () in

            var alertView = UIAlertView(title: nil,
                message: NSLocalizedString("SettingsEditHeader", comment: "Title"),
                delegate: nil,
                cancelButtonTitle: NSLocalizedString("AlertCancel", comment: "Cancel Title"))
            
            alertView.addButtonWithTitle(NSLocalizedString("AlertSave", comment: "Save Title"))
            alertView.alertViewStyle = UIAlertViewStyle.PlainTextInput
            alertView.textFieldAtIndex(0)!.autocapitalizationType = UITextAutocapitalizationType.Words
            alertView.textFieldAtIndex(0)!.text = self.user!.getNameModel().get()
            alertView.textFieldAtIndex(0)?.keyboardAppearance = MainAppTheme.common.isDarkKeyboard ? UIKeyboardAppearance.Dark : UIKeyboardAppearance.Light
            
            alertView.tapBlock = { (alertView, buttonIndex) -> () in
                if (buttonIndex == 1) {
                    let textField = alertView.textFieldAtIndex(0)!
                    if count(textField.text) > 0 {
                        self.execute(MSG.editMyNameCommandWithName(textField.text))
                    }
                }
            }
            
            alertView.show()
        }).showTopSeparator(15)

        // Settings
        var actionsSection = tableData.addSection()
            .setHeaderHeight(15)
            .setFooterHeight(15)
        
        // Settings: Notifications
        actionsSection.addNavigationCell("SettingsNotifications", actionClosure: { () -> () in
            self.navigateNext(SettingsNotificationsViewController(), removeCurrent: false)
        }).showBottomSeparator(15)
        
        // Settings: Privacy
        actionsSection.addNavigationCell("SettingsSecurity", actionClosure: { () -> () in
            self.navigateNext(SettingsPrivacyViewController(), removeCurrent: false)
        }).hideTopSeparator()
        
        // Support
        var supportSection = tableData.addSection()
            .setHeaderHeight(15)
            .setFooterHeight(15)
        
        // Support: Ask Question
        supportSection.addNavigationCell("SettingsAskQuestion", actionClosure: { () -> () in
            self.execute(MSG.findUsersCommandWithQuery("75551234567"), successBlock: { (val) -> Void in
                var user:AMUserVM!
                if let users = val as? IOSObjectArray {
                    if Int(users.length()) > 0 {
                        if let tempUser = users.objectAtIndex(0) as? AMUserVM {
                            user = tempUser
                        }
                    }
                }
                self.navigateDetail(ConversationViewController(peer: AMPeer.userWithInt(user.getId())))
            }, failureBlock: { (val) -> Void in
                // TODO: Implement
            })
        }).showBottomSeparator(15)
        
        // Support: Ask Question
        supportSection.addNavigationCell("SettingsAbout", actionClosure: { () -> () in
            UIApplication.sharedApplication().openURL(NSURL(string: "https://actor.im")!)
        }).showBottomSeparator(15).hideTopSeparator()
        
        // Support: App version
        var version = NSBundle.mainBundle().infoDictionary!["CFBundleShortVersionString"] as! String
        supportSection.addCommonCell()
            .setContent("App Version: \(version)")
            .setStyle(.Hint)
            .hideTopSeparator()
        
        // Bind
        
        tableView.reloadData()
        
        binder.bind(user!.getNameModel()!, closure: { (value: String?) -> () in
            if value == nil {
                return
            }
            
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? UserPhotoCell {
                cell.setUsername(value!)
            }
        })
        
        binder.bind(MSG.getOwnAvatarVM().getUploadState(), valueModel2: user!.getAvatarModel()) { (upload: AMAvatarUploadState?, avatar:  AMAvatar?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? UserPhotoCell {
                if (upload != nil && upload!.isUploading().boolValue) {
                    cell.userAvatarView.bind(self.user!.getNameModel().get(), id: jint(self.uid), fileName: upload?.getDescriptor())
                    cell.setProgress(true)
                } else {
                    cell.userAvatarView.bind(self.user!.getNameModel().get(), id: jint(self.uid), avatar: avatar, clearPrev: false)
                    cell.setProgress(false)
                }
            }
        }
        
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
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        MSG.onProfileOpenWithUid(jint(uid))
        
        MainAppTheme.navigation.applyStatusBar()
        navigationController?.navigationBar.shadowImage = UIImage()

        applyScrollUi(tableView)
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        MSG.onProfileClosedWithUid(jint(uid))
        
        navigationController?.navigationBar.lt_reset()
    }
}

