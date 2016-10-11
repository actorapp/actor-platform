//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

class AAUserViewController: AAContentTableController {
    
    var headerRow: AAAvatarRow!
    var isContactRow: AACommonRow!
    
    init(uid: Int) {
        super.init(style: AAContentTableStyle.settingsPlain)
        
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
                    let presenceText = Actor.getFormatter().formatPresence(presence, with: self.user.getSex())
                    
                    if !self.isBot {
                        r.subtitle = presenceText
                        if presence!.state.ordinal() == ACUserPresence_State.online().ordinal() {
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
                    if avatar != nil && avatar?.fullImage != nil {
                        
                        let full = avatar?.fullImage.fileReference
                        let small = avatar?.smallImage.fileReference
                        let size = CGSize(width: Int((avatar?.fullImage.width)!), height: Int((avatar?.fullImage.height)!))
                        
                        self.present(AAPhotoPreviewController(file: full!, previewFile: small, size: size, fromView: view), animated: true, completion: nil)
                    }
                }
            }
            
            if (ActorSDK.sharedActor().enableCalls && !self.isBot) {
                // Profile: Starting Voice Call
                s.action("CallsStartAudio") { (r) -> () in
                    r.selectAction = { () -> Bool in
                        self.execute(Actor.doCall(withUid: jint(self.uid)))
                        return false
                    }
                }
            }
            
            // Profile: Send messages
            s.action("ProfileSendMessage") { (r) -> () in
                r.selectAction = { () -> Bool in
                    if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer.user(with: jint(self.uid))) {
                        self.navigateDetail(customController)
                    } else {
                        self.navigateDetail(ConversationViewController(peer: ACPeer.user(with: jint(self.uid))))
                    }
                    self.popover?.dismiss(animated: true)
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
                        let hasPhone = UIApplication.shared.canOpenURL(URL(string: "telprompt://")!)
                        if (!hasPhone) {
                            UIPasteboard.general.string = "+\(phoneNumber)"
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
                let peer = ACPeer.user(with: jint(self.uid))
                r.style = .switch
                r.content = AALocalized("ProfileNotifications")
                
                r.bindAction = { (r) -> () in
                    r.switchOn = Actor.isNotificationsEnabled(with: peer)
                }
                
                r.switchAction = { (on: Bool) -> () in
                    if !on && !self.user.isBot() {
                        self.confirmAlertUser("ProfileNotificationsWarring",
                            action: "ProfileNotificationsWarringAction",
                            tapYes: { () -> () in
                                Actor.changeNotificationsEnabled(with: peer, withValue: false)
                            }, tapNo: { () -> () in
                                r.reload()
                        })
                        return
                    }
                    Actor.changeNotificationsEnabled(with: peer, withValue: on)
                }
                
                if(ActorSDK.sharedActor().enableChatGroupSound) {
                    if(Actor.isNotificationsEnabled(with: peer)){
                        r.selectAction = {() -> Bool in
                            // Sound: Choose sound
                            let setRingtoneController = AARingtonesViewController()
                            let sound = Actor.getNotificationsSound(with: peer)
                            setRingtoneController.selectedRingtone = (sound != nil) ? sound! : ""
                            setRingtoneController.completion = {(selectedSound:String) in
                                Actor.changeNotificationsSound(peer, withValue: selectedSound)
                            }
                            let navigationController = AANavigationController(rootViewController: setRingtoneController)
                            if (AADevice.isiPad) {
                                navigationController.isModalInPopover = true
                                navigationController.modalPresentationStyle = UIModalPresentationStyle.currentContext
                            }
                            self.present(navigationController, animated: true, completion: {
                                }
                            )
                            return false
                        }
                    }
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
                        r.style = .destructive
                    } else {
                        r.content = AALocalized("ProfileAddToContacts")
                        r.style = .action
                    }
                }
                
                r.selectAction = { () -> Bool in
                    if (self.user.isContactModel().get().booleanValue()) {
                        self.execute(Actor.removeContactCommand(withUid: jint(self.uid))!)
                    } else {
                        self.execute(Actor.addContactCommand(withUid: jint(self.uid))!)
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
                                    c.executeSafeOnlySuccess(Actor.editNameCommand(withUid: jint(self.uid), withName: d)!, successBlock: { (val) -> Void in
                                        c.dismissController()
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
        
        // Block Contact
        section { (s) -> () in
            s.common { (r) -> () in
                r.bindAction = { (r) -> () in
                    if !self.user.isBlockedModel().get().booleanValue() {
                        r.content = AALocalized("ProfileBlockContact")
                    } else {
                        r.content = AALocalized("ProfileUnblockContact")
                    }
                    r.style = .destructive
                }
                
                r.selectAction = { () -> Bool in
                    if !self.user.isBlockedModel().get().booleanValue() {
                        self.executePromise(Actor.blockUser(jint(self.uid)),
                            successBlock: { success in
                                DispatchQueue.main.async(execute: {
                                    r.reload()
                                })
                            } ,failureBlock:nil)
                    } else {
                        self.executePromise(Actor.unblockUser(jint(self.uid)),
                            successBlock: { success in
                                DispatchQueue.main.async(execute: {
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
    
    override func tableWillBind(_ binder: AABinder) {
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
