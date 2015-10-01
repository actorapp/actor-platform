//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit
import MessageUI
import Social
import AddressBookUI
import ContactsUI

class ContactsViewController: ContactsContentViewController, ContactsContentViewControllerDelegate, UIAlertViewDelegate, MFMessageComposeViewControllerDelegate, MFMailComposeViewControllerDelegate {
    
    var inviteText: String {
        get {
            return localized("InviteText")
                .replace("{link}", dest: AppConfig.appInviteUrl!)
        }
    }
    
    override init() {
        super.init()
        
        content = ACAllEvents_Main.CONTACTS()
        
        tabBarItem = UITabBarItem(title: localized("TabPeople"),
            image: MainAppTheme.tab.createUnselectedIcon("TabIconContacts"),
            selectedImage: MainAppTheme.tab.createSelectedIcon("TabIconContactsHighlighted"))
        
        navigationItem.title = localized("TabPeople")
        navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Add, target: self, action: "findContact")
        
        delegate = self
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func contactDidTap(controller: ContactsContentViewController, contact: ACContact) -> Bool {
        navigateDetail(ConversationViewController(peer: ACPeer_userWithInt_(contact.uid)))
        return true
    }
    
    func willAddContacts(controller: ContactsContentViewController, section: ACManagedSection) {
        
        section.custom { (r: ACCustomRow<ContactActionCell>) -> () in
            
            r.height = 56
            
            r.closure = { (cell: ContactActionCell)->() in
                cell.bind("ic_add_user", actionTitle: localized("ContactsActionAdd"))
            }
            
            r.selectAction = { () -> Bool in
                self.findContact()
                return false
            }
        }
        
        section.custom { (r: ACCustomRow<ContactActionCell>) -> () in
            
            r.height = 56
            
            r.closure = { (cell: ContactActionCell)->() in
                cell.bind("ic_invite_user", actionTitle: localized("ContactsActionInvite"))
            }
            
            r.selectAction = { () -> Bool in
                
                let builder = MenuBuilder()
                
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
                
                if SLComposeViewController.isAvailableForServiceType(SLServiceTypeTencentWeibo) {
                    builder.add("Tencent Weibo") { () -> () in
                        let vc = SLComposeViewController(forServiceType: SLServiceTypeTencentWeibo)
                        vc.setInitialText(self.inviteText)
                        self.presentViewController(vc, animated: true, completion: nil)
                    }
                }
                
                if SLComposeViewController.isAvailableForServiceType(SLServiceTypeSinaWeibo) {
                    builder.add("Sina Weibo") { () -> () in
                        let vc = SLComposeViewController(forServiceType: SLServiceTypeSinaWeibo)
                        vc.setInitialText(self.inviteText)
                        self.presentViewController(vc, animated: true, completion: nil)
                    }
                }
                
                if SLComposeViewController.isAvailableForServiceType(SLServiceTypeTwitter) {
                    builder.add("Twitter") { () -> () in
                        let vc = SLComposeViewController(forServiceType: SLServiceTypeTwitter)
                        vc.setInitialText(self.inviteText)
                        self.presentViewController(vc, animated: true, completion: nil)
                    }
                }
                
                if SLComposeViewController.isAvailableForServiceType(SLServiceTypeFacebook) {
                    builder.add("Facebook") { () -> () in
                        let vc = SLComposeViewController(forServiceType: SLServiceTypeFacebook)
                        vc.addURL(NSURL(string: AppConfig.appInviteUrl!))
                        self.presentViewController(vc, animated: true, completion: nil)
                    }
                }
                
                self.showActionSheet(builder.items, cancelButton: "AlertCancel", destructButton: nil, sourceView: UIView(), sourceRect: CGRectZero, tapClosure: builder.tapClosure)
                
                return true
            }
        }
    }
 
    // Searching for contact
    
