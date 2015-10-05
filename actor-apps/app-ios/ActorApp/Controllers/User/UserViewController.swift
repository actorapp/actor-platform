//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class UserViewController: ACContentTableController {
    
    var headerRow: ACAvatarRow!
    var isContactRow: ACCommonRow!

    init(uid: Int) {
        super.init(style: ACContentTableStyle.SettingsPlain)

        self.uid = uid
        self.autoTrack = true
        
        self.title = localized("ProfileTitle")
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func tableDidLoad() {
        
        // Profile
        section { (s) -> () in

            // Profile: Avatar
            self.headerRow = s.avatar { (r) -> () in
                
                r.bindAction = { (r) -> () in
                    r.id = self.uid
                    r.title = self.user.getNameModel().get()
                    r.avatar = self.user.getAvatarModel().get()
                    
                    let presence = self.user.getPresenceModel().get()
                    let presenceText = Actor.getFormatter().formatPresence(presence, withSex: self.user.getSex())
                    
                    if !self.isBot {
                        r.subtitle = presenceText
                        if presence!.state.ordinal() == jint(ACUserPresence_State.ONLINE.rawValue) {
                            r.subtitleStyle = "user.online"
                        } else {
                            r.subtitleStyle = "user.offline"
                        }
                    } else {
                        r.subtitleStyle = "user.online"
                        r.subtitle = "bot"
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
            
            // Profile: Send messages
            s.action("ProfileSendMessage") { (r) -> () in
                r.selectAction = { () -> Bool in
                    self.navigateDetail(ConversationViewController(peer: ACPeer.userWithInt(jint(self.uid))))
                    self.popover?.dismissPopoverAnimated(true)
                    return false
                }
            }
        }
        
        let nick = self.user.getNickModel().get()
        let about = self.user.getAboutModel().get()
        
        if !self.isBot || nick != nil || about != nil {

            // Contact
            section { (s) -> () in
                
                // Contact: Nickname
                if let n = nick {
                    s.titled("ProfileUsername", content: "@\(n)")
                }
                
                // Contact: Phones
                s.arrays { (r: ACManagedArrayRows<ACUserPhone, TitledCell>) -> () in
                    r.height = 55
                    r.data = self.user.getPhonesModel().get().toSwiftArray()
                    r.bindData = { (c: TitledCell, d: ACUserPhone) -> () in
                        c.setTitle(d.title, content: "+\(d.phone)")
                    }
                    r.bindCopy = { (d: ACUserPhone) -> String? in
                        return "+\(d.phone)"
                    }
                    r.selectAction = { (c: ACUserPhone) -> Bool in
                        let phoneNumber = c.phone
                        let hasPhone = UIApplication.sharedApplication().canOpenURL(NSURL(string: "telprompt://")!)
                        if (!hasPhone) {
                            UIPasteboard.generalPasteboard().string = "+\(phoneNumber)"
                            self.alertUser("NumberCopied")
                        } else {
                            UIApplication.sharedApplication().openURL(NSURL(string: "telprompt://+\(phoneNumber)")!)
                        }
                        return true
                    }
                }
                
                // Contact: About
                if let a = about {
                    s.text("ProfileAbout", content: a)
                }
            }
        }
        
        section { (s) -> () in
            s.common { (r) -> () in
                let peer = ACPeer.userWithInt(jint(self.uid))
                r.style = .Switch
                r.content = localized("ProfileNotifications")
                
                r.bindAction = { (r) -> () in
                    r.switchOn = Actor.isNotificationsEnabledWithPeer(peer)
                }
                
                r.switchAction = { (on: Bool) -> () in
                    if !on && !self.user.isBot().boolValue {
                        self.confirmAlertUser("ProfileNotificationsWarring",
                            action: "ProfileNotificationsWarringAction",
                            tapYes: { () -> () in
                                Actor.changeNotificationsEnabledWithPeer(peer, withValue: false)
                            }, tapNo: { () -> () in
                                r.reload()
                        })
                        return
                    }
                    Actor.changeNotificationsEnabledWithPeer(peer, withValue: on)
                }
            }
        }
        
        
        // Edit contact
        section { (s) -> () in
                
            // Edit contact: Add/Remove
            self.isContactRow = s.common { (r) -> () in
                r.bindAction = { (r) -> () in
                    if self.user.isContactModel().get().booleanValue() {
                        r.content = localized("ProfileRemoveFromContacts")
                        r.style = .Destructive
                    } else {
                        r.content = localized("ProfileAddToContacts")
                        r.style = .Action
                    }
                }
                r.selectAction = { () -> Bool in
                    if (self.user.isContactModel().get().booleanValue()) {
                        self.execute(Actor.removeContactCommandWithUid(jint(self.uid)))
                    } else {
                        self.execute(Actor.addContactCommandWithUid(jint(self.uid)))
                    }
                    return true
                }
            }
            
            if !self.isBot {
                
                // Edit contact: Renaming
                s.action("ProfileRename") { (r) -> () in
                    r.selectAction = { () -> Bool in
                        
                        func renameUser() {
                            self.startEditField { (c) -> () in
                                
                                c.title = "ProfileEditHeader"
                                c.initialText = self.user.getNameModel().get()
                                
                                c.didDoneTap = { (d, c) in
                                    if d.length == 0 {
                                        return
                                    }
                                    
                                    c.executeSafeOnlySuccess(Actor.editNameCommandWithUid(jint(self.uid), withName: d), successBlock: { (val) -> Void in
                                        c.dismiss()
                                    })
                                }
                            }
                        }
                        
                        if (!Actor.isRenameHintShown()) {
                            self.confirmAlertUser("ProfileRenameMessage",
                                action: "ProfileRenameAction",
                                tapYes: { () -> () in
                                    renameUser()
                            })
                        } else {
                            renameUser()
                        }
                        return true
                    }
                }
            }

        }
        
    }
    
    override func tableWillBind(binder: Binder) {
        binder.bind(user.getAvatarModel(), closure: { (value: ACAvatar?) -> () in
            self.headerRow.reload()
        })
        
        binder.bind(user.getNameModel(), closure: { ( name: String?) -> () in
            self.headerRow.reload()
        })
        
        if !isBot {
            binder.bind(user.getPresenceModel(), closure: { (presence: ACUserPresence?) -> () in
                self.headerRow.reload()
            })
            
            binder.bind(user.isContactModel(), closure: { (contect: ARValueModel?) -> () in
                self.isContactRow.reload()
            })
        }
    }
}
