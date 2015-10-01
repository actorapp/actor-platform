//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit
import MobileCoreServices 

class SettingsViewController: ACContentTableController {
    
    private var phonesCells: ACManagedArrayRows<ACUserPhone, TitledCell>!
    
    private var headerCell: ACAvatarRow!
    private var nicknameCell: ACTitledRow!
    private var aboutCell: ACTextRow!
    
    init() {
        super.init(style: ACContentTableStyle.SettingsPlain)
        
        uid = Int(Actor.myUid())
        
        content = ACAllEvents_Main.SETTINGS()
        
        tabBarItem = UITabBarItem(title: localized("TabSettings"),
            image: MainAppTheme.tab.createUnselectedIcon("TabIconSettings"),
            selectedImage: MainAppTheme.tab.createSelectedIcon("TabIconSettingsHighlighted"))
        
        applyStyle("controller.settings")
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func tableDidLoad() {
        
        // Profile
        section { [unowned self] (s) -> () in

            // Profile: Photo and name
            self.headerCell = s.avatar() { [unowned self] (r) -> () in

                r.bindAction = { [unowned self] (r) -> () in
                    
                    let upload = Actor.getOwnAvatarVM().uploadState.get() as? ACAvatarUploadState
                    let avatar = self.user.getAvatarModel().get()
                    let presence = self.user.getPresenceModel().get()
                    let presenceText = Actor.getFormatter().formatPresence(presence, withSex: self.user.getSex())
                    let name = self.user.getNameModel().get()
                    
                    r.id = self.uid
                    r.title = name
                    
                    if (upload != nil && upload!.isUploading.boolValue) {
                        r.avatar = nil
                        r.avatarPath = upload!.descriptor
                        r.avatarLoading = true
                    } else {
                        r.avatar = avatar
                        r.avatarPath = nil
                        r.avatarLoading = false
                    }
                    
                    if presenceText != nil {
                        r.subtitle = presenceText
                        if presence!.state.ordinal() == jint(ACUserPresence_State.ONLINE.rawValue) {
                            r.subtitleStyle = "user.online"
                        } else {
                            r.subtitleStyle = "user.offline"
                        }
                    } else {
                        r.subtitle = ""
                    }
                }
                
                r.avatarDidTap = { [unowned self] (view: UIView) -> () in
                    let avatar = self.user.getAvatarModel().get()
                    if avatar != nil && avatar.fullImage != nil {
                        
                        let full = avatar.fullImage.fileReference
                        let small = avatar.smallImage.fileReference
                        let size = CGSize(width: Int(avatar.fullImage.width), height: Int(avatar.fullImage.height))
                        
                        self.presentViewController(PhotoPreviewController(file: full, previewFile: small, size: size, fromView: view), animated: true, completion: nil)
                    }
                }
            }
            
            // Profile: Set Photo
            s.action("SettingsSetPhoto") { [unowned self] (r) -> () in
                r.selectAction = { [unowned self] () -> Bool in
                    let hasCamera = UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera)
                    self.showActionSheet(hasCamera ? ["PhotoCamera", "PhotoLibrary"] : ["PhotoLibrary"],
                        cancelButton: "AlertCancel",
                        destructButton: self.user.getAvatarModel().get() != nil ? "PhotoRemove" : nil,
                        sourceView: self.view,
                        sourceRect: self.view.bounds,
                        tapClosure: { [unowned self] (index) -> () in
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
                                self.pickAvatar(takePhoto, closure: { [unowned self] (image) -> () in
                                    Actor.changeOwnAvatar(image)
                                })
                            }
                    })
                    return true
                }
            }
            
            // Profile: Set Name
            s.action("SettingsChangeName") { [unowned self] (r) -> () in
                r.selectAction = { [unowned self] () -> Bool in
                    
                    self.startEditField { (c) -> () in
                        c.title = "SettingsEditHeader"
                        c.hint = "SettingsEditHint"
                        
                        c.initialText = self.user.getNameModel().get()
                        
                        c.fieldAutocapitalizationType = .Words
                        c.fieldHint = "SettingsEditFieldHint"
                        
                        c.didDoneTap = { (t, c) -> () in

                            if t.length == 0 {
                                return
                            }
                            
                            c.executeSafeOnlySuccess(Actor.editMyNameCommandWithName(t)) { (val) -> Void in
                                c.dismiss()
                            }
                        }
                    }
                    
                    return true
                }
            }
        }
        
//        section { (s) -> () in
//            s.action("Connect VK", closure: { (r) -> () in
//                r.selectAction = { () -> Bool in
//                    self.executeSafe(Actor.startWebAction("vkOAuth")) { (val) -> Void in
//                        if let d = val as? ACWebActionDescriptor {
//                            let controller = AANavigationController()
//                            controller.viewControllers = [WebActionController(desc: d)]
//                            self.presentViewController(controller, animated: true, completion: nil)
//                        }
//                    }
//                    return false
//                }
//            })
//        }
        
        // Settings
        section { (s) -> () in
            
            // Settings: Notifications
            s.navigate("SettingsNotifications", controller: SettingsNotificationsViewController.self)
            
            // Settings: Security
            s.navigate("SettingsSecurity", controller: SettingsPrivacyViewController.self)
            
            // Settings: Wallpapper
            s.custom({ [unowned self] (r: ACCustomRow<WallpapperSettingsCell>) -> () in
                r.height = 230
                r.closure = { [unowned self] (cell) -> () in
                    cell.wallpapperDidTap = { [unowned self] (name) -> () in
                        self.presentViewController(WallpapperPreviewController(imageName: name), animated: true, completion: nil)
                    }
                }
//                r.selectAction = { [unowned self] () -> Bool in
//                    self.navigateNext(SettingsWallpapper(), removeCurrent: false)
//                    return false
//                }
            })
        }
        
