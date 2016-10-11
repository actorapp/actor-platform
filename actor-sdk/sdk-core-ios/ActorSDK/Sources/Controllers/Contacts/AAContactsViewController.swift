//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import MessageUI
import Social
import AddressBookUI
import ContactsUI
fileprivate func < <T : Comparable>(lhs: T?, rhs: T?) -> Bool {
  switch (lhs, rhs) {
  case let (l?, r?):
    return l < r
  case (nil, _?):
    return true
  default:
    return false
  }
}

fileprivate func > <T : Comparable>(lhs: T?, rhs: T?) -> Bool {
  switch (lhs, rhs) {
  case let (l?, r?):
    return l > r
  default:
    return rhs < lhs
  }
}


open class AAContactsViewController: AAContactsListContentController, AAContactsListContentControllerDelegate, UIAlertViewDelegate, MFMessageComposeViewControllerDelegate, MFMailComposeViewControllerDelegate {
    
    var inviteText: String {
        get {
            return AALocalized("InviteText").replace("{link}", dest: ActorSDK.sharedActor().inviteUrl).replace("{appname}", dest: ActorSDK.sharedActor().appName)
        }
    }
    
    public override init() {
        super.init()
        
        content = ACAllEvents_Main.contacts()
        
        tabBarItem = UITabBarItem(title: "TabPeople", img: "TabIconContacts", selImage: "TabIconContactsHighlighted")
        
        navigationItem.title = AALocalized("TabPeople")
        navigationItem.backBarButtonItem = UIBarButtonItem(title: AALocalized("ContactsBack"), style: UIBarButtonItemStyle.plain, target: nil, action: nil)
        navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.add, target: self, action: #selector(AAContactsViewController.findContact))
        
        delegate = self
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    open func contactDidTap(_ controller: AAContactsListContentController, contact: ACContact) -> Bool {
        
        if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer_userWithInt_(contact.uid)) {
            navigateDetail(customController)
        } else {
            navigateDetail(ConversationViewController(peer: ACPeer_userWithInt_(contact.uid)))
        }
        
        return true
    }
    
    open func willAddContacts(_ controller: AAContactsListContentController, section: AAManagedSection) {
        
        section.custom { (r: AACustomRow<AAContactActionCell>) -> () in
            
            r.height = 56
            
            r.closure = { (cell: AAContactActionCell)->() in
                cell.bind("ic_add_user", actionTitle: AALocalized("ContactsActionAdd"))
            }
            
            r.selectAction = { () -> Bool in
                self.findContact()
                return AADevice.isiPad
            }
        }
        
        section.custom { (r: AACustomRow<AAContactActionCell>) -> () in
            
            r.height = 56
            
            r.closure = { (cell: AAContactActionCell)->() in
                cell.bind("ic_invite_user", actionTitle: "\(AALocalized("ContactsActionInvite")) \(ActorSDK.sharedActor().appName)")
            }
            
            r.selectAction = { () -> Bool in
                
                let builder = AAMenuBuilder()
                
                if MFMessageComposeViewController.canSendText() {
                    builder.add("SMS") { () -> () in
                        self.showSmsInvitation(nil)
                    }
                }
                
                if MFMailComposeViewController.canSendMail() {
                    builder.add("Email") { () -> () in
                        self.showEmailInvitation(nil)
                    }    
                }
                
                if SLComposeViewController.isAvailable(forServiceType: SLServiceTypeTencentWeibo) {
                    builder.add("Tencent Weibo") { () -> () in
                        let vc = SLComposeViewController(forServiceType: SLServiceTypeTencentWeibo)!
                        vc.setInitialText(self.inviteText)
                        self.present(vc, animated: true, completion: nil)
                    }
                }
                
                if SLComposeViewController.isAvailable(forServiceType: SLServiceTypeSinaWeibo) {
                    builder.add("Sina Weibo") { () -> () in
                        let vc = SLComposeViewController(forServiceType: SLServiceTypeSinaWeibo)!
                        vc.setInitialText(self.inviteText)
                        self.present(vc, animated: true, completion: nil)
                    }
                }
                
                if SLComposeViewController.isAvailable(forServiceType: SLServiceTypeTwitter) {
                    builder.add("Twitter") { () -> () in
                        let vc = SLComposeViewController(forServiceType: SLServiceTypeTwitter)!
                        vc.setInitialText(self.inviteText)
                        self.present(vc, animated: true, completion: nil)
                    }
                }
                
                if SLComposeViewController.isAvailable(forServiceType: SLServiceTypeFacebook) {
                    builder.add("Facebook") { () -> () in
                        let vc = SLComposeViewController(forServiceType: SLServiceTypeFacebook)!
                        vc.add(URL(string: ActorSDK.sharedActor().inviteUrl))
                        self.present(vc, animated: true, completion: nil)
                    }
                }
                
                let view = self.tableView.cellForRow(at: IndexPath(row: 1, section: 0))!.contentView
                
                self.showActionSheet(builder.items, cancelButton: "AlertCancel", destructButton: nil, sourceView: view, sourceRect: view.bounds, tapClosure: builder.tapClosure)
                
                return true
            }
        }
    }
 
