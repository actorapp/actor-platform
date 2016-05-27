//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import MobileCoreServices

public class AASettingsViewController: AAContentTableController {
    
    private var phonesCells: AAManagedArrayRows<ACUserPhone, AATitledCell>!
    private var emailCells: AAManagedArrayRows<ACUserEmail, AATitledCell>!
    
    private var headerCell: AAAvatarRow!
    private var nicknameCell: AATitledRow!
    private var aboutCell: AATitledRow!
    
    public init() {
        super.init(style: AAContentTableStyle.SettingsPlain)
        
        uid = Int(Actor.myUid())
        
        content = ACAllEvents_Main.SETTINGS()
        
        tabBarItem = UITabBarItem(title: "TabSettings", img: "TabIconSettings", selImage: "TabIconSettingsHighlighted")
        
        navigationItem.title = AALocalized("TabSettings")
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func tableDidLoad() {
        
        // Profile
        section { [unowned self] (s) -> () in

            // Profile: Photo and name
            self.headerCell = s.avatar() { [unowned self] (r) -> () in

                r.bindAction = { [unowned self] (r) -> () in
                    
                    let upload = Actor.getOwnAvatarVM()!.uploadState.get() as? ACAvatarUploadState
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
                        if presence!.state.ordinal() == ACUserPresence_State.ONLINE().ordinal() {
                            r.subtitleColor = ActorSDK.sharedActor().style.userOnlineColor
                        } else {
                            r.subtitleColor = ActorSDK.sharedActor().style.userOfflineColor
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
                        
                        self.presentViewController(AAPhotoPreviewController(file: full, previewFile: small, size: size, fromView: view), animated: true, completion: nil)
                    }
                }
            }
            
            // Profile: Set Photo
            s.action("SettingsSetPhoto") { [unowned self] (r) -> () in
                r.selectAction = { [unowned self] () -> Bool in
                    let hasCamera = UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera)
                    let view = self.tableView.cellForRowAtIndexPath(NSIndexPath(forRow: 1, inSection: 0))!.contentView
                    self.showActionSheet(hasCamera ? ["PhotoCamera", "PhotoLibrary"] : ["PhotoLibrary"],
                        cancelButton: "AlertCancel",
                        destructButton: self.user.getAvatarModel().get() != nil ? "PhotoRemove" : nil,
                        sourceView: view,
                        sourceRect: view.bounds,
                        tapClosure: { [unowned self] (index) -> () in
                            if index == -2 {
                                self.confirmAlertUser("PhotoRemoveGroupMessage",
                                    action: "PhotoRemove",
                                    tapYes: { () -> () in
                                        Actor.removeMyAvatar()
                                }, tapNo: nil)
                            } else if index >= 0 {
                                let takePhoto: Bool = (index == 0) && hasCamera
                                self.pickAvatar(takePhoto, closure: { (image) -> () in
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
                            
                            c.executeSafeOnlySuccess(Actor.editMyNameCommandWithName(t)!) { (val) -> Void in
                                c.dismiss()
                            }
                        }
                    }
                    
                    return true
                }
            }
            
            ActorSDK.sharedActor().delegate.actorSettingsHeaderDidCreated(self, section: s)
        }
        

        
        // Settings
        section { (s) -> () in
            
            ActorSDK.sharedActor().delegate.actorSettingsConfigurationWillCreated(self, section: s)
            
            // Settings: Notifications
            s.navigate("SettingsNotifications", controller: AASettingsNotificationsViewController.self)
            
            // Settings: Security
            s.navigate("SettingsSecurity", controller: AASettingsPrivacyViewController.self)
            
            // Settings: Wallpapper
            s.custom({ [unowned self] (r: AACustomRow<AAWallpapperSettingsCell>) -> () in
                r.height = 230
                r.closure = { [unowned self] (cell) -> () in
                    cell.wallpapperDidTap = { [unowned self] (name) -> () in
                        self.presentViewController(AAWallpapperPreviewController(imageName: name), animated: true, completion: nil)
                    }
                    
                }
                
                r.selectAction = { () -> Bool in
                    self.navigateNext(AASettingsWallpapersController(), removeCurrent: false)
                    return false
                }

            })
            
            ActorSDK.sharedActor().delegate.actorSettingsConfigurationDidCreated(self, section: s)
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
                        r.subtitle = AALocalized("SettingsUsernameNotSet")
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
                            c.executeSafeOnlySuccess(Actor.editMyNickCommandWithNick(nNick)!, successBlock: { (val) -> Void in
                                c.dismiss()
                            })
                        }
                    }
                    
                    return AADevice.isiPad
                }
            }
            
            // Contacts: About
            self.aboutCell = s.titled("ProfileAbout") { [unowned self] (r) -> () in
                
                r.accessoryType = .DisclosureIndicator
                
                r.bindAction = { [unowned self] (r) -> () in
                    if let about = self.user.getAboutModel().get() {
                        r.subtitle = about
                        r.isAction = false
                    } else {
                        r.subtitle = AALocalized("SettingsAboutNotSet")
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
                            controller.executeSafeOnlySuccess(Actor.editMyAboutCommandWithNick(updatedText)!, successBlock: { (val) -> Void in
                                controller.dismiss()
                            })
                        }
                    }
                    
                    return AADevice.isiPad
                }
                
            }
 