        // Contacts
        section { [unowned self] (s) -> () in

            // Contacts: Nicknames
            self.nicknameCell = s.titled("ProfileUsername") { [unowned self] (r) -> () in
                
                r.accessoryType = .DisclosureIndicator

                r.bindAction = { [unowned self] (r) -> () in
                    if let nick = self.user.getNickModel().get() {
                        r.subtitle = "@\(nick)"
                        r.isAction = false
                    } else {
                        r.subtitle = localized("SettingsUsernameNotSet")
                        r.isAction = true
                    }
                }
                
                r.selectAction = { [unowned self] () -> Bool in
                    
                    self.startEditField { (c) -> () in
                        
                        c.title = "SettingsUsernameTitle"
                        c.actionTitle = "AlertSave"
                        
                        if let nick = self.user.getNickModel().get() {
                            c.initialText = nick
                        }
                        
                        c.fieldHint = "SettingsUsernameHintField"
                        c.fieldAutocorrectionType = .No
                        c.fieldAutocapitalizationType = .None
                        c.hint = "SettingsUsernameHint"
                        
                        c.didDoneTap = { (t, c) -> () in
                            var nNick: String? = t.trim()
                            if nNick?.length == 0 {
                                nNick = nil
                            }
                            c.executeSafeOnlySuccess(Actor.editMyNickCommandWithNick(nNick), successBlock: { (val) -> Void in
                                c.dismiss()
                            })
                        }
                    }
                    
                    return false
                }
            }
            
            // Contacts: About
            self.aboutCell = s.text("ProfileAbout") { [unowned self] (r) -> () in

                r.navigate = true
                
                r.bindAction = { [unowned self] (r) -> () in
                    if let about = self.user.getAboutModel().get() {
                        r.content = about
                        r.isAction = false
                    } else {
                        r.content = localized("SettingsAboutNotSet")
                        r.isAction = true
                    }
                }
                
                r.selectAction = { [unowned self] () -> Bool in
                    
                    self.startEditText { (config) -> () in
                        
                        config.title = "SettingsChangeAboutTitle"
                        config.hint = "SettingsChangeAboutHint"
                        config.actionTitle = "NavigationSave"
                        
                        config.initialText = self.user.getAboutModel().get()
                        
                        config.didCompleteTap = { (text, controller) -> () in
                            
                            var updatedText: String? = text.trim()
                            if updatedText?.length == 0 {
                                updatedText = nil
                            }
                            controller.executeSafeOnlySuccess(Actor.editMyAboutCommandWithNick(updatedText), successBlock: { (val) -> Void in
                                controller.dismiss()
                            })
                        }
                    }
                    
                    return false
                }
            }
 
            // Profile: Phones
            self.phonesCells = s.arrays() { [unowned self] (r: ACManagedArrayRows<ACUserPhone, TitledCell>) -> () in

                r.height = 55
                
                r.data = self.user.getPhonesModel().get().toSwiftArray()
                
                r.bindData = { [unowned self] (c: TitledCell, d: ACUserPhone) -> () in
                    c.setTitle(d.title, content: "+\(d.phone)")
                    c.accessoryType = .None
                }
                
                r.bindCopy = { (d: ACUserPhone) -> String? in
                    return "+\(d.phone)"
                }
                
                r.selectAction = { [unowned self] (d: ACUserPhone) -> Bool in
                    let hasPhone = UIApplication.sharedApplication().canOpenURL(NSURL(string: "telprompt://")!)
                    if (!hasPhone) {
                        UIPasteboard.generalPasteboard().string = "+\(d.phone)"
                        self.alertUser("NumberCopied")
                    } else {
                        UIApplication.sharedApplication().openURL(NSURL(string: "telprompt://+\(d.phone)")!)
                    }
                    return true
                }
            }
        }
        
        // Support
        section { (s) -> () in

            // Support: Ask Question
            if let account = AppConfig.supportAccount {
                s.navigate("SettingsAskQuestion", closure: { (r) -> () in
                    r.selectAction = { () -> Bool in
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
        
        // Header
        
        binder.bind(user.getNameModel()!) { [unowned self] (value: String?) -> () in
            self.headerCell.reload()
        }
        
        binder.bind(user.getAvatarModel()) { [unowned self] (value: ACAvatar?) -> () in
            self.headerCell.reload()
        }
        
        binder.bind(Actor.getOwnAvatarVM().uploadState) { [unowned self] (value: ACAvatarUploadState?) -> () in
            self.headerCell.reload()
        }
        
        binder.bind(user.getPresenceModel()) { [unowned self] (presence: ACUserPresence?) -> () in
            self.headerCell.reload()
        }
        
        // Bind nick
        
        binder.bind(user.getNickModel()) { [unowned self] (value: String?) -> () in
            self.nicknameCell.reload()
        }
        
        // Bind about
        
        binder.bind(user.getAboutModel()) { [unowned self] (value: String?) -> () in
            self.aboutCell.reload()
        }

        // Bind Phone
        
        binder.bind(user.getPhonesModel(), closure: { [unowned self] (phones: ACArrayListUserPhone?) -> () in
            self.phonesCells.data = (phones?.toSwiftArray())!
            self.phonesCells.reload()
        })
    }
}