    // Searching for contact
    
    open func findContact() {
        
        startEditField { (c) -> () in
            c.title = "FindTitle"
            c.actionTitle = "NavigationFind"
            
            c.hint = "FindHint"
            c.fieldHint = "FindFieldHint"
            
            c.fieldAutocapitalizationType = .none
            c.fieldAutocorrectionType = .no
            c.fieldReturnKey = .search
            
            c.didDoneTap = { (t, c) -> () in
                
                if t.length == 0 {
                    return
                }
                
                self.executeSafeOnlySuccess(Actor.findUsersCommand(withQuery: t), successBlock: { (val) -> Void in
                    var user: ACUserVM? = nil
                    if let users = val as? IOSObjectArray {
                        if Int(users.length()) > 0 {
                            if let tempUser = users.object(at: 0) as? ACUserVM {
                                user = tempUser
                            }
                        }
                    }
                    
                    if user != nil {
                        c.execute(Actor.addContactCommand(withUid: user!.getId())!, successBlock: { (val) -> Void in
                            if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer_userWithInt_(user!.getId())) {
                                self.navigateDetail(customController)
                            } else {
                                self.navigateDetail(ConversationViewController(peer: ACPeer_userWithInt_(user!.getId())))
                            }
                            c.dismissController()
                        }, failureBlock: { (val) -> Void in
                            if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer_userWithInt_(user!.getId())) {
                                self.navigateDetail(customController)
                            } else {
                                self.navigateDetail(ConversationViewController(peer: ACPeer_userWithInt_(user!.getId())))
                            }
                            c.dismissController()
                        })
                    } else {
                        c.alertUser("FindNotFound")
                    }
                })
            }
        }
    }
    
    open func alertView(_ alertView: UIAlertView, clickedButtonAt buttonIndex: Int) {
        if buttonIndex == 1 {
            let textField = alertView.textField(at: 0)!
            if textField.text?.length > 0 {
                execute(Actor.findUsersCommand(withQuery: textField.text), successBlock: { (val) -> () in
                    var user: ACUserVM?
                    user = val as? ACUserVM
                    if user == nil {
                        if let users = val as? IOSObjectArray {
                            if Int(users.length()) > 0 {
                                if let tempUser = users.object(at: 0) as? ACUserVM {
                                    user = tempUser
                                }
                            }
                        }
                    }
                    if user != nil {
                        self.execute(Actor.addContactCommand(withUid: user!.getId())!, successBlock: { (val) -> () in
                                if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer_userWithInt_(user!.getId())) {
                                    self.navigateDetail(customController)
                                } else {
                                    self.navigateDetail(ConversationViewController(peer: ACPeer_userWithInt_(user!.getId())))
                                }
                            }, failureBlock: { (val) -> () in
                                self.showSmsInvitation([textField.text!])
                        })
                    } else {
                        self.showSmsInvitation([textField.text!])
                    }
                    }, failureBlock: { (val) -> () in
                        self.showSmsInvitation([textField.text!])
                })
            }
        }
    }
    
    // Email Invitation
    
    open func showEmailInvitation(_ recipients: [String]?) {
        if MFMailComposeViewController.canSendMail() {
            
            let messageComposeController = MFMailComposeViewController()
            messageComposeController.mailComposeDelegate = self
            
            // Replace
            messageComposeController.setSubject(inviteText)
            
            // TODO: Replace with bigger text
            messageComposeController.setMessageBody(inviteText, isHTML: false)
            messageComposeController.setToRecipients(recipients)
            present(messageComposeController, animated: true, completion: nil)
        }
    }
    
    open func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
        controller.dismiss(animated: true, completion: nil)
    }
    
    // SMS Invitation
    open func showSmsInvitation() {
        self.showSmsInvitation(nil)
    }
    
    open func showSmsInvitation(_ recipients: [String]?) {
        if MFMessageComposeViewController.canSendText() {
            let messageComposeController = MFMessageComposeViewController()
            messageComposeController.messageComposeDelegate = self
            messageComposeController.body = inviteText
            messageComposeController.recipients = recipients
            present(messageComposeController, animated: true, completion: nil)
        }
    }
    
    @objc open func messageComposeViewController(_ controller: MFMessageComposeViewController, didFinishWith result: MessageComposeResult) {
        controller.dismiss(animated: true, completion: nil)
    }
}