    func findContact() {
        
        startEditField { (c) -> () in
            c.title = "FindTitle"
            c.actionTitle = "NavigationFind"
            
            c.hint = "FindHint"
            c.fieldHint = "FindFieldHint"
            
            c.fieldAutocapitalizationType = .None
            c.fieldAutocorrectionType = .No
            c.fieldReturnKey = .Search
            
            c.didDoneTap = { (t, c) -> () in
                
                if t.length == 0 {
                    return
                }
                
                self.executeSafeOnlySuccess(Actor.findUsersCommandWithQuery(t), successBlock: { (val) -> Void in
                    var user: ACUserVM? = nil
                    if let users = val as? IOSObjectArray {
                        if Int(users.length()) > 0 {
                            if let tempUser = users.objectAtIndex(0) as? ACUserVM {
                                user = tempUser
                            }
                        }
                    }
                    
                    if user != nil {
                        c.execute(Actor.addContactCommandWithUid(user!.getId()), successBlock: { (val) -> Void in
                            self.navigateNext(ConversationViewController(peer: ACPeer_userWithInt_(user!.getId())))
                            c.dismiss()
                        }, failureBlock: { (val) -> Void in
                            self.navigateNext(ConversationViewController(peer: ACPeer_userWithInt_(user!.getId())))
                            c.dismiss()
                        })
                    } else {
                        c.alertUser("FindNotFound")
                    }
                })
            }
        }
    }
    
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        if buttonIndex == 1 {
            let textField = alertView.textFieldAtIndex(0)!
            if textField.text?.length > 0 {
                execute(Actor.findUsersCommandWithQuery(textField.text), successBlock: { (val) -> () in
                    var user: ACUserVM?
                    user = val as? ACUserVM
                    if user == nil {
                        if let users = val as? IOSObjectArray {
                            if Int(users.length()) > 0 {
                                if let tempUser = users.objectAtIndex(0) as? ACUserVM {
                                    user = tempUser
                                }
                            }
                        }
                    }
                    if user != nil {
                        self.execute(Actor.addContactCommandWithUid(user!.getId()), successBlock: { (val) -> () in
                            self.navigateDetail(ConversationViewController(peer: ACPeer_userWithInt_(user!.getId())))
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
    
    func showEmailInvitation(recipients: [String]?) {
        if MFMailComposeViewController.canSendMail() {
            
            if AppConfig.appInviteUrl == nil {
                // Silently ignore if not configured
                return
            }
            
            let messageComposeController = MFMailComposeViewController()
            messageComposeController.mailComposeDelegate = self
            
            // Replace
            messageComposeController.setSubject(inviteText)
            
            // Replace with bigger text
            messageComposeController.setMessageBody(inviteText, isHTML: false)
            messageComposeController.setToRecipients(recipients)
            
            // Hacking styles
            messageComposeController.navigationBar.tintColor = MainAppTheme.navigation.titleColor
            presentViewController(messageComposeController, animated: true, completion: { () -> Void in
                MainAppTheme.navigation.applyStatusBarFast()
            })
        }
    }
    
    func mailComposeController(controller: MFMailComposeViewController, didFinishWithResult result: MFMailComposeResult, error: NSError?) {
        controller.dismissViewControllerAnimated(true, completion: nil)
    }
    
    // SMS Invitation
    
    func showSmsInvitation(recipients: [String]?) {
        if MFMessageComposeViewController.canSendText() {
            
            if AppConfig.appInviteUrl == nil {
                // Silently ignore if not configured
                return
            }
            
            let messageComposeController = MFMessageComposeViewController()
            messageComposeController.messageComposeDelegate = self
            messageComposeController.body = inviteText
            messageComposeController.recipients = recipients
            
            // Hacking styles
            messageComposeController.navigationBar.tintColor = MainAppTheme.navigation.titleColor
            presentViewController(messageComposeController, animated: true, completion: { () -> Void in
                MainAppTheme.navigation.applyStatusBarFast()
            })
        }
    }
    
    func messageComposeViewController(controller: MFMessageComposeViewController, didFinishWithResult result: MessageComposeResult) {
        controller.dismissViewControllerAnimated(true, completion: nil)
    }
}