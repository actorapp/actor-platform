//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit
import MobileCoreServices 

class SettingsViewController: ACContentTableController {
    
    private let uid: Int
    private var user: ACUserVM?
    
    private var phones: ACArrayListUserPhone?
    
    private var aboutCell: UATextCellRegion!
    
    init() {
        uid = Int(Actor.myUid())
        super.init(tableViewStyle: UITableViewStyle.Plain)
        
        applyStyle("controller.settings")
        
        tabBarItem = UITabBarItem(title: localized("TabSettings"),
            image: MainAppTheme.tab.createUnselectedIcon("TabIconSettings"),
            selectedImage: MainAppTheme.tab.createSelectedIcon("TabIconSettingsHighlighted"))
        
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func tableDidLoad() {
        
        view.backgroundColor = MainAppTheme.list.backyardColor
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        
        user = Actor.getUserWithUid(jint(uid))
        
        section { [unowned self] (s) -> () in
            
            s.addCustomCell { [unowned self] (tableView, indexPath) -> UITableViewCell in
                let cell: AvatarCell = self.managedTable.dequeueCell(indexPath)
                cell.titleLabel.text = self.user!.getNameModel().get()
                cell.didTap = { [unowned self] () -> () in
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

            // Profile: Set Photo
            s.addActionCell("SettingsSetPhoto", actionClosure: { [unowned self] () -> Bool in
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
            s.addActionCell("SettingsChangeName", actionClosure: { [unowned self] () -> Bool in
                
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

            
        }
        
        section { (s) -> () in

            s.next("SettingsNotifications", closure: { () -> UIViewController in
                return SettingsNotificationsViewController()
            })
            
            s.next("SettingsSecurity", closure: { () -> UIViewController in
                return SettingsPrivacyViewController()
            })
            
            s.addCustomCell { [unowned self] (tableView, indexPath) -> UITableViewCell in
                let cell: WallpapperSettingsCell = self.managedTable.dequeueCell(indexPath)
                cell.wallpapperDidTap = { (name) -> () in
                    self.presentViewController(WallpapperPreviewController(imageName: name), animated: true, completion: nil)
                }
                return cell
                }.setAction { [unowned self] () -> Bool in
                    self.navigateNext(SettingsWallpapper(), removeCurrent: false)
                    return false
            }.setHeight(230)
        }
        
        section { (s) -> () in
            
            // Profile: Nick
            s.addCustomCell { [unowned self] (tableView, indexPath) -> UITableViewCell in
                    let cell = self.managedTable.dequeueTitledCell(indexPath)
                    
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
                .setAction { [unowned self] () -> Bool in
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
            
            self.aboutCell = s
                .addTextCell(localized("ProfileAbout"), text: about)
                .setEnableNavigation(true)
            
            if self.user!.getAboutModel().get() == nil {
                self.aboutCell.setIsAction(true)
            } else {
                self.aboutCell.setCopy(self.user!.getAboutModel().get())
            }
            
            self.aboutCell.setAction { [unowned self] () -> Bool in
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
            
            // Profile: Phones
            s.addCustomCells(55, countClosure: { [unowned self] () -> Int in
                    if (self.phones != nil) {
                        return Int(self.phones!.size())
                    }
                    return 0
                    }) { (tableView, index, indexPath) -> UITableViewCell in
                        let cell = self.managedTable.dequeueTitledCell(indexPath)
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

        }
        
        section { (s) -> () in

            // Support: Ask Question
            if let account = AppConfig.supportAccount {
                s.navigate("SettingsAskQuestion", closure: { [unowned self] () -> () in
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
                })
            }
            
            // Support: Twitter
            if let twitter = AppConfig.appTwitter {
                s.url("SettingsTwitter", url: "https://twitter.com/\(twitter)")
            }
            
            // Support: Home page
            if let homePage = AppConfig.appHomePage {
                s.url("SettingsAbout", url: homePage)
            }
            
            // Support: App version
            let version = NSBundle.mainBundle().infoDictionary!["CFBundleShortVersionString"] as! String
            s.hint(version.replace("{version}", dest: version))
        }
    }

    
    override func tableWillBind(binder: Binder) {
        
        // Bind name
        
        binder.bind(user!.getNameModel()!) { [unowned self] (value: String?) -> () in
            if value == nil {
                return
            }
            
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AvatarCell {
                cell.titleLabel.text = value!
            }
        }
        
        // Bind nick
        
        binder.bind(user!.getNickModel()) { [unowned self] (value: String?) -> () in
            self.tableView.reloadRowsAtIndexPaths([NSIndexPath(forRow: 0, inSection: 1)], withRowAnimation: UITableViewRowAnimation.Automatic)
        }
        
        // Bind about
        
        binder.bind(user!.getAboutModel()) { [unowned self] (value: String?) -> () in
            var about = self.user!.getAboutModel().get()
            if about == nil {
                about = localized("SettingsAboutNotSet")
                self.aboutCell.setIsAction(true)
            } else {
                self.aboutCell.setIsAction(false)
            }
            self.aboutCell.setContent(localized("ProfileAbout"), text: about)
            self.tableView.reloadRowsAtIndexPaths([NSIndexPath(forRow: 1, inSection: 1)], withRowAnimation: UITableViewRowAnimation.Automatic)
        }

        
        // Bind Avatar
        
        binder.bind(Actor.getOwnAvatarVM().uploadState, valueModel2: user!.getAvatarModel()) { [unowned self] (upload: ACAvatarUploadState?, avatar:  ACAvatar?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AvatarCell {
                if (upload != nil && upload!.isUploading.boolValue) {
                    cell.avatarView.bind(self.user!.getNameModel().get(), id: jint(self.uid), fileName: upload?.description)
                    cell.progress.hidden = false
                    cell.progress.startAnimating()
                } else {
                    cell.avatarView.bind(self.user!.getNameModel().get(), id: jint(self.uid), avatar: avatar, clearPrev: false)
                    cell.progress.hidden = true
                    cell.progress.stopAnimating()
                }
            }
        }
        
        // Bind Presence

        binder.bind(user!.getPresenceModel()) { [unowned self] (presence: ACUserPresence?) -> () in
            let presenceText = Actor.getFormatter().formatPresence(presence, withSex: self.user!.getSex())
            if presenceText != nil {
                if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AvatarCell {
                    cell.subtitleLabel.text = presenceText
                    
                    if presence!.state.ordinal() == jint(ACUserPresence_State.ONLINE.rawValue) {
                        cell.subtitleLabel.applyStyle("user.online")
                    } else {
                        cell.subtitleLabel.applyStyle("user.offline")
                    }
                }
            }
        }
        
        // Bind Phone
        
        binder.bind(user!.getPhonesModel(), closure: { [unowned self] (phones: ACArrayListUserPhone?) -> () in
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