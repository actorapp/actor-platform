//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit
import MessageUI

public class AARootTabViewController : UITabBarController, MFMessageComposeViewControllerDelegate, UIAlertViewDelegate {
    
    private var appEmptyContainer = UIView()
    private var appIsSyncingPlaceholder = BigPlaceholderView(topOffset: 44 + 20)
    private var appIsEmptyPlaceholder = BigPlaceholderView(topOffset: 44 + 20)
    
    public init() {
        super.init(nibName: nil, bundle: nil)
        
        self.preferredContentSize = CGSize(width: 320.0, height: 600.0)
        self.preferredContentSize = CGSize(width: 320.0, height: 600.0)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("Not implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        appEmptyContainer.hidden = true
        appIsEmptyPlaceholder.hidden = true
        appIsEmptyPlaceholder.setImage(
            UIImage(named: "contacts_list_placeholder"),
            title: NSLocalizedString("Placeholder_Empty_Title", comment: "Placeholder Title"),
            subtitle: NSLocalizedString("Placeholder_Empty_Message", comment: "Placeholder Message"),
            actionTitle: NSLocalizedString("Placeholder_Empty_Action", comment: "Placeholder Action"),
            subtitle2: NSLocalizedString("Placeholder_Empty_Message2", comment: "Placeholder Message2"),
            actionTarget: self, actionSelector: Selector("showSmsInvitation"),
            action2title: NSLocalizedString("Placeholder_Empty_Action2", comment: "Placeholder Action2"),
            action2Selector: Selector("doAddContact"))
        appEmptyContainer.addSubview(appIsEmptyPlaceholder)
        
        appIsSyncingPlaceholder.hidden = true
        appIsSyncingPlaceholder.setImage(
            UIImage(named: "chat_list_placeholder"),
            title: NSLocalizedString("Placeholder_Loading_Title", comment: "Placeholder Title"),
            subtitle: NSLocalizedString("Placeholder_Loading_Message", comment: "Placeholder Message"))
        appEmptyContainer.addSubview(appIsSyncingPlaceholder)
        
        view.addSubview(appEmptyContainer)
        view.backgroundColor = UIColor.whiteColor()
    }
    
    public func showAppIsSyncingPlaceholder() {
        appIsEmptyPlaceholder.hidden = true
        appIsSyncingPlaceholder.hidden = false
        appEmptyContainer.hidden = false
    }

    public func showAppIsEmptyPlaceholder() {
        appIsEmptyPlaceholder.hidden = false
        appIsSyncingPlaceholder.hidden = true
        appEmptyContainer.hidden = false
    }
    
    public func hidePlaceholders() {
        appEmptyContainer.hidden = true
    }
    
    public func showSmsInvitation(phone: String?) {
        if MFMessageComposeViewController.canSendText() {

//            // Silently ignore if not configured
//            if AppConfig.appInviteUrl == nil {
//                return
//            }
//            
//            let messageComposeController = MFMessageComposeViewController()
//            messageComposeController.messageComposeDelegate = self
//            if (phone != nil) {
//                 messageComposeController.recipients = [phone!]
//            }
//            messageComposeController.body = localized("InviteText")
//                .replace("{link}", dest: AppConfig.appInviteUrl!)
//            messageComposeController.navigationBar.tintColor = MainAppTheme.navigation.titleColor
//            presentViewController(messageComposeController, animated: true, completion: { () -> Void in
//                MainAppTheme.navigation.applyStatusBarFast()
//            })
        } else {
            UIAlertView(title: "Error", message: "Cannot send SMS", delegate: nil, cancelButtonTitle: "OK").show()
        }
    }
    
    public func showSmsInvitation() {
        showSmsInvitation(nil)
    }
    
    public func doAddContact() {
        let alertView = UIAlertView(
            title: NSLocalizedString("ContactsAddHeader", comment: "Alert Title"),
            message: NSLocalizedString("ContactsAddHint", comment: "Alert Hint"),
            delegate: self,
            cancelButtonTitle: NSLocalizedString("AlertCancel", comment: "Alert Cancel"),
            otherButtonTitles: NSLocalizedString("AlertNext", comment: "Alert Next"))
        
        alertView.alertViewStyle = UIAlertViewStyle.PlainTextInput
        alertView.show()
    }
    
    // MARK: -
    // MARK: Layout
    
    public override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        appEmptyContainer.frame = view.bounds
        appIsSyncingPlaceholder.frame = CGRect(x: 0, y: 0, width: view.bounds.width, height: view.bounds.height)
        appIsEmptyPlaceholder.frame = CGRect(x: 0, y: 0, width: view.bounds.width, height: view.bounds.height)
    }
    
    public override func shouldAutorotate() -> Bool {
        return false
    }
    
    public override func supportedInterfaceOrientations() -> UIInterfaceOrientationMask {
        return UIInterfaceOrientationMask.Portrait
    }
    
    public override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return UIStatusBarStyle.LightContent
    }
    
    public func messageComposeViewController(controller: MFMessageComposeViewController, didFinishWithResult result: MessageComposeResult) {
        controller.dismissViewControllerAnimated(true, completion: nil)
    }

    public func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        // TODO: Localize
        if buttonIndex == 1 {
            let textField = alertView.textFieldAtIndex(0)!
            if textField.text?.length > 0 {
                self.execute(Actor.findUsersCommandWithQuery(textField.text), successBlock: { (val) -> Void in
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
                            // DO Nothing
                            }, failureBlock: { (val) -> () in
                                self.showSmsInvitation(textField.text)
                        })
                    } else {
                        self.showSmsInvitation(textField.text)
                    }
                    }, failureBlock: { (val) -> Void in
                        self.showSmsInvitation(textField.text)
                })
            }
        }
    }
}