            // Profile: Phones
            self.phonesCells = s.arrays() { (r: AAManagedArrayRows<ACUserPhone, AATitledCell>) -> () in

                r.height = 55
                
                r.data = self.user.getPhonesModel().get().toSwiftArray()
                
                r.bindData = { (c: AATitledCell, d: ACUserPhone) -> () in
                    c.setContent(AALocalized("SettingsMobilePhone"), content: "+\(d.phone)", isAction: false)
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
                    }
                    return true
                }
            }
            
            self.emailCells = s.arrays() { (r: AAManagedArrayRows<ACUserEmail, AATitledCell>) -> () in
                
                r.height = 55
                
                r.data = self.user.getEmailsModel().get().toSwiftArray()
                
                r.bindData = { (c: AATitledCell, d: ACUserEmail) -> () in
                    c.setContent(d.title, content: d.email, isAction: false)
                    c.accessoryType = .None
                }
                
                r.bindCopy = { (d: ACUserEmail) -> String? in
                    return d.email
                }
                
                r.selectAction = { (d: ACUserEmail) -> Bool in
                    ActorSDK.sharedActor().openUrl("mailto:\(d.email)")
                    return true
                }
            }

        }
        
        // Support
        section { (s) -> () in
            
            ActorSDK.sharedActor().delegate.actorSettingsSupportWillCreated(self, section: s)
            
            // Support: Ask Question
            if let account = ActorSDK.sharedActor().supportAccount {
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
                            if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer.userWithInt(user.getId())) {
                                self.navigateDetail(customController)
                            } else {
                                self.navigateDetail(ConversationViewController(peer: ACPeer.userWithInt(user.getId())))
                            }
                        }
                        return true
                    }
                })
            }

            // Support: Twitter
            if let twitter = ActorSDK.sharedActor().supportTwitter {
                s.url("SettingsTwitter", url: "https://twitter.com/\(twitter)")
            }

            // Support: Home page
            if let homePage = ActorSDK.sharedActor().supportHomepage {
                s.url("SettingsAbout", url: homePage)
            }

            // Support: App version
            let version = NSBundle.mainBundle().infoDictionary!["CFBundleShortVersionString"] as! String
            s.hint(AALocalized("SettingsVersion").replace("{version}", dest: version))
            
            ActorSDK.sharedActor().delegate.actorSettingsSupportDidCreated(self, section: s)
        }
    }

    public override func tableWillBind(binder: AABinder) {
        
        // Header
        
        binder.bind(user.getNameModel()) { [unowned self] (value: String?) -> () in
            self.headerCell.reload()
        }
        
        binder.bind(user.getAvatarModel()) { [unowned self] (value: ACAvatar?) -> () in
            self.headerCell.reload()
        }
        
        binder.bind(Actor.getOwnAvatarVM()!.uploadState) { [unowned self] (value: ACAvatarUploadState?) -> () in
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
        
        // Bind Email
        
        binder.bind(user.getEmailsModel(), closure: { [unowned self] (emails: ACArrayListUserEmail?) -> () in
            self.emailCells.data = (emails?.toSwiftArray())!
            self.emailCells.reload()
        })
    }
}
