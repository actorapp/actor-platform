//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit
import MobileCoreServices 

class SettingsViewController: AATableViewController {
    
    // MARK: -
    // MARK: Private vars
    
    private let UserInfoCellIdentifier = "UserInfoCellIdentifier"
    private let TitledCellIdentifier = "TitledCellIdentifier"
    private let TextCellIdentifier = "TextCellIdentifier"
    
    private var tableData: UABaseTableData!
    
    private let uid: Int
    private var user: ACUserVM?
    private var binder = Binder()
    
    private var phones: JavaUtilArrayList?
    
    // MARK: -
    // MARK: Constructors
    
    init() {
        uid = Int(Actor.myUid())
        super.init(style: UITableViewStyle.Plain)
        
        var title = "";
        if (MainAppTheme.tab.showText) {
            title = NSLocalizedString("TabSettings", comment: "Settings Title")
        }
        
        tabBarItem = UITabBarItem(title: title,
            image: MainAppTheme.tab.createUnselectedIcon("TabIconSettings"),
            selectedImage: MainAppTheme.tab.createSelectedIcon("TabIconSettingsHighlighted"))
        
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
        
        title = localized("TabSettings")

        user = Actor.getUserWithUid(jint(uid))
        
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        tableView.clipsToBounds = false
        tableView.tableFooterView = UIView()
        
        tableData = UATableData(tableView: tableView)
        tableData.registerClass(AvatarCell.self, forCellReuseIdentifier: UserInfoCellIdentifier)
        tableData.registerClass(TitledCell.self, forCellReuseIdentifier: TitledCellIdentifier)
        tableData.registerClass(TextCell.self, forCellReuseIdentifier: TextCellIdentifier)
        
        var section = tableData.addSection(true)
            .setFooterHeight(15)

        // Avatar
        section.addCustomCell { (tableView, indexPath) -> UITableViewCell in
            let cell: AvatarCell = tableView.dequeueReusableCellWithIdentifier(self.UserInfoCellIdentifier, forIndexPath: indexPath) as! AvatarCell
            cell.selectionStyle = .None
            if self.user != nil {
                cell.titleLabel.text = self.user!.getNameModel().get()
            }
            cell.didTap = { () -> () in
                let avatar = self.user!.getAvatarModel().get()
                if avatar != nil && avatar.getFullImage() != nil {
                    self.presentViewController(PhotoPreviewController(file: avatar.getFullImage().getFileReference(), fromView: cell.avatarView), animated: true, completion: nil)
                }
            }
            return cell
        }.setHeight(92)
        
        // Profile: Set Photo
        section.addActionCell("SettingsSetPhoto", actionClosure: { () -> Bool in
            let hasCamera = UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera)
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
                                Actor.removeMyAvatar()
                        })
                    } else if index >= 0 {
                        let takePhoto: Bool = (index == 0) && hasCamera
                        self.pickAvatar(takePhoto, closure: { (image) -> () in
                            Actor.changeOwnAvatar(image)
                        })
                    }
            })
            return true
        })
        
        // Profile: Set Name
        section.addActionCell("SettingsChangeName", actionClosure: { () -> Bool in
            
            let alertView = UIAlertView(title: nil,
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
                    if textField.text!.length > 0 {
                        self.execute(Actor.editMyNameCommandWithName(textField.text))
                    }
                }
            }
            
            alertView.show()
            
            return true
        })
        
        
        section = tableData.addSection(true)
            .setHeaderHeight(15)
            .setFooterHeight(15)
        
        // Nick
        section
            .addCustomCell { (tableView, indexPath) -> UITableViewCell in
                let cell: TitledCell = tableView.dequeueReusableCellWithIdentifier(self.TitledCellIdentifier, forIndexPath: indexPath) as! TitledCell
                
                cell.enableNavigationIcon()
                
                if let nick = self.user!.getNickModel().get() {
                    cell.setTitle(localized("ProfileUsername"), content: "@\(nick)")
                    cell.setAction(false)
                } else {
                    cell.setTitle(localized("ProfileUsername"), content: localized("SettingsUsernameNotSet"))
                    cell.setAction(true)
                }
                
                return cell
                
            }
            .setHeight(55)
            .setCopy(self.user!.getNickModel().get())
            .setAction { () -> Bool in
                self.textInputAlert("SettingsUsernameTitle", content: self.user!.getNickModel().get(), action: "AlertSave") { (nval) -> () in
                    var nNick: String? = nval.trim()
                    if nNick?.length == 0 {
                        nNick = nil
                    }
                    self.executeSafe(Actor.editMyNickCommandWithNick(nNick))
                }
            return true
        }
        
        var about = self.user!.getAboutModel().get()
        if about == nil {
            about = localized("SettingsAboutNotSet")
        }
        let aboutCell = section
            .addTextCell(localized("ProfileAbout"), text: about)
            .setEnableNavigation(true)
        
        if self.user!.getAboutModel().get() == nil {
            aboutCell.setIsAction(true)
        } else {
            aboutCell.setCopy(self.user!.getAboutModel().get())
        }
        
        aboutCell.setAction { () -> Bool in
            var text = self.user!.getAboutModel().get()
            if text == nil {
                text = ""
            }
            let controller = EditTextController(title: localized("SettingsChangeAboutTitle"), actionTitle: localized("NavigationSave"), content: text, completition: { (newText) -> () in
                var updatedText: String? = newText.trim()
                if updatedText?.length == 0 {
                    updatedText = nil
                }
                self.execute(Actor.editMyAboutCommandWithNick(updatedText))
            })
            let navigation = AANavigationController(rootViewController: controller)
            self.presentViewController(navigation, animated: true, completion: nil)
            
            return false
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
        
        // Settings
        section = tableData.addSection(true)
            .setHeaderHeight(15)
            .setFooterHeight(15)
        
        // Settings: Notifications
        section.addNavigationCell("SettingsNotifications") { () -> Bool in
            self.navigateNext(SettingsNotificationsViewController(), removeCurrent: false)
            return false
        }
        
        // Settings: Privacy
        section.addNavigationCell("SettingsSecurity") { () -> Bool in
            self.navigateNext(SettingsPrivacyViewController(), removeCurrent: false)
            return false
        }
        
        // Support
        section = tableData.addSection(true)
            .setHeaderHeight(15)
            .setFooterHeight(15)
        
        // Support: Ask Question
        if let account = AppConfig.supportAccount {
            section.addNavigationCell("SettingsAskQuestion") { () -> Bool in
                self.executeSafe(Actor.findUsersCommandWithQuery(account)) { (val) -> Void in
                    var user:ACUserVM!
                    if let users = val as? IOSObjectArray {
                        if Int(users.length()) > 0 {
                            if let tempUser = users.objectAtIndex(0) as? ACUserVM {
                                user = tempUser
                            }
                        }
                    }
                    self.navigateDetail(ConversationViewController(peer: ACPeer.userWithInt(user.getId())))
                }
                return true
            }
        }
        
        // Support: Twitter
        if let twitter = AppConfig.appTwitter {
            section.addNavigationCell("SettingsTwitter") { () -> Bool in
                UIApplication.sharedApplication().openURL(NSURL(string: "https://twitter.com/\(twitter)")!)
                return false
            }
        }
        
        // Support: Home page
        if let homePage = AppConfig.appHomePage {
            section.addNavigationCell("SettingsAbout") { () -> Bool in
                UIApplication.sharedApplication().openURL(NSURL(string: homePage)!)
                return false
            }
        }
        
        // Support: App version
        let version = NSBundle.mainBundle().infoDictionary!["CFBundleShortVersionString"] as! String
        section.addCommonCell()
            .setContent(NSLocalizedString("SettingsVersion", comment: "Version").stringByReplacingOccurrencesOfString("{version}", withString: version, options: NSStringCompareOptions(), range: nil))
            .setStyle(.Hint)
        
        // Bind
        
        tableView.reloadData()
        
        binder.bind(user!.getNameModel()!) { (value: String?) -> () in
            if value == nil {
                return
            }
            
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AvatarCell {
                cell.titleLabel.text = value!
            }
        }
        
        binder.bind(user!.getAboutModel()) { (value: String?) -> () in
            var about = self.user!.getAboutModel().get()
            if about == nil {
                about = localized("SettingsAboutNotSet")
                aboutCell.setIsAction(true)
            } else {
                aboutCell.setIsAction(false)
            }
            aboutCell.setContent(localized("ProfileAbout"), text: about)
            self.tableView.reloadRowsAtIndexPaths([NSIndexPath(forRow: 1, inSection: 1)], withRowAnimation: UITableViewRowAnimation.Automatic)
        }
        
        binder.bind(user!.getNickModel()) { (value: String?) -> () in
            self.tableView.reloadRowsAtIndexPaths([NSIndexPath(forRow: 0, inSection: 1)], withRowAnimation: UITableViewRowAnimation.Automatic)
        }
        
        binder.bind(Actor.getOwnAvatarVM().getUploadState(), valueModel2: user!.getAvatarModel()) { (upload: ACAvatarUploadState?, avatar:  ACAvatar?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AvatarCell {
                if (upload != nil && upload!.isUploading().boolValue) {
                    cell.avatarView.bind(self.user!.getNameModel().get(), id: jint(self.uid), fileName: upload?.getDescriptor())
                    cell.progress.hidden = false
                    cell.progress.startAnimating()
                } else {
                    cell.avatarView.bind(self.user!.getNameModel().get(), id: jint(self.uid), avatar: avatar, clearPrev: false)
                    cell.progress.hidden = true
                    cell.progress.stopAnimating()
                }
            }
        }
        
        binder.bind(user!.getPresenceModel()) { (presence: ACUserPresence?) -> () in
            let presenceText = Actor.getFormatter().formatPresence(presence, withSex: self.user!.getSex())
            if presenceText != nil {
                if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AvatarCell {
                    cell.subtitleLabel.text = presenceText
                    
                    if presence!.getState().ordinal() == jint(ACUserPresence_State.ONLINE.rawValue) {
                        cell.subtitleLabel.applyStyle("user.online")
                    } else {
                        cell.subtitleLabel.applyStyle("user.offline")
                    }
                }
            }
        }
        
        binder.bind(user!.getPhonesModel(), closure: { (phones: JavaUtilArrayList?) -> () in
            if phones != nil {
                self.phones = phones
                self.tableView.reloadData()
            }
        })
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        Actor.onProfileOpenWithUid(jint(uid))
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        Actor.onProfileClosedWithUid(jint(uid))
    }
}

