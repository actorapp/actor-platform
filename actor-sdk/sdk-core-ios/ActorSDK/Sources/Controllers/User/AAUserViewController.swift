//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

class AAUserViewController: AAContentTableController {
    
    var headerRow: AAAvatarRow!
    var isContactRow: AACommonRow!
    
    init(uid: Int) {
        super.init(style: AAContentTableStyle.SettingsPlain)
        
        self.uid = uid
        self.autoTrack = true
        
        self.title = AALocalized("ProfileTitle")
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
                        if presence!.state.ordinal() == ACUserPresence_State.ONLINE().ordinal() {
                            r.subtitleColor = self.appStyle.userOnlineColor
                        } else {
                            r.subtitleColor = self.appStyle.userOfflineColor
                        }
                    } else {
                        r.subtitle = "bot"
                        r.subtitleColor = self.appStyle.userOnlineColor
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
            
            if (ActorSDK.sharedActor().enableCalls && !self.isBot) {
                // Profile: Starting Voice Call
                s.action("CallsStartAudio") { (r) -> () in
                    r.selectAction = { () -> Bool in
                        self.execute(Actor.doCallWithUid(jint(self.uid)))
                        return false
                    }
                }
            }
            
            // Profile: Send messages
            s.action("ProfileSendMessage") { (r) -> () in
                r.selectAction = { () -> Bool in
                    if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer.userWithInt(jint(self.uid))) {
                        self.navigateDetail(customController)
                    } else {
                        self.navigateDetail(ConversationViewController(peer: ACPeer.userWithInt(jint(self.uid))))
                    }
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
                s.arrays { (r: AAManagedArrayRows<ACUserPhone, AATitledCell>) -> () in
                    r.height = 55
                    r.data = self.user.getPhonesModel().get().toSwiftArray()
                    r.bindData = { (c: AATitledCell, d: ACUserPhone) -> () in
                        c.setContent(AALocalized("SettingsMobilePhone"), content: "+\(d.phone)", isAction: false)
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
                            ActorSDK.sharedActor().openUrl("telprompt://+\(phoneNumber)")
                        }
                        return true
                    }
                }
                
                // Contact: Emails
                s.arrays { (r: AAManagedArrayRows<ACUserEmail, AATitledCell>) -> () in
                    r.height = 55
                    r.data = self.user.getEmailsModel().get().toSwiftArray()
                    r.bindData = { (c: AATitledCell, d: ACUserEmail) -> () in
                        c.setContent(d.title, content: d.email, isAction: false)
                    }
                    r.bindCopy = { (d: ACUserEmail) -> String? in
                        return d.email
                    }
                    r.selectAction = { (c: ACUserEmail) -> Bool in
                        ActorSDK.sharedActor().openUrl("mailto:\(c.email)")
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
                r.content = AALocalized("ProfileNotifications")
                
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
                        r.content = AALocalized("ProfileRemoveFromContacts")
                        r.style = .Destructive
                    } else {
                        r.content = AALocalized("ProfileAddToContacts")
                        r.style = .Action
                    }
                }
                
                r.selectAction = { () -> Bool in
                    if (self.user.isContactModel().get().booleanValue()) {
                        self.execute(Actor.removeContactCommandWithUid(jint(self.uid))!)
                    } else {
                        self.execute(Actor.addContactCommandWithUid(jint(self.uid))!)
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
                                    c.executeSafeOnlySuccess(Actor.editNameCommandWithUid(jint(self.uid), withName: d)!, successBlock: { (val) -> Void in
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
        
        if !self.isBot {
            // Block Contact
            section { (s) -> () in
                s.common { (r) -> () in
                    r.bindAction = { (r) -> () in
                        if !self.user.isBlockedModel().get().booleanValue() {
                            r.content = AALocalized("ProfileBlockContact")
                        } else {
                            r.content = AALocalized("ProfileUnblockContact")
                        }
                        r.style = .Action
                    }
        
                    r.selectAction = { () -> Bool in
                        if !self.user.isBlockedModel().get().booleanValue() {
                            self.executePromise(Actor.blockUser(jint(self.uid)),
                                successBlock: { success in
                                    dispatch_async(dispatch_get_main_queue(),{
                                        let peer = ACPeer.userWithInt(jint(self.uid))
                                        self.execute(Actor.deleteChatCommandWithPeer(peer))
                                        r.reload()
                                    })
                                } ,failureBlock:nil)
                        } else {
                            self.executePromise(Actor.unblockUser(jint(self.uid)),
                                successBlock: { success in
                                    dispatch_async(dispatch_get_main_queue(),{
                                        r.reload()
                                    })
                                } ,failureBlock:nil)
                        }
                        r.reload()
                        return true
                    }
                }
            }
        }
    }
    
    override func tableWillBind(binder: AABinder) {
